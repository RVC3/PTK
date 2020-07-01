package ru.ppr.cppk.ui.activity.resultBarcodeCoupon;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.query.SaleWithCouponNumberQuery;
import ru.ppr.cppk.entity.event.model.CouponReadEvent;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.logic.PtkModeChecker;
import ru.ppr.cppk.logic.pdSale.PdSaleEnv;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.logic.pdSale.loader.DepStationsLoader;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.PermissionDvc;
import rx.Completable;

/**
 * @author Dmitry Nevolin
 */
public class ResultBarcodeCouponPresenter extends BaseMvpViewStatePresenter<ResultBarcodeCouponView, ResultBarcodeCouponViewState> {

    private InteractionListener interactionListener;
    private boolean initialized = false;
    ////////////////////
    private long couponReadEventId;
    private LocalDaoSession localDaoSession;
    private SecurityDaoSession securityDaoSession;
    private UiThread uiThread;
    private CommonSettings commonSettings;
    private PrivateSettings privateSettings;
    private NsiVersionManager nsiVersionManager;
    private PdSaleEnvFactory pdSaleEnvFactory;
    private StationRepository stationRepository;
    private PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder;
    private PtkModeChecker ptkModeChecker;
    private PermissionChecker permissionChecker;
    ////////////////////
    private Date timestamp;
    private int nsiVersion;
    private boolean tariffForSinglePdExists;
    private boolean tariffForBaggageExists;
    private boolean couponIsValid;
    private Station station;
    /**
     * Переменная для временного хранения категории ПД, для которой нужно запустить продажу
     */
    private int ticketCategoryForSale = (int) TicketCategory.Code.SINGLE;

    public ResultBarcodeCouponPresenter() {

    }

