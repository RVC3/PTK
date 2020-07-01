package ru.ppr.cppk.ui.fragment.pdSalePreparation;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.model.CouponReadEvent;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.CriticalNsiChecker;
import ru.ppr.cppk.logic.TicketStorageTypeToTicketTypeChecker;
import ru.ppr.cppk.logic.TrainPdCostCalculator;
import ru.ppr.cppk.logic.base.PdCostCalculator;
import ru.ppr.cppk.logic.pdSale.PdSaleEnv;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.model.ETicketDataParams;
import ru.ppr.cppk.model.PdSaleData;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.model.RemoveExemptionParams;
import ru.ppr.cppk.model.TariffsChain;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionParams;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionResult;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ExemptionGroup;
import ru.ppr.nsi.entity.FeeType;
import ru.ppr.nsi.entity.ProcessingFee;
import ru.ppr.nsi.entity.Region;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.entity.TrainCategoryPrefix;
import ru.ppr.nsi.repository.ExemptionGroupRepository;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.TrainCategoryRepository;
import rx.Completable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Aleksandr Brazhkin
 */
public class PdSalePreparationPresenter extends BaseMvpViewStatePresenter<PdSalePreparationView, PdSalePreparationViewState> {

    private static final String TAG = Logger.makeLogTag(PdSalePreparationPresenter.class);