    @Override
    protected ResultBarcodeCouponViewState provideViewState() {
        return new ResultBarcodeCouponViewState();
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    void initialize(
            long couponReadEventId,
            UiThread uiThread,
            LocalDaoSession localDaoSession,
            PtkModeChecker ptkModeChecker,
            SecurityDaoSession securityDaoSession,
            CommonSettings commonSettings,
            PrivateSettings privateSettings,
            NsiVersionManager nsiVersionManager,
            PdSaleEnvFactory pdSaleEnvFactory,
            StationRepository stationRepository,
            PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder,
            PermissionChecker permissionChecker
    ) {
        if (!initialized) {
            this.initialized = true;
            this.couponReadEventId = couponReadEventId;
            this.uiThread = uiThread;
            this.localDaoSession = localDaoSession;
            this.ptkModeChecker = ptkModeChecker;
            this.securityDaoSession = securityDaoSession;
            this.commonSettings = commonSettings;
            this.privateSettings = privateSettings;
            this.nsiVersionManager = nsiVersionManager;
            this.pdSaleEnvFactory = pdSaleEnvFactory;
            this.stationRepository = stationRepository;
            this.pdSaleRestrictionsParamsBuilder = pdSaleRestrictionsParamsBuilder;
            this.permissionChecker = permissionChecker;
            onInitialized();
        }
    }

    private void onInitialized() {
        Completable
                .fromAction(() -> {
                    uiThread.post(() -> view.showProgress());

                    timestamp = new Date();
                    nsiVersion = nsiVersionManager.getCurrentNsiVersionId();

                    CouponReadEvent couponReadEvent = localDaoSession.getCouponReadEventDao().load(couponReadEventId);
                    station = stationRepository.load(couponReadEvent.getStationCode(), nsiVersion);
                    uiThread.post(() -> {
                        view.setStationName(station == null ? null : station.getName());
                        view.setPrintDateTime(couponReadEvent.getPrintDateTime());
                    });
                    // Проверяем на повторное использование
                    boolean alreadyUsed = new SaleWithCouponNumberQuery(localDaoSession, couponReadEvent.getPreTicketNumber()).query();
                    if (alreadyUsed) {
                        uiThread.post(() -> view.setErrorMessage(ResultBarcodeCouponView.ErrorMessage.ALREADY_USED));
                    } else {
                        uiThread.post(() -> view.setErrorMessage(ResultBarcodeCouponView.ErrorMessage.NONE));
                    }
                    // Проверяем время печати
                    boolean printDateTimeIsValid = true;
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR_OF_DAY, -commonSettings.getCouponValidityTime());
                    Date minPrintDateTime = calendar.getTime();
                    if (couponReadEvent.getPrintDateTime().before(minPrintDateTime)) {
                        printDateTimeIsValid = false;
                        uiThread.post(() -> {
                            view.showMoreThanNHoursError(commonSettings.getCouponValidityTime());
                            view.setPrintDateTimeValid(false);
                        });
                    }
                    // Проверяем наличие тарифов для станции
                    if (station != null) {
                        // Проверка на null на всякий случай, для устойчивости ко всему
                        tariffForSinglePdExists = isTariffExistForStation(station, (int) TicketCategory.Code.SINGLE);
                        tariffForBaggageExists = isTariffExistForStation(station, (int) TicketCategory.Code.BAGGAGE);
                    }
                    if (!tariffForSinglePdExists && !tariffForBaggageExists) {
                        uiThread.post(() -> view.setStationValid(false));
                    }

                    // Проверим станцию отправления
                    boolean isSaleForThisStationEnabledForCurrentWorkMode = true;
                    if (station != null) {
                        if (ptkModeChecker.isMobileCashRegisterOutputMode()) {
                            if (privateSettings.getCurrentStationCode() == station.getCode()) {
                                isSaleForThisStationEnabledForCurrentWorkMode = false;
                                uiThread.post(() -> {
                                    view.showSalePdDisabledError();
                                });
                            }
                        }
                    }

                    // Вычисляем валидность кода
                    couponIsValid = isSaleForThisStationEnabledForCurrentWorkMode && !alreadyUsed && printDateTimeIsValid && (tariffForSinglePdExists || tariffForBaggageExists);

                    // Настраиваем видимость кнопок оформления ПД
                    boolean salePdButtonVisibility = privateSettings.isSaleEnabled() && permissionChecker.checkPermission(PermissionDvc.SalePd);
                    boolean saleBaggageButtonVisibility = privateSettings.isSaleEnabled() && permissionChecker.checkPermission(PermissionDvc.SaleBaggage);
                    uiThread.post(() -> {
                        view.setSalePdBtnVisible(salePdButtonVisibility);
                        view.setBaggagePdBtnVisible(saleBaggageButtonVisibility);
                    });

                    // Настраиваем доступность кнопок оформления ПД
                    boolean salePdBtnEnabled = permissionChecker.checkPermission(PermissionDvc.SalePd);
                    boolean baggagePdBtnEnable = permissionChecker.checkPermission(PermissionDvc.SaleBaggage);
                    uiThread.post(() -> {
                        view.setSalePdBtnEnabled(salePdBtnEnabled);
                        view.setBaggagePdBtnEnabled(baggagePdBtnEnable);
                    });

                    uiThread.post(() -> {
                        view.setValidityStatus(couponIsValid);
                        view.hideProgress();
                    });
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();

    }

    void onSellPdClicked() {
        ticketCategoryForSale = (int) TicketCategory.Code.SINGLE;
        if (couponIsValid && !tariffForSinglePdExists) {
            view.showErrorTariffNotFound(station.getName());
            return;
        }
        navigateToPdSale(couponIsValid);
    }

    void onSellBaggageClicked() {
        ticketCategoryForSale = (int) TicketCategory.Code.BAGGAGE;
        if (couponIsValid && !tariffForBaggageExists) {
            view.showErrorTariffNotFound(station.getName());
            return;
        }
        navigateToPdSale(couponIsValid);
    }

    void onContinueWithoutTariffClicked() {
        navigateToPdSale(false);
    }

    private void navigateToPdSale(boolean withCouponReadEventId) {
        PdSaleParams pdSaleParams = new PdSaleParams();
        pdSaleParams.setTicketCategoryCode(ticketCategoryForSale);
        pdSaleParams.setDirectionCode(TicketWayType.OneWay.getCode());
        if (withCouponReadEventId) {
            pdSaleParams.setCouponReadEventId(couponReadEventId);
        }
        interactionListener.navigateToPdSale(pdSaleParams);
    }

    /**
     * Проверяет наличие тарифа для станции отправления.
     *
     * @param station            Станция отправления
     * @param ticketCategoryCode Категория ПД
     * @return {@code true} если тариф есть, {@code false} иначе
     */
    private boolean isTariffExistForStation(@NonNull Station station, int ticketCategoryCode) {
        // Получаем ограничения на оформление ПД
        PdSaleEnv pdSaleEnv;
        if (ticketCategoryCode == TicketCategory.Code.SINGLE) {
            pdSaleEnv = pdSaleEnvFactory.pdSaleEnvForSinglePd();
        } else if (ticketCategoryCode == TicketCategory.Code.BAGGAGE) {
            pdSaleEnv = pdSaleEnvFactory.pdSaleEnvForBaggage();
        } else {
            throw new IllegalArgumentException("Unknown ticketCategoryCode = " + ticketCategoryCode);
        }
        pdSaleEnv.pdSaleRestrictions().update(pdSaleRestrictionsParamsBuilder.create(timestamp, nsiVersion));
        DepStationsLoader depStationsLoader = pdSaleEnv.depStationsLoader();

        // Пытаемся получить станцию в списке тех, для которых есть тарифы
        List<Station> stationsWithTariffs = depStationsLoader.loadAllStations((long) station.getCode(), null, null);

        // Если станция есть в списке, можем оформлять от нее ПД
        return !stationsWithTariffs.isEmpty();
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void navigateToPdSale(PdSaleParams pdSaleParams);
    }

}