    private static final int MAX_PD_COUNT = 10;
    private static final byte[] BYTES_FOR_CHECK_SIGN = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};

    private InteractionListener interactionListener;

    private boolean mInitialized = false;
    private int nsiVersion;
    private Date timestamp;
    private PdSaleParams mPdSaleParams;
    private PdSaleData mPdSaleData;
    private UiThread mUiThread;
    private NsiDaoSession mNsiDaoSession;
    private PrinterManager mPrinterManager;
    private TicketStorageTypeToTicketTypeChecker mTicketStorageTypeToTicketTypeChecker;
    private PrivateSettings mPrivateSettings;
    private PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder;
    private PdCostCalculator mPdCostCalculator;
    private NsiVersionManager nsiVersionManager;
    private CriticalNsiChecker criticalNsiChecker;
    private TrainCategoryRepository trainCategoryRepository;
    private StationRepository stationRepository;
    private ExemptionRepository exemptionRepository;
    private ExemptionGroupRepository exemptionGroupRepository;
    /////////////////////////////////////
    /**
     * Текущая категория поезда
     */
    private int trainCategoryCode = -1;
    /**
     * Компаратор для сортировки тарифных планов в выпадающем списке
     */
    private Comparator<TariffPlan> tariffPlanComparator;
    /**
     * Список тарифных планов для выбранных станций
     */
    private List<TariffPlan> mTariffPlans = Collections.emptyList();
    /**
     * Список типов ПД для выбранных станций
     */
    private List<TicketType> mTicketTypes = Collections.emptyList();
    /**
     * Стацнии отправления в выпадающем списке
     */
    private List<Station> mDepartureStations = Collections.emptyList();
    /**
     * Станции назначения в выпадающем списке
     */
    private List<Station> mDestinationStations = Collections.emptyList();
    /**
     * Кешированный список стацний отправления в выпадающем списке без фильтра по введенным символам.
     * Используется для быстрого обновления выпадающего списка напрямую из UI-потока при отмене выбора станции.
     */
    private List<Station> mDepartureStationsWithoutFilter = null;
    /**
     * Кешированный список стацний назначения в выпадающем списке без фильтра по введенным символам.
     * Используется для быстрого обновления выпадающего списка напрямую из UI-потока при отмене выбора станции.
     */
    private List<Station> mDestinationStationsWithoutFilter = null;
    /**
     * Транзитная станция для текущего маршрута
     */
    private Station mTransitStation = null;
    private Subscription checkSignSubscription = null;

    private PdSaleEnvFactory pdSaleEnvFactory;
    private PdSaleEnv pdSaleEnv;
    /**
     * Информация о льготе для UI
     * http://agile.srvdev.ru/browse/CPPKPP-34481
     * Со слов Рзянкиной Натальи Владимировны данные в льготах из разных регоинов не пойдут в конфликт,
     * поэтому отображаем информацию в UI только по первой льготе
     * Множество разных регионов в даннос случае ограничено Москвой и Московской областью
     */
    private ExemptionForEvent exemptionForEventForUi;
    /**
     * Информация о льготе для UI
     * http://agile.srvdev.ru/browse/CPPKPP-34481
     * Со слов Рзянкиной Натальи Владимировны данные в льготах из разных регоинов не пойдут в конфликт,
     * поэтому отображаем информацию в UI только по первой льготе
     * Множество разных регионов в даннос случае ограничено Москвой и Московской областью
     */
    private Exemption exemptionForUi;

    public PdSalePreparationPresenter() {

    }

    @Override
    protected PdSalePreparationViewState provideViewState() {
        return new PdSalePreparationViewState();
    }

    void initialize(PdSaleParams pdSaleParams,
                    PdSaleData pdSaleData,
                    UiThread uiThread,
                    NsiDaoSession nsiDaoSession,
                    PrinterManager printerManager,
                    PrivateSettings privateSettings,
                    PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder,
                    TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker,
                    PdSaleEnvFactory pdSaleEnvFactory,
                    NsiVersionManager nsiVersionManager,
                    CriticalNsiChecker criticalNsiChecker,
                    TrainCategoryRepository trainCategoryRepository,
                    StationRepository stationRepository,
                    ExemptionRepository exemptionRepository,
                    ExemptionGroupRepository exemptionGroupRepository) {
        if (!mInitialized) {
            this.mInitialized = true;
            this.mPdSaleParams = pdSaleParams;
            this.mPdSaleData = pdSaleData;
            this.mUiThread = uiThread;
            this.mNsiDaoSession = nsiDaoSession;
            this.mPrinterManager = printerManager;
            this.mPrivateSettings = privateSettings;
            this.pdSaleRestrictionsParamsBuilder = pdSaleRestrictionsParamsBuilder;
            this.mTicketStorageTypeToTicketTypeChecker = ticketStorageTypeToTicketTypeChecker;
            this.mPdCostCalculator = new TrainPdCostCalculator(mPdSaleData);
            this.pdSaleEnvFactory = pdSaleEnvFactory;
            this.nsiVersionManager = nsiVersionManager;
            this.criticalNsiChecker = criticalNsiChecker;
            this.trainCategoryRepository = trainCategoryRepository;
            this.stationRepository = stationRepository;
            this.exemptionRepository = exemptionRepository;
            this.exemptionGroupRepository = exemptionGroupRepository;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");

        if (criticalNsiChecker.checkCriticalNsiCloseDialogShouldBeShown()) {
            if (criticalNsiChecker.checkCriticalNsiCloseShiftPermissions()) {
                view.setCriticalNsiCloseShiftDialogVisible(true);
            } else {
                view.setCriticalNsiBackDialogVisible(true);
            }

            return;
        }

        Completable
                .fromAction(() -> {
                    mUiThread.post(view::showProgress);

                    timestamp = new Date();

                    nsiVersion = nsiVersionManager.getCurrentNsiVersionId();

                    int ticketCategoryCode = mPdSaleParams.getTicketCategoryCode();
                    if (ticketCategoryCode == TicketCategory.Code.SINGLE) {
                        pdSaleEnv = pdSaleEnvFactory.pdSaleEnvForSinglePd();
                    } else if (ticketCategoryCode == TicketCategory.Code.BAGGAGE) {
                        pdSaleEnv = pdSaleEnvFactory.pdSaleEnvForBaggage();
                    } else {
                        throw new IllegalArgumentException("Unknown ticketCategoryCode = " + ticketCategoryCode);
                    }
                    pdSaleEnv.pdSaleRestrictions().update(pdSaleRestrictionsParamsBuilder.create(timestamp, nsiVersion));

                    mUiThread.post(() -> {
                        // Устанавливаем текст в UI в зависимости от категории ПД
                        if (ticketCategoryCode == (int) TicketCategory.Code.BAGGAGE) {
                            view.setOnePdCostLabel(PdSalePreparationView.TicketCategoryLabel.BAGGAGE);
                            view.setTitle(PdSalePreparationView.TicketCategoryLabel.BAGGAGE);
                        } else {
                            view.setOnePdCostLabel(PdSalePreparationView.TicketCategoryLabel.SINGLE);
                            view.setTitle(PdSalePreparationView.TicketCategoryLabel.SINGLE);
                        }
                        // Заоблокируем кнопку переключения напралвения
                        view.setDirectionBtnEnabled(false);
                        // Настраиваем видимость кнопки "Электронный билет"
                        view.setSendETicketBtnVisible(mPrinterManager.getPrinter().isFederalLaw54Supported());
                        // Устанавливаем видимость кнопок типа оплаты
                        view.setPaymentTypeVisible(mPrivateSettings.isPosEnabled());
                    });
                    // Устанавливаем способ оплаты по умолчанию
                    mPdSaleData.setPaymentType(PaymentType.INDIVIDUAL_CASH);
                    // Устанавливаем количество ПД
                    setPdCount(1);
                    // Обновим состояние кнопок количества ПД
                    updatePdCountButtonsState();
                    // Устанавливаем текущую категорию поезда
                    if (mPrivateSettings.isMobileCashRegister()) {
                        trainCategoryCode = -1;
                    } else {
                        TrainCategoryPrefix trainCategoryPrefix = mPrivateSettings.getTrainCategoryPrefix();
                        TrainCategory trainCategory = trainCategoryRepository.getTrainCategoryToPrefix(
                                trainCategoryPrefix,
                                nsiVersion
                        );
                        trainCategoryCode = trainCategory.code;
                    }
                    // Настраиваем сортировку тарифных планов
                    if (mPrivateSettings.isMobileCashRegister()) {
                        tariffPlanComparator = (tariffPlan1, tariffPlan2) -> {
                            // В режиме мобильной кассы сортируем просто по коду
                            // https://aj.srvdev.ru/browse/CPPKPP-31605
                            return tariffPlan1.getCode() - tariffPlan2.getCode();
                        };
                    } else {
                        tariffPlanComparator = (tariffPlan1, tariffPlan2) -> {
                            // В поезде вверх поднимаем тарифные планы для текущей категории поезда, далее просто по коду
                            // https://aj.srvdev.ru/browse/CPPKPP-31605)
                            if (tariffPlan1.getTrainCategoryCode().equals(tariffPlan2.getTrainCategoryCode())) {
                                return tariffPlan1.getCode() - tariffPlan2.getCode();
                            } else if (tariffPlan1.getTrainCategoryCode() == trainCategoryCode) {
                                return -1;
                            } else if (tariffPlan2.getTrainCategoryCode() == trainCategoryCode) {
                                return 1;
                            } else {
                                return tariffPlan1.getCode() - tariffPlan2.getCode();
                            }
                        };
                    }
                    // Установливаем текст сбора
                    if ((mPrivateSettings.isMobileCashRegister() && mPrivateSettings.isOutputMode())) {
                        mUiThread.post(() -> view.setFeeLabel(PdSalePreparationView.FeeLabel.AT_DESTINATION_STATION));
                    } else {
                        mUiThread.post(() -> view.setFeeLabel(PdSalePreparationView.FeeLabel.IN_TRAIN));
                    }
                    // Устанавливаем текущие станции
                    Station departureStation = stationRepository.load(
                            mPdSaleParams.getDepartureStationCode(),
                            nsiVersion
                    );
                    Station destinationStation = stationRepository.load(
                            mPdSaleParams.getDestinationStationCode(),
                            nsiVersion
                    );
                    if (departureStation != null && destinationStation != null) {
                        // Устанавливаем обе станции при оформлении багажа к проданному ПД
                        setDepartureStation(departureStation);
                        setDestinationStation(destinationStation, true);
                    } else if (mPdSaleData.getCouponReadEvent() != null) {
                        // Устанавливаем станцию отправления из талона ТППД
                        CouponReadEvent couponReadEvent = mPdSaleData.getCouponReadEvent();
                        Station station = stationRepository.load(couponReadEvent.getStationCode(), nsiVersion);
                        mUiThread.post(() -> view.setDepartureStationEnabled(false));
                        setDepartureStation(station);
                        if (mPrivateSettings.isMobileCashRegister() && mPrivateSettings.isOutputMode()) {
                            // Устанавливаем текущую станцию в станцию назначения для режима мобильной кассы на выход
                            Station destStation = stationRepository.load(
                                    (long) mPrivateSettings.getCurrentStationCode(),
                                    nsiVersion
                            );
                            setDestinationStation(destStation, true);
                        }
                    } else if (mPrivateSettings.isMobileCashRegister()) {
                        // Устанавливаем текущую станцию для режима мобильной кассы
                        Station station = stationRepository.load(
                                (long) mPrivateSettings.getCurrentStationCode(),
                                nsiVersion
                        );
                        if (mPrivateSettings.isOutputMode()) {
                            setDestinationStation(station, true);
                        } else {
                            setDepartureStation(station);
                        }
                    } else {
                        // Обновляем станции в выпадающих списках
                        updateDepartureStations("");
                        updateDestinationStations("");
                    }
                    // Устанавливаем направление (по умолчанию "туда")
                    setDirection(TicketWayType.valueOf(mPdSaleParams.getDirectionCode()));
                    // Проверяем возможность смены направления
                    updateCanChangeDirection();
                    mUiThread.post(view::hideProgress);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    List<Station> onDepartureStationTextChanged(String text) {
        Logger.trace(TAG, "onDepartureStationTextChanged, text = " + text);
        updateDepartureStations(text);
        return mDepartureStations;
    }

    List<Station> onDestinationStationTextChanged(String text) {
        Logger.trace(TAG, "onDestinationStationTextChanged, text = " + text);
        updateDestinationStations(text);
        return mDestinationStations;
    }

    private Pair<List<Tariff>, List<Tariff>> findTariffs(TariffPlan tariffPlan, TicketType ticketType, Station departureStation, Station destinationStation) {
        if (tariffPlan != null && ticketType != null && departureStation != null && destinationStation != null) {

            Pair<List<TariffsChain>, List<TariffsChain>> foundTariffs = null;

            if (pdSaleEnv.pdSaleRestrictions().isDirectTariffAllowed((long) departureStation.getCode(), (long) destinationStation.getCode())) {
                foundTariffs = pdSaleEnv.tariffsLoader().loadDirectTariffsThereAndBack(
                        (long) departureStation.getCode(),
                        (long) destinationStation.getCode(),
                        (long) tariffPlan.getCode(),
                        (long) ticketType.getCode()
                );
            }
            if (foundTariffs != null && !foundTariffs.first.isEmpty()) {
                // Есть прямой тариф
                return new Pair<>(foundTariffs.first.get(0).getTariffs(),
                        foundTariffs.second.isEmpty() ? null : foundTariffs.second.get(0).getTariffs());
            } else {
                // Ищем транзитный маршрут
                foundTariffs = pdSaleEnv.tariffsLoader().loadTransitTariffsThereAndBack(
                        (long) departureStation.getCode(),
                        (long) destinationStation.getCode(),
                        (long) tariffPlan.getCode(),
                        (long) ticketType.getCode()
                );

                if (foundTariffs.first.isEmpty()) {
                    onUnhandledErrorOccurred("Tariff there not found", departureStation, destinationStation);

                    return null;
                } else {
                    // Есть транзитные тарифы
                    List<Tariff> thereTariffs = foundTariffs.first.get(0).getTariffs();
                    List<Tariff> backTariffs = foundTariffs.second.isEmpty() ? null : foundTariffs.second.get(0).getTariffs();
                    // Кладем в обратном порядке, чтобы можно было продавать билеты туда-обратно,
                    // перебирая индексы в одинковом порядке, а не в противоположных
                    if (backTariffs != null) {
                        Collections.reverse(backTariffs);
                    }
                    return new Pair<>(thereTariffs, backTariffs);
                }
            }
        } else {
            return null;
        }
    }

    private void setTariff(Pair<List<Tariff>, List<Tariff>> tariffsThereAndBack) {
        if (tariffsThereAndBack == null) {
            mPdSaleData.setTariffsThere(null);
            mPdSaleData.setTariffsBack(null);
            setTransitStation(null);
            return;
        }
        mPdSaleData.setTariffsThere(tariffsThereAndBack.first);
        mPdSaleData.setTariffsBack(tariffsThereAndBack.second);
        if (tariffsThereAndBack.first.size() == 1) {
            // Прямой маршрут
            setTransitStation(null);
        } else if (tariffsThereAndBack.first.size() == 2) {
            // Транзитный маршрут
            setTransitStation(tariffsThereAndBack.first.get(0).getStationDestination(mNsiDaoSession));
        } else {
            onUnhandledErrorOccurred("tariffsThereAndBack.first has incorrect size", null, null);
        }
    }

    private void setTransitStation(Station station) {
        mTransitStation = station;
        mUiThread.post(() -> view.setTransitStationName(mTransitStation == null ? null : mTransitStation.getName()));
    }

    private void updateDepartureStations(@NonNull String filter) {

        final String likeQuery = filter.toUpperCase(Locale.getDefault());

        if ("".equals(filter) && mDepartureStationsWithoutFilter != null) {
            // Используем кешированный список
            mDepartureStations = mDepartureStationsWithoutFilter;
        } else {
            mDepartureStations = pdSaleEnv.depStationsLoader().loadAllStations(
                    null,
                    mPdSaleData.getDestinationStation() == null ? null : (long) mPdSaleData.getDestinationStation().getCode(),
                    likeQuery);
            if ("".equals(filter)) {
                // Кешируем список станций
                mDepartureStationsWithoutFilter = mDepartureStations;
            }
        }
        mUiThread.post(() -> view.setDepartureStations(mDepartureStations));
    }

    private void updateDestinationStations(@NonNull String filter) {

        final String likeQuery = filter.toUpperCase(Locale.getDefault());

        if ("".equals(filter) && mDestinationStationsWithoutFilter != null) {
            // Используем кешированный список
            mDestinationStations = mDestinationStationsWithoutFilter;
        } else {
            mDestinationStations = pdSaleEnv.destStationsLoader().loadAllStations(
                    mPdSaleData.getDepartureStation() == null ? null : (long) mPdSaleData.getDepartureStation().getCode(),
                    null,
                    likeQuery);
            if ("".equals(filter)) {
                // Кешируем список станций
                mDestinationStationsWithoutFilter = mDestinationStations;
            }
        }
        mUiThread.post(() -> view.setDestinationStations(mDestinationStations));
    }

    private List<TicketType> findTicketTypes(TariffPlan tariffPlan, Station departureStation, Station destinationStation) {
        if (tariffPlan != null && departureStation != null && destinationStation != null) {
            // Грузим варианты только если выбраны обе станции
            List<TicketType> ticketTypes;
            if (pdSaleEnv.pdSaleRestrictions().isDirectTariffAllowed((long) departureStation.getCode(), (long) destinationStation.getCode())) {
                ticketTypes = pdSaleEnv.ticketTypesLoader().loadAllTicketTypes((long) departureStation.getCode(), (long) destinationStation.getCode(), (long) tariffPlan.getCode());
            } else {
                ticketTypes = pdSaleEnv.ticketTypesLoader().loadTransitTicketTypes((long) departureStation.getCode(), (long) destinationStation.getCode(), (long) tariffPlan.getCode());
            }
            return ticketTypes;
        } else {
            // Станция не выбрана, чистим типы ПД
            return Collections.emptyList();
        }
    }

    private void setTicketTypes(List<TicketType> ticketTypes) {
        mTicketTypes = ticketTypes;
        mUiThread.post(() -> view.setTicketTypes(mTicketTypes));
    }

    private void setTicketTypeByPosition(int position) {
        if (position == -1) {
            mPdSaleData.setTicketType(null);
        } else {
            mPdSaleData.setTicketType(mTicketTypes.get(position));
        }
        mUiThread.post(() -> view.setSelectedTicketTypePosition(position));
    }

    private List<TariffPlan> findTariffPlans(Station departureStation, Station destinationStation) {
        if (departureStation != null && destinationStation != null) {
            // Грузим варианты только если выбраны обе станции
            List<TariffPlan> tariffPlans;
            if (pdSaleEnv.pdSaleRestrictions().isDirectTariffAllowed((long) departureStation.getCode(), (long) destinationStation.getCode())) {
                tariffPlans = pdSaleEnv.tariffPlansLoader().loadAllTariffPlans((long) departureStation.getCode(), (long) destinationStation.getCode());
            } else {
                tariffPlans = pdSaleEnv.tariffPlansLoader().loadTransitTariffPlans((long) departureStation.getCode(), (long) destinationStation.getCode());
            }
            return tariffPlans;
        } else {
            // Станция не выбрана, чистим категории поездов
            return Collections.emptyList();
        }
    }

    private void setTariffPlans(List<TariffPlan> tariffPlans) {
        mTariffPlans = tariffPlans;
        mUiThread.post(() -> view.setTariffPlans(mTariffPlans));
    }

    private void setTariffPlanByPosition(int position) {
        if (position == -1) {
            mPdSaleData.setTariffPlan(null);
        } else {
            mPdSaleData.setTariffPlan(mTariffPlans.get(position));
        }
        mUiThread.post(() -> view.setSelectedTariffPlanPosition(position));
    }

    private void setExemptions(List<ExemptionForEvent> exemptionsForEvent) {
        if (exemptionsForEvent != null) {
            List<Pair<ExemptionForEvent, Exemption>> exemptions = new ArrayList<>();
            for (ExemptionForEvent exemptionForEvent : exemptionsForEvent) {
                // Ищем льготу в НСИ
                Exemption exemption = exemptionRepository.getExemption(exemptionForEvent.getCode(), exemptionForEvent.getActiveFromDate(), exemptionForEvent.getVersionId());
                if (exemption == null) {
                    onUnhandledErrorOccurred("Exemption is null", null, null);

                    return;
                }
                // Добавляем льготу в список
                exemptions.add(new Pair<>(exemptionForEvent, exemption));
            }

            mPdSaleData.setExemptions(exemptions);

            // http://agile.srvdev.ru/browse/CPPKPP-34481
            // Со слов Рзянкиной Натальи Владимировны данные в льготах из разных регоинов не пойдут в конфликт,
            // поэтому отображаем информацию в UI только по первой льготе
            // * Множество разных регионов в даннос случае ограничено Москвой и Московской областью
            exemptionForEventForUi = exemptions.get(0).first;
            exemptionForUi = exemptions.get(0).second;
            mUiThread.post(() -> {
                // Отображаем информацию о льготе
                view.setExemptionValue(
                        exemptionForUi.getExemptionExpressCode(),
                        exemptionForUi.getPercentage()
                );
            });
        } else {
            mPdSaleData.setExemptions(null);
            exemptionForEventForUi = null;
            exemptionForUi = null;
            mUiThread.post(() -> {
                // Отобажаем отсутствие льготы
                view.setExemptionValue(0, 0);
            });
        }
    }

    /**
     * Выполняет перерасчет выпадающего дохода.
     */
    private void updateLossSum() {
        List<Pair<ExemptionForEvent, Exemption>> exemptions = mPdSaleData.getExemptions();
        if (exemptions != null) {
            int i = 0;
            for (Pair<ExemptionForEvent, Exemption> exemptionPair : mPdSaleData.getExemptions()) {
                // Обновляем выпадающий доход
                BigDecimal lossSum = mPdCostCalculator.getOneTicketCostLossSum(i++);
                exemptionPair.first.setLossSumm(lossSum);
            }
        }
    }

    void onWriteToCardBtnClicked() {
        Logger.info(TAG, "onWriteToCardBtnClicked");
        if (mTransitStation != null) {
            // Временно оставляем, см. http://agile.srvdev.ru/browse/CPPKPP-35429
            throw new IllegalStateException("Button should not be available");
        }
        callRunnableAfterCheckingSign(() -> interactionListener.writePd());
    }

    void onPrintPdBtnClicked() {
        Logger.info(TAG, "onPrintPdBtnClicked");
        if (mTransitStation == null) {
            // Для прямых маршрутов всё как раньше
            // Не нужны проверки, все уже сделано на этапе настройки видимости кнопки
            callRunnableAfterCheckingSign(() -> interactionListener.printPd());
        } else {
            // Для транзитных маршрутов своя логика
            // http://agile.srvdev.ru/browse/CPPKPP-34481
            // Печать данного типа ПД может быть запрещена
            // Проверка не выполняется на этапе настройки видимости кнопки
            if (canCurrentTicketTypeBeSoldOnTicketStorageType(TicketStorageType.Paper)) {
                callRunnableAfterCheckingSign(() -> interactionListener.printPd());
            } else {
                showDeniedForSaleOnTicketStorageTypeError(TicketStorageType.Paper);
            }
        }
    }

    /**
     * Показать ошибку "Запрещено оформление ПД данного вида на тип носителя "<наименование типа носителя>" в UI
     *
     * @param ticketStorageType
     */
    private void showDeniedForSaleOnTicketStorageTypeError(TicketStorageType ticketStorageType) {
        String ticketStorageTypeName = mNsiDaoSession.getTicketStorageTypeDao().getName(ticketStorageType, nsiVersion);
        view.showDeniedForSaleOnTicketStorageTypeError(ticketStorageTypeName);
    }

    void onProcessBtnClicked() {
        Logger.info(TAG, "onProcessBtnClicked");
        if (mTransitStation == null) {
            // Для прямых маршрутов всё как раньше
            // Не нужны проверки, все уже сделано на этапе настройки видимости кнопки

            SmartCard smartCard = exemptionForEventForUi == null ? null : exemptionForEventForUi.getSmartCardFromWhichWasReadAboutExemption();
            if (smartCard != null) {
                Logger.info(TAG, "Exemption read from smart card: " + smartCard.toString());
                callRunnableAfterCheckingSign(() -> interactionListener.writePd());
            } else {
                Logger.info(TAG, "Exemption entered manually");
                //для багажа остается вероятность видимости этой кнопки, поэтому добавим проверку
                //http://agile.srvdev.ru/browse/CPPKPP-38545
                if (canCurrentTicketTypeBeSoldOnTicketStorageType(TicketStorageType.Paper)) {
                    callRunnableAfterCheckingSign(() -> interactionListener.printPd());
                } else {
                    showDeniedForSaleOnTicketStorageTypeError(TicketStorageType.Paper);
                }
            }
        } else {
            // Для транзитных маршрутов своя логика
            // http://agile.srvdev.ru/browse/CPPKPP-34481
            // Печать данного типа ПД может быть запрещена
            // Проверка не выполняется на этапе настройки видимости кнопки
            if (canCurrentTicketTypeBeSoldOnTicketStorageType(TicketStorageType.Paper)) {
                callRunnableAfterCheckingSign(() -> interactionListener.printPd());
            } else {
                showDeniedForSaleOnTicketStorageTypeError(TicketStorageType.Paper);
            }
        }
    }

    void onDecrementPdCountBtnClicked() {
        setPdCount(mPdSaleData.getPdCount() - 1);
        // Обновляем цену
        updateFee(true);
        updatePrice();
    }

    void onIncrementPdCountBtnClicked() {
        setPdCount(mPdSaleData.getPdCount() + 1);
        // Сбрасываем льготу
        setExemptions(null);
        // Обновляем цену
        updateFee(true);
        updatePrice();
    }

    /**
     * Устанавливает количество ПД.
     */
    private void setPdCount(int newValue) {
        mPdSaleData.setPdCount(newValue);
        mUiThread.post(() -> {
            view.setDecrementPdCountBtnEnabled(newValue - 1 >= 1);
            view.setIncrementPdCountBtnEnabled(newValue + 1 <= MAX_PD_COUNT);
            view.setPdCount(newValue);
        });
        // Обновим состояние кнопок продажи
        updateSaleButtonsState();
    }

    private void updatePdCountButtonsState() {
        /*
         * Условия для запрета продажи более 1-го ПД:
         * - Продажа по льготе
         * - Запись на карту
         * - Продажа ПД по талону ТППД
         */
        if (exemptionForEventForUi == null && canCurrentTicketTypeBeSoldOnTicketStorageType(TicketStorageType.Paper) && mPdSaleData.getCouponReadEvent() == null)
            mUiThread.post(() -> view.setPdCountLayoutEnabled(true));
        else {
            mUiThread.post(() -> view.setPdCountLayoutEnabled(false));
        }
    }

    void onDirectionBtnClicked() {
        if (mPdSaleData.getDirection() == TicketWayType.OneWay) {
            setDirection(TicketWayType.TwoWay);
        } else {
            setDirection(TicketWayType.OneWay);
        }
        // Обновляем цену
        mUiThread.post(this::updatePrice);
    }

    void onTicketTypeSelected(int position) {
        Logger.trace(TAG, "onTicketTypeSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    mUiThread.post(() -> view.showProgress());
                    // Устанавливаем выбранный тип ПД
                    TicketType ticketType = mTicketTypes.get(position);
                    setTicketTypeByPosition(position);
                    // Находим тариф "Туда" и парный ему тариф "Обратно"
                    Pair<List<Tariff>, List<Tariff>> tariffsThereAndBack = findTariffs(mPdSaleData.getTariffPlan(), ticketType, mPdSaleData.getDepartureStation(), mPdSaleData.getDestinationStation());
                    // http://agile.srvdev.ru/browse/CPPKPP-30337
                    // Заключение по ответам Рзянкиной Натальи: Не беспокоимся, что тарифа для другого тарифного плана в этом направлении может не быть.
                    setTariff(tariffsThereAndBack);
                    //сбросим количество ПД если необходимо
                    resetPdCountIfNeed();
                    //обновим доступность кнопок увеличения/уменьшения количества ПД
                    updatePdCountButtonsState();
                    // Обновим состояние кнопок продажи
                    updateSaleButtonsState();
                    // Сбрасываем льготу
                    setExemptions(null);
                    // Проверяем возможность смены направления
                    updateCanChangeDirection();
                    // Обновляем сбор и цену
                    mUiThread.post(() -> {
                        updateFee(true);
                        updatePrice();
                        view.setCostGroupVisible(true);
                    });
                    mUiThread.post(() -> view.hideProgress());
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    private void resetPdCountIfNeed() {
        if (!canCurrentTicketTypeBeSoldOnTicketStorageType(TicketStorageType.Paper)) {
            setPdCount(1);
        }
    }

    void onTariffPlanSelected(int position) {
        Logger.trace(TAG, "onTariffPlanSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    mUiThread.post(() -> view.showProgress());
                    // Устанавливаем выбранный тарифный план
                    TariffPlan tariffPlan = mTariffPlans.get(position);
                    setTariffPlanByPosition(position);
                    // Сбрасываем льготу
                    setExemptions(null);
                    // Обновляем сумму сбора
                    updateProcessingFee();
                    // Обновляем список типов ПД
                    updateTicketTypes();
                    // Находим тариф "Туда" и парный ему тариф "Обратно"
                    Pair<List<Tariff>, List<Tariff>> tariffsThereAndBack = findTariffs(tariffPlan, mPdSaleData.getTicketType(), mPdSaleData.getDepartureStation(), mPdSaleData.getDestinationStation());
                    // http://agile.srvdev.ru/browse/CPPKPP-30337
                    // Заключение по ответам Рзянкиной Натальи: Не беспокоимся, что тарифа для другого тарифного плана в это направлении может не быть.
                    setTariff(tariffsThereAndBack);
                    //сбросим количество ПД если необходимо
                    resetPdCountIfNeed();
                    //обновим доступность кнопок увеличения/уменьшения количества ПД
                    updatePdCountButtonsState();
                    // Обновим состояние кнопок продажи
                    updateSaleButtonsState();
                    // Проверяем возможность смены направления
                    updateCanChangeDirection();
                    // Обновляем сбор и цену
                    mUiThread.post(() -> {
                        updateFee(true);
                        updatePrice();
                        view.setCostGroupVisible(true);
                    });
                    mUiThread.post(() -> view.hideProgress());
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    private void setDepartureStation(Station station) {
        // Устнавливаем выбранную станцию
        mPdSaleData.setDepartureStation(station);
        mUiThread.post(() -> view.setDepartureStationName(mPdSaleData.getDepartureStation().getName()));
        // Вернем полный список станций отправления
        updateDepartureStations("");
        // Очищаем кешированный список станций наначения
        mDestinationStationsWithoutFilter = null;
        // Обновляем список станций наначения
        updateDestinationStations("");
        // Сбрасываем льготу
        setExemptions(null);
        // Обновляем список тарифных планов
        updateTariffPlans();
        // Обновляем сумму сбора
        updateProcessingFee();
        // Обновляем список типов ПД
        updateTicketTypes();
        // Ищем тариф
        Pair<List<Tariff>, List<Tariff>> tariffsThereAndBack = findTariffs(mPdSaleData.getTariffPlan(), mPdSaleData.getTicketType(), mPdSaleData.getDepartureStation(), mPdSaleData.getDestinationStation());
        setTariff(tariffsThereAndBack);
        //сбросим количество ПД если необходимо
        resetPdCountIfNeed();
        //обновим доступность кнопок увеличения/уменьшения количества ПД
        updatePdCountButtonsState();
        // Обновим состояние кнопок продажи
        updateSaleButtonsState();
        // Проверяем возможность смены направления
        updateCanChangeDirection();
        if (mPdSaleData.getTariffsThere() != null) {
            // Обновляем сбор и цену
            mUiThread.post(() -> {
                updateFee(true);
                updatePrice();
                view.setCostGroupVisible(true);
            });
        } else {
            // Настраиваем интерфейс
            mUiThread.post(() -> view.setCostGroupVisible(false));
        }
    }

    private void setDestinationStation(Station station, boolean shouldResetFee) {
        // Устнавливаем выбранную станцию
        mPdSaleData.setDestinationStation(station);
        mUiThread.post(() -> view.setDestinationStationName(mPdSaleData.getDestinationStation().getName()));
        // Вернем полный список станций наначения
        updateDestinationStations("");
        // Очищаем кешированный список станций отправления
        mDepartureStationsWithoutFilter = null;
        // Обновляем список станций отправления
        updateDepartureStations("");
        // Сбрасываем льготу
        setExemptions(null);
        // Обновляем список тарифных планов
        updateTariffPlans();
        // Обновляем сумму сбора
        updateProcessingFee();
        // Обновляем список типов ПД
        updateTicketTypes();
        // Ищем тариф
        Pair<List<Tariff>, List<Tariff>> tariffsThereAndBack = findTariffs(mPdSaleData.getTariffPlan(), mPdSaleData.getTicketType(), mPdSaleData.getDepartureStation(), mPdSaleData.getDestinationStation());
        setTariff(tariffsThereAndBack);
        //сбросим количество ПД если необходимо
        resetPdCountIfNeed();
        //обновим доступность кнопок увеличения/уменьшения количества ПД
        updatePdCountButtonsState();
        // Обновим состояние кнопок продажи
        updateSaleButtonsState();
        // Проверяем возможность смены направления
        updateCanChangeDirection();
        if (mPdSaleData.getTariffsThere() != null) {
            // Обновляем сбор и цену
            mUiThread.post(() -> {
                updateFee(shouldResetFee);
                updatePrice();
                view.setCostGroupVisible(true);
            });
        } else {
            // Настраиваем интерфейс
            mUiThread.post(() -> view.setCostGroupVisible(false));
        }
    }

    void onDepartureStationSelected(int position) {
        Logger.trace(TAG, "onDepartureStationSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    mUiThread.post(() -> view.showProgress());
                    setDepartureStation(mDepartureStations.get(position));
                    mUiThread.post(() -> view.hideProgress());
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onDestinationStationSelected(int position) {
        Logger.trace(TAG, "onDestinationStationSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    mUiThread.post(() -> view.showProgress());
                    // http://agile.srvdev.ru/browse/CPPKPP-32816
                    // Будем сбрасывать флаг, если до этого момента станция назначения вообще не была выбрана и CheckBox был скрыт
                    boolean shouldResetFee = mPdSaleData.getDestinationStation() == null;
                    setDestinationStation(mDestinationStations.get(position), shouldResetFee);
                    mUiThread.post(() -> view.hideProgress());
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    private void updateTariffPlans() {
        // Обновляем список тарифных планов
        List<TariffPlan> tariffPlans = findTariffPlans(mPdSaleData.getDepartureStation(), mPdSaleData.getDestinationStation());
        Collections.sort(tariffPlans, tariffPlanComparator);
        setTariffPlans(tariffPlans);
        // Ищем в новом списке индекс текущего тарифного плана
        TariffPlan currentTariffPlan = mPdSaleData.getTariffPlan();
        int indexOfCurrentTariffPlan = -1;
        if (currentTariffPlan != null) {
            for (int i = 0; i < tariffPlans.size(); i++) {
                TariffPlan tariffPlan = tariffPlans.get(i);
                if (currentTariffPlan.getCode().equals(tariffPlan.getCode())) {
                    Logger.trace(TAG, "updateTariffPlans - same tariffPlan: " + tariffPlan.getShortName());
                    indexOfCurrentTariffPlan = i;
                    break;
                }
            }
        }
        // Устанавливаем тарифный план
        if (mPrivateSettings.isMobileCashRegister() && indexOfCurrentTariffPlan != -1) {
            // https://aj.srvdev.ru/browse/CPPKPP-31605
            // Рзянкина Наталья Владимировна:
            // Пытаемся сохранить значение поля только в режиме мобильной кассы
            // Если мы едем в поезде, то будем сбрасывать
            setTariffPlanByPosition(indexOfCurrentTariffPlan);
        } else {
            // https://aj.srvdev.ru/browse/CPPKPP-31605
            // Рзянкина Наталья Владимировна:
            // Пытаемся сохранить значение поля только в режиме мобильной кассы
            // Если мы в поезде, то очистим поле тарифного плана, если нет тарифного плана для текущей категории поезда.
            boolean listIsEmpty = tariffPlans.isEmpty();
            boolean firstItemWithCurrentTrainCategory = !listIsEmpty && trainCategoryCode == mTariffPlans.get(0).getTrainCategoryCode();
            boolean saleInTrain = !mPrivateSettings.isMobileCashRegister();
            boolean shouldClearCurrentTariffPlan = listIsEmpty || saleInTrain && !firstItemWithCurrentTrainCategory;
            if (shouldClearCurrentTariffPlan) {
                Logger.trace(TAG, "updateTariffPlans - no tariffPlan");
                setTariffPlanByPosition(-1);
            } else {
                Logger.trace(TAG, "updateTariffPlans - first tariffPlan");
                setTariffPlanByPosition(0);
            }
        }
    }

    private void updateTicketTypes() {
        // Обновляем список типов ПД
        List<TicketType> ticketTypes = findTicketTypes(mPdSaleData.getTariffPlan(), mPdSaleData.getDepartureStation(), mPdSaleData.getDestinationStation());
        setTicketTypes(ticketTypes);
        // Ищем в новом списке индекс текущего типа ПД
        TicketType currentTicketType = mPdSaleData.getTicketType();
        int indexOfCurrentTicketType = -1;
        if (currentTicketType != null) {
            for (int i = 0; i < ticketTypes.size(); i++) {
                TicketType ticketType = ticketTypes.get(i);
                if (currentTicketType.getCode() == ticketType.getCode()) {
                    Logger.trace(TAG, "updateTicketTypes - same ticketType: " + ticketType.getShortName());
                    indexOfCurrentTicketType = i;
                    break;
                }
            }
        }
        // Устанавливаем тип ПД
        if (indexOfCurrentTicketType != -1) {
            setTicketTypeByPosition(indexOfCurrentTicketType);
        } else {
            boolean shouldClearCurrentTicketType = ticketTypes.isEmpty();
            if (shouldClearCurrentTicketType) {
                Logger.trace(TAG, "updateTicketTypes - no ticketType");
                setTicketTypeByPosition(-1);
            } else {
                Logger.trace(TAG, "updateTicketTypes - first ticketType");
                setTicketTypeByPosition(0);
            }
        }
    }

    private void updateProcessingFee() {
        Log.d(TAG, "updateProcessingFee()====== called");
        Log.e(TAG, "updateProcessingFee: ", new Exception());
        ProcessingFee processingFee = null;
        TariffPlan tariffPlan = mPdSaleData.getTariffPlan();
        TrainCategory trainCategory = tariffPlan == null ? null : tariffPlan.getTrainCategory(mNsiDaoSession);
        if (trainCategory != null) {
            FeeType feeType;
            if (mPrivateSettings.isMobileCashRegister() && mPrivateSettings.isOutputMode()) {
                feeType = FeeType.PD_AFTER_TRIP;
            } else {
                feeType = mPdSaleParams.getTicketCategoryCode() == (int) TicketCategory.Code.SINGLE ? FeeType.PD_IN_TRAIN : FeeType.BAGGAGE_IN_TRAIN;
            }
            processingFee = mNsiDaoSession.getProcessingFeeDao().getProcessingFee(trainCategory.code, feeType, nsiVersionManager.getCurrentNsiVersionId());
        }

        Log.d(TAG, "updateProcessingFee: " + processingFee);

      //  processingFee.setTax(new BigDecimal(16.67));

        mPdSaleData.setProcessingFee(processingFee);
    }

    private void updateCanChangeDirection() {
        if (mPdSaleData.getTariffsBack() != null) {
            // Если тариф обратно есть
            // Активируем кнопку переключения направления без каких-либо ограничений
            // http://agile.srvdev.ru/browse/CPPKPP-41022
            mUiThread.post(() -> view.setDirectionBtnEnabled(true));
        } else {
            // Если тарифа обратно нет
            // Блокируем кнопку переключения направления
            // Сбрасываем текущее направление на "Туда"
            mUiThread.post(() -> view.setDirectionBtnEnabled(false));
            setDirection(TicketWayType.OneWay);
        }
    }

    private boolean isChangeTakeFeeAllowed() {
        // Зафигачим всякие условия
        boolean isWorkModeDisableTakeFee = mPrivateSettings.isMobileCashRegister() && !mPrivateSettings.isOutputMode(); //режим работы блокирует возможность брать сбор
        boolean isExistFee = mPdSaleData.getProcessingFee() != null; //существует ли вообще сбор как таковой
        boolean isSaleByCoupon = mPdSaleData.getCouponReadEvent() != null;

        // Разрешаем вручную ставить/убирать галку, если:
        // - сбор существует
        // - сбор не блокируется режимом работы
        // - продажа производится без талона ТППД
        return !isWorkModeDisableTakeFee && isExistFee && !isSaleByCoupon;
    }

    private boolean isTakeFeeEnabledByDefault() {
        // Зафигачим всякие условия
        boolean isDefaultMode = !mPrivateSettings.isMobileCashRegister(); //находимся ли мы в обычно режиме работы
        boolean isOutputModeState = mPrivateSettings.isMobileCashRegister() && mPrivateSettings.isOutputMode(); //нажодимся ли мы в режиме работы мобильной кассы на выход
        boolean isWorkModeDisableTakeFee = mPrivateSettings.isMobileCashRegister() && !mPrivateSettings.isOutputMode(); //режим работы блокирует возможность брать сбор
        boolean isExistFee = mPdSaleData.getProcessingFee() != null; //существует ли вообще сбор как таковой
        boolean isExemptionExist = exemptionForUi != null; //существует ли льгота
        boolean isEnabledTakeFeeForExemption = isExemptionExist && exemptionForUi.isTakeProcessingFee(); //стоит ли флаг брать сбор для льготы
        boolean isExemptionDisabledTakeFee = isExemptionExist && !isEnabledTakeFeeForExemption; //данная льгота запрещает брать сбор
        boolean isExemptionDisabledTakeFeeConsideringWorkMode = isExemptionDisabledTakeFee && !isOutputModeState; //проверка на запрет брать сбор с учетом льготы и режима работы ПТК
        boolean isDepartureStationCanSaleTickets = mPdSaleData.getDepartureStation().isCanSaleTickets(); //есть ли БПА на станции отправления
        boolean isSaleByCoupon = mPdSaleData.getCouponReadEvent() != null;
        //если мы находимся в обычно режиме работы, либо в режиме работы мобильной кассы на выход,
        //то теоретически галочка может быть как включена так и выключена
        boolean isDefaultOrOutputStateMode = isOutputModeState || isDefaultMode;

        // В режиме мобильной кассы на выход сбор для доплаты по умолчанию не берем!!! https://aj.srvdev.ru/browse/CPPKPP-31739
        // Берем сбор, если:
        // - сбор не блокируется режимом работы,
        // - сбор существует,
        // - сбор не блокируется настройками льготы, принимая во внимание режим работы
        // - продажа производится без талона ТППД
        return isDefaultOrOutputStateMode && !isWorkModeDisableTakeFee && isExistFee && !isExemptionDisabledTakeFeeConsideringWorkMode && isDepartureStationCanSaleTickets & !isSaleByCoupon;
    }

    private void updateFee(boolean shouldResetFee) {
        boolean isTakeFee = isTakeFeeEnabledByDefault();
        boolean isEnabledCheckBox = isChangeTakeFeeAllowed();
        // Настроим чекбокс, чтобы при обновлении вьюхи не сбрасывался уже взведенный чекбокс
        if (shouldResetFee) {
            mPdSaleData.setIncludeFee(isTakeFee);
        } else {
            mPdSaleData.setIncludeFee(isEnabledCheckBox ? mPdSaleData.isIncludeFee() : isTakeFee);
        }
        mUiThread.post(() -> {
            view.setFeeChecked(mPdSaleData.isIncludeFee());
            view.setFeeEnabled(isEnabledCheckBox);
            // Если есть вероятность что льготу будем брать, то подпишем стоимость
            if (isTakeFee || isEnabledCheckBox) {
                view.setFeeValue(mPdSaleData.getProcessingFee().getTariff());
            } else {
                view.setFeeValue(null);
            }
        });
    }

    private void updateSaleButtonsState() {
        if (mTransitStation == null) {
            // Для прямых маршрутов всё как раньше
            if (exemptionForEventForUi == null && mPdSaleParams.getTicketCategoryCode() == (int) TicketCategory.Code.SINGLE) {
                if (canCurrentTicketTypeBeSoldOnTicketStorageType(TicketStorageType.Paper)) {
                    if (mPdSaleData.getPdCount() == 1) {
                        mUiThread.post(() -> view.setSaleButtonsState(PdSalePreparationView.SaleButtonsState.WRITE_AND_PRINT));
                    } else {
                        mUiThread.post(() -> view.setSaleButtonsState(PdSalePreparationView.SaleButtonsState.PRINT_ONLY));
                    }
                } else {
                    mUiThread.post(() -> view.setSaleButtonsState(PdSalePreparationView.SaleButtonsState.WRITE_ONLY));
                }
            } else {
                mUiThread.post(() -> view.setSaleButtonsState(PdSalePreparationView.SaleButtonsState.PROCESS));
            }
        } else {
            // Для транзитных маршрутов своя логика
            if (mPdSaleParams.getTicketCategoryCode() == (int) TicketCategory.Code.SINGLE) {
                // При продаже ПД доступна только кнопка "Распечатать билет"
                mUiThread.post(() -> view.setSaleButtonsState(PdSalePreparationView.SaleButtonsState.PRINT_ONLY));
            } else {
                // При продаже квитанции на багаж доступна только кнопка "Оформить"
                mUiThread.post(() -> view.setSaleButtonsState(PdSalePreparationView.SaleButtonsState.PROCESS));
            }
        }
    }

    void onExemptionSelected(@NonNull SelectExemptionResult selectExemptionResult) {
        // Устанавливаем льготу
        setExemptions(selectExemptionResult.getExemptionsForEvent());
        // Устанавливаем доп. информацию по ЭТТ
        mPdSaleData.setAdditionalInfoForEtt(selectExemptionResult.getAdditionalInfoForEtt());
        // Сбрасываем количество ПД
        setPdCount(1);
        // Обновляем состояние кнопок количества ПД
        updatePdCountButtonsState();
        // Обновляем сбор и цену
        updateFee(true);
        updatePrice();
        // Обновляем состояние кнопок продажи
        updateSaleButtonsState();
    }

    void onExemptionRemoved() {
        // Устанавливаем льготу
        setExemptions(null);
        // Устанавливаем доп. информацию по ЭТТ
        mPdSaleData.setAdditionalInfoForEtt(null);
        // Обновляем сбор и цену
        updateFee(true);
        updatePrice();
        // Обновляем состояние кнопок продажи
        updateSaleButtonsState();
        // Обновляем состояние кнопок количества ПД
        updatePdCountButtonsState();
    }

    /**
     * Производит обновление цен на экране
     */
    private void updatePrice() {

        updateLossSum();

        BigDecimal price = mPdCostCalculator.getOneTicketCostValueWithoutDiscountForAllTariffs();
        Logger.trace(TAG, "updatePrice, tariffPrice = " + price.toPlainString());

        BigDecimal ticketCostValueWithoutDiscount = mPdCostCalculator.getOneTicketCostValueWithoutDiscountForAllTariffs();
        Logger.trace(TAG, "updatePrice, ticketCostValueWithoutDiscount = " + ticketCostValueWithoutDiscount.toPlainString());
        view.setOnePdCost(ticketCostValueWithoutDiscount);

        BigDecimal totalCostValueWithDiscount = mPdCostCalculator.getAllTicketsTotalCostValueWithDiscount();
        if (totalCostValueWithDiscount.compareTo(BigDecimal.ZERO) == 0) {
            mPdSaleData.setPaymentType(PaymentType.INDIVIDUAL_CASH);
            view.setPaymentType(mPdSaleData.getPaymentType());
            view.setPaymentTypeEnabled(false);
        } else {
            view.setPaymentTypeEnabled(true);
        }

        Logger.trace(TAG, "updatePrice, totalCostValueWithDiscount = " + totalCostValueWithDiscount.toPlainString());
        view.setTotalCost(totalCostValueWithDiscount);
    }

    /**
     * Метод для проверки.
     *
     * @param runnable
     */
    private void callRunnableAfterCheckingSign(Runnable runnable) {

        if (checkSignSubscription != null) {
            return;
        }

        Logger.trace(TAG, "callRunnableAfterCheckingSign start");
        view.showProgress();


        checkSignSubscription = Single
                .fromCallable(() -> {
                    SignDataResult signDataResult = Di.INSTANCE.getEdsManager().signData(BYTES_FOR_CHECK_SIGN, new Date());
                    Logger.trace(TAG, "callRunnableAfterCheckingSign finish, signDataResult = " + signDataResult);
                    if (!signDataResult.isSuccessful()) {
                        throw new Exception("Could not to sign test data");
                    }
                    return signDataResult;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(SchedulersCPPK.eds())
                .subscribe(new SingleSubscriber<SignDataResult>() {
                    @Override
                    public void onSuccess(SignDataResult signDataResult) {
                        checkSignSubscription = null;
                        view.hideProgress();
                        runnable.run();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Logger.error(TAG, error);
                        checkSignSubscription = null;
                        view.hideProgress();
                        view.showEdsFailedError();
                    }
                });
    }

    /**
     * Метод для установки направления поездки.
     *
     * @param wayType тип направления билета.
     */
    private void setDirection(@NonNull final TicketWayType wayType) {
        mPdSaleData.setDirection(wayType);
        mUiThread.post(() -> view.setDirection(wayType));
    }

    /**
     * Производит проверку наличия на станции БПА или СК.
     *
     * @return {@code true} если на станции есть БПА или СК, false - иначе
     */
    private boolean hasBPAorSK(@NonNull final Station station) {
        return station.isCanSaleTickets();
    }

    /**
     * Метод для определения возможности оформления ПД на выбранный носитель.
     *
     * @return результат проверки.
     */
    private boolean canCurrentTicketTypeBeSoldOnTicketStorageType(TicketStorageType ticketStorageType) {
        if (mPdSaleData.getTicketType() == null) return false;
        return mTicketStorageTypeToTicketTypeChecker.check(ticketStorageType, mPdSaleData.getTicketType());
    }

    void onETicketDataSelected(ETicketDataParams eTicketDataParams) {
        mPdSaleData.seteTicketDataParams(eTicketDataParams);
    }

    void onCheckEdsFailedDialogClosed() {
        interactionListener.onCheckingECPFailed();
    }

    void onPaymentTypeChecked(PaymentType paymentType) {
        mPdSaleData.setPaymentType(paymentType);
        view.setPaymentType(paymentType);
    }

    void onDepartureStationEditCanceled() {
        Station station = mPdSaleData.getDepartureStation();
        view.setDepartureStationName(station == null ? "" : station.getName());
        // Вернем полный список станций
        // За счет кешированного списка без фильтра можем позолить себе сделать операцию в UI
        updateDepartureStations("");
    }

    void onDestinationStationEditCanceled() {
        Station station = mPdSaleData.getDestinationStation();
        view.setDestinationStationName(station == null ? "" : station.getName());
        // Вернем полный список станций
        // За счет кешированного списка без фильтра можем позолить себе сделать операцию в UI
        updateDestinationStations("");
    }

    void onExemptionClicked() {
        Logger.trace(TAG, "onExemptionClicked");
        if (mPdSaleData.getExemptions() == null) {

            if (!checkStationBeforeSelectExemption()) {
                // http://agile.srvdev.ru/browse/CPPKPP-34481
                return;
            }

            //http://agile.srvdev.ru/browse/CPPKPP-33826
            final Calendar pdEndTime = Calendar.getInstance();
            pdEndTime.setTime(new Date(System.currentTimeMillis()));
            pdEndTime.set(Calendar.HOUR_OF_DAY, 23);
            pdEndTime.set(Calendar.MINUTE, 59);
            pdEndTime.set(Calendar.SECOND, 59);
            pdEndTime.set(Calendar.MILLISECOND, 999);

            // Запускаем сценарий выбора льготы

            // http://agile.srvdev.ru/browse/CPPKPP-34481
            // Проверяем льготы отдельно для каждого тарифа
            List<Integer> regionCodes = new ArrayList<>();
            regionCodes.add(mPdSaleData.getDepartureStation().getRegionCode());
            if (mTransitStation != null) {
                regionCodes.add(mTransitStation.getRegionCode());
            }

            SelectExemptionParams selectExemptionParams = new SelectExemptionParams();
            selectExemptionParams.setPdStartDateTime(new Date());
            selectExemptionParams.setPdEndDateTime(pdEndTime.getTime());
            selectExemptionParams.setTariffPlanCode(mPdSaleData.getTariffPlan().getCode());
            selectExemptionParams.setTicketTypeCode(mPdSaleData.getTicketType().getCode());
            selectExemptionParams.setTrainCategory(mPdSaleData.getTariffPlan().getTrainCategory(mNsiDaoSession));
            selectExemptionParams.setRegionCodes(regionCodes);
            selectExemptionParams.setTicketCategoryCode((int) TicketCategory.Code.SINGLE);
            selectExemptionParams.setAllowReadFromBsc(true);
            selectExemptionParams.setVersionNsi(nsiVersion);
            selectExemptionParams.setTimeStamp(new Date());
            interactionListener.navigateToSelectExemption(selectExemptionParams);
        } else {
            // Запускаем сценарий удаления льготы
            ExemptionGroup exemptionGroup = exemptionForUi.getExemptionGroup(exemptionGroupRepository, nsiVersionManager.getCurrentNsiVersionId());
            RemoveExemptionParams removeExemptionParams = new RemoveExemptionParams();
            removeExemptionParams.setExpressCode(exemptionForEventForUi.getExpressCode());
            removeExemptionParams.setGroupName(exemptionGroup == null ? null : exemptionGroup.getGroupName());
            removeExemptionParams.setPercentage(exemptionForUi.getPercentage());
            removeExemptionParams.setFio(exemptionForEventForUi.getFio());
            removeExemptionParams.setDocumentNumber(exemptionForEventForUi.getNumberOfDocumentWhichApproveExemption());
            removeExemptionParams.setRequireSnilsNumber(exemptionForUi.isRequireSnilsNumber());
            SmartCard smartCard = exemptionForEventForUi.getSmartCardFromWhichWasReadAboutExemption();
            if (smartCard != null) {
                removeExemptionParams.setBscNumber(smartCard.getOuterNumber());
                removeExemptionParams.setBscType(smartCard.getType().getAbbreviation());
            }
            interactionListener.navigateToRemoveExemption(removeExemptionParams);
        }
    }

    /**
     * Выполняет проверку возможности использования льготы для выбранных станций.
     *
     * @return {@code true} если проверка пройдена успешно, {@code false} иначе
     */
    private boolean checkStationBeforeSelectExemption() {
        if (mTransitStation == null) {
            // http://agile.srvdev.ru/browse/CPPKPP-34481
            // Для прямых маршрутов нет ограничений
            // >> Запрещена ли продажа НЕтранзитного льготного ПД, если станция отправления и станция назначения находятся в разных регионах?
            // >> Нет, такого запрета нет. При продаже нетранзитного льготного в отношении регионов контролируется только регион предоставления льготы и регион действия.
            return true;
        } else {
            // http://agile.srvdev.ru/browse/CPPKPP-34481
            // Нужно убедиться, что все станции в одном регионе
            // >> Так у нас же запрещена продажа льготных ПД, если станция отправления, транзит и назначения находятся в разных регионах
            // Делаем специально через Set, без прямых сравнений: закладываемся на возможность продажи транзитных ПД через N станций.
            Set<Integer> regions = new HashSet<>(3);
            regions.add(mPdSaleData.getDepartureStation().getRegionCode());
            regions.add(mTransitStation.getRegionCode());
            regions.add(mPdSaleData.getDestinationStation().getRegionCode());
            if (regions.size() == 1) {
                return true;
            } else {
                if (regions.size() == 2 && regions.contains(Region.MOSCOW) && regions.contains(Region.MOSCOW_REGION)) {
                    // http://agile.srvdev.ru/browse/CPPKPP-34481
                    // Если все станции из двух регионов "Москва" и "Московская область", считаем, что проверка пройдена
                    // >> Москва и МО обрабатываются как один регион. Все остальное - как разные регионы
                    // >> В НСИ нет никакой информации о том, что Москва = МО, и это нужно просто зашивать в код
                    return true;
                }
                view.showExemptionInDifferentRegionsDeniedError();
                return false;
            }
        }
    }

    void onFeeCheckedChanged(boolean checked) {
        Logger.trace(TAG, "onFeeCheckedChanged, checked = " + checked);
        if (mPdSaleData.isIncludeFee() != checked) {
            view.setFeeChecked(checked);
            mPdSaleData.setIncludeFee(checked);
            updatePrice();
        }
    }

    void onSendETicketBtnClick() {
        Logger.trace(TAG, "onSendETicketBtnClick");
        interactionListener.onSendETicketBtnClick(mPdSaleData.geteTicketDataParams());
    }

    private void onUnhandledErrorOccurred(@NonNull String message, @Nullable Station departureStation, @Nullable Station destinationStation) {
        Logger.info(TAG + "|ATTENTION, UNHANDLED ERROR OCCURRED|",
                message + "|" + (departureStation == null ? null : departureStation.getName()) + "|" + (destinationStation == null ? null : destinationStation.getName()));
        view.setUnhandledErrorOccurredDialogVisible(true);
        Completable
                .fromAction(() -> {
                    // Сбросим станцию отправления
                    mPdSaleData.setDepartureStation(null);
                    // Сбросим станцию назначения
                    mPdSaleData.setDestinationStation(null);
                    // Вернем полный список станций отправления
                    mDepartureStationsWithoutFilter = null;
                    updateDepartureStations("");
                    // Вернем полный список станций наначения
                    mDestinationStationsWithoutFilter = null;
                    updateDestinationStations("");
                    mUiThread.post(() -> view.setDepartureStationName(null));
                    mUiThread.post(() -> view.setDestinationStationName(null));
                    // Сбросим льготу
                    setExemptions(null);
                    // Сбросим список тарифных планов
                    mPdSaleData.setTariffPlan(null);
                    updateTariffPlans();
                    mUiThread.post(() -> view.setSelectedTariffPlanPosition(-1));
                    // Сбросим сумму сбора
                    updateProcessingFee();
                    // Сбросим список типов ПД
                    mPdSaleData.setTicketType(null);
                    updateTicketTypes();
                    mUiThread.post(() -> view.setSelectedTicketTypePosition(-1));
                    // Сбросим тариф
                    setTariff(null);
                    // Сбросим количество ПД если необходимо
                    resetPdCountIfNeed();
                    // Сбросим доступность кнопок увеличения/уменьшения количества ПД
                    mUiThread.post(() -> view.setPdCountLayoutEnabled(false));
                    // Проверяем возможность смены направления
                    updateCanChangeDirection();
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onUnhandledErrorOccurredDialogDismiss() {
        view.setUnhandledErrorOccurredDialogVisible(false);
    }

    void onCriticalNsiBackDialogRead() {
        interactionListener.navigateBack();
    }

    void onCriticalNsiCloseShiftDialogRead() {
        interactionListener.navigateToCloseShiftActivity();
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void onSendETicketBtnClick(ETicketDataParams eTicketDataParams);

        void onCheckingECPFailed();

        void navigateToSelectExemption(SelectExemptionParams selectExemptionParams);

        void navigateToRemoveExemption(RemoveExemptionParams removeExemptionParams);

        void writePd();

        void printPd();

        void navigateBack();

        void navigateToCloseShiftActivity();
    }

}
