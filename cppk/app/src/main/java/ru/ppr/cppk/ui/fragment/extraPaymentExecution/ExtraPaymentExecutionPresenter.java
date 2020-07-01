package ru.ppr.cppk.ui.fragment.extraPaymentExecution;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.ppr.core.helper.Resources;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.R;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.CriticalNsiChecker;
import ru.ppr.cppk.logic.pdSale.PdSaleEnv;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParams;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.model.ETicketDataParams;
import ru.ppr.cppk.model.ExtraPaymentParams;
import ru.ppr.cppk.model.RemoveExemptionParams;
import ru.ppr.cppk.model.TariffsChain;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionParams;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionResult;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ExemptionGroup;
import ru.ppr.nsi.entity.FeeType;
import ru.ppr.nsi.entity.ProcessingFee;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.query.TariffPlansQuery;
import ru.ppr.nsi.repository.ExemptionGroupRepository;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;
import ru.ppr.nsi.repository.TrainCategoryRepository;
import rx.Completable;

/**
 * @author Aleksandr Brazhkin
 */
public class ExtraPaymentExecutionPresenter extends BaseMvpViewStatePresenter<ExtraPaymentExecutionView, ExtraPaymentExecutionViewState> {

    private static final String TAG = Logger.makeLogTag(ExtraPaymentExecutionPresenter.class);

    private InteractionListener interactionListener;

    private boolean mInitialized = false;
    private int mNsiVersion;
    private Date mTimeStamp;
    private PrivateSettings mPrivateSettings;
    private CommonSettings mCommonSettings;
    private Resources mResources;
    private ExtraPaymentParams mExtraPaymentParams;
    private DataSalePD mDataSalePd;
    private NsiDaoSession mNsiDaoSession;
    private Tariff mParentTariff;
    private NsiVersionManager nsiVersionManager;
    private CriticalNsiChecker criticalNsiChecker;
    private TariffRepository tariffRepository;
    private TariffPlanRepository tariffPlanRepository;
    private TrainCategoryRepository trainCategoryRepository;
    private TicketTypeRepository ticketTypeRepository;
    private ExemptionRepository exemptionRepository;
    private ExemptionGroupRepository exemptionGroupRepository;
    /**
     * Список тарифных планов для выбранных станций
     */
    private List<TariffPlan> mTariffPlans = Collections.emptyList();
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
     * UI-поток
     */
    private UiThread mUiThread;

    private PdSaleEnvFactory pdSaleEnvFactory;
    private PdSaleEnv pdSaleEnv;
    private PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder;

    public ExtraPaymentExecutionPresenter() {

    }

    @Override
    protected ExtraPaymentExecutionViewState provideViewState() {
        return new ExtraPaymentExecutionViewState();
    }

    void initialize(
            UiThread uiThread,
            PrivateSettings privateSettings,
            CommonSettings commonSettings,
            NsiDaoSession nsiDaoSession,
            Resources resources,
            ExtraPaymentParams extraPaymentParams,
            DataSalePD dataSalePd,
            NsiVersionManager nsiVersionManager,
            CriticalNsiChecker criticalNsiChecker,
            TariffRepository tariffRepository,
            TariffPlanRepository tariffPlanRepository,
            TrainCategoryRepository trainCategoryRepository,
            TicketTypeRepository ticketTypeRepository,
            ExemptionRepository exemptionRepository,
            ExemptionGroupRepository exemptionGroupRepository,
            PdSaleEnvFactory pdSaleEnvFactory,
            PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder) {
        if (!mInitialized) {
            mUiThread = uiThread;
            mInitialized = true;
            mPrivateSettings = privateSettings;
            mCommonSettings = commonSettings;
            mNsiDaoSession = nsiDaoSession;
            mResources = resources;
            mExtraPaymentParams = extraPaymentParams;
            mDataSalePd = dataSalePd;
            this.nsiVersionManager = nsiVersionManager;
            this.criticalNsiChecker = criticalNsiChecker;
            this.tariffRepository = tariffRepository;
            this.tariffPlanRepository = tariffPlanRepository;
            this.trainCategoryRepository = trainCategoryRepository;
            this.ticketTypeRepository = ticketTypeRepository;
            this.exemptionRepository = exemptionRepository;
            this.exemptionGroupRepository = exemptionGroupRepository;
            this.pdSaleEnvFactory = pdSaleEnvFactory;
            this.pdSaleRestrictionsParamsBuilder = pdSaleRestrictionsParamsBuilder;

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
                    mUiThread.post(view::showProgressDialog);

                    mTimeStamp = new Date();
                    mNsiVersion = nsiVersionManager.getCurrentNsiVersionId();

                    //Заполняем ConnectionType
                    mDataSalePd.setConnectionType(ConnectionType.SURCHARGE);

                    // Заполняем ParentTicketInfo
                    ParentTicketInfo parentTicketInfo = new ParentTicketInfo();
                    parentTicketInfo.setSaleDateTime(mExtraPaymentParams.getParentPdSaleDateTime());
                    parentTicketInfo.setTicketNumber(mExtraPaymentParams.getParentPdNumber());
                    parentTicketInfo.setWayType(TicketWayType.valueOf(mExtraPaymentParams.getParentPdDirectionCode()));
                    parentTicketInfo.setCashRegisterNumber(mExtraPaymentParams.getParentPdDeviceId());
                    mDataSalePd.setParentTicketInfo(parentTicketInfo);

                    //заполняем AdditionalInoForEtt
                    mDataSalePd.setAdditionalInfoForEttFromCard(mExtraPaymentParams.getAdditionalInfoForEtt());

                    // Ищем родительский тариф
                    mParentTariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                            mExtraPaymentParams.getParentPdTariffCode(),
                            nsiVersionManager.getNsiVersionIdForDate(mExtraPaymentParams.getParentPdSaleDateTime()));

                    // Заполняем "окружение"
                    pdSaleEnv = pdSaleEnvFactory.pdSaleEnvForExtraPayment();
                    PdSaleRestrictionsParams pdSaleRestrictionsParams = pdSaleRestrictionsParamsBuilder.create(mTimeStamp, mNsiVersion);
                    PdSaleRestrictionsParams.ExtraPaymentData extraPaymentData = new PdSaleRestrictionsParams.ExtraPaymentData();
                    extraPaymentData.setParentDepartureStationCode(mParentTariff.getStationDepartureCode() != null ? Long.valueOf(mParentTariff.getStationDepartureCode()) : null);
                    extraPaymentData.setParentDestinationStationCode(mParentTariff.getStationDestinationCode() != null ? Long.valueOf(mParentTariff.getStationDestinationCode()) : null);
                    extraPaymentData.setParentDepartureTariffZoneCode(mParentTariff.getDepartureTariffZoneCode());
                    extraPaymentData.setParentDestinationTariffZoneCode(mParentTariff.getDestinationTariffZoneCode());
                    pdSaleRestrictionsParams.setExtraPaymentData(extraPaymentData);
                    pdSaleEnv.pdSaleRestrictions().update(pdSaleRestrictionsParams);

                    // Показываем информацию о ПД-основании
                    ExtraPaymentExecutionView.ParentPdInfo parentPdInfo = new ExtraPaymentExecutionView.ParentPdInfo();
                    parentPdInfo.ticketType = mParentTariff.getTicketType(mNsiDaoSession).toString();
                    parentPdInfo.pdNumber = mExtraPaymentParams.getParentPdNumber();
                    parentPdInfo.departureStation = mParentTariff.getStationDeparture(mNsiDaoSession).getName();
                    parentPdInfo.destinationStation = mParentTariff.getStationDestination(mNsiDaoSession).getName();
                    parentPdInfo.trainCategory = mParentTariff.getTariffPlan(tariffPlanRepository).getTrainCategory(mNsiDaoSession).name;
                    parentPdInfo.exemptionCode = mExtraPaymentParams.getParentPdExemptionExpressCode();
                    parentPdInfo.direction = TicketWayType.valueOf(mExtraPaymentParams.getParentPdDirectionCode());
                    // http://agile.srvdev.ru/browse/CPPKPP-43304
                    // Отображаем текущую дату
                    parentPdInfo.startDateTime = mTimeStamp;
                    parentPdInfo.terminalNumber = mExtraPaymentParams.getParentPdDeviceId();
                    mUiThread.post(() -> view.setParentPdInfo(parentPdInfo));

                    // Тип билета для доплаты
                    // Устанавливем "Разовый полный" или "Разовый детский"
                    // http://agile.srvdev.ru/browse/CPPKPP-33774
                    long parentTicketTypeCode = mParentTariff.getTicketType(mNsiDaoSession).getCode();
                    long ticketTypeCode;
                    if (parentTicketTypeCode == TicketType.Code.SINGLE_CHILD) {
                        ticketTypeCode = TicketType.Code.SINGLE_CHILD;
                    } else {
                        ticketTypeCode = TicketType.Code.SINGLE_FULL;
                    }
                    TicketType ticketType = ticketTypeRepository.load((int) ticketTypeCode, mNsiVersion);
                    mDataSalePd.setTicketType(ticketType);

                    // Устанавливаем категорию поезда
                    // Категорию поезда хардкодим, на PrivateSettings.getTrainCategoryPrefix() завязываться нельзя,
                    // потому что в режиме мобильной кассы всегда устновлена категория 6000, а доплату при этом оформлять можно
                    TrainCategory trainCategory = trainCategoryRepository.load(TrainCategory.CATEGORY_CODE_7, mNsiVersion);
                    mDataSalePd.setTrainCategory(trainCategory);

                    // Устанавливаем сбор
                    FeeType feeType = (mPrivateSettings.isMobileCashRegister() && mPrivateSettings.isOutputMode()) ?
                            FeeType.PD_AFTER_TRIP : FeeType.PD_IN_TRAIN;
                    ProcessingFee processingFee = mNsiDaoSession.getProcessingFeeDao().getProcessingFee(mDataSalePd.getTrainCategory().code, feeType, nsiVersionManager.getCurrentNsiVersionId());
                    mDataSalePd.setProcessingFee(processingFee);
                    mUiThread.post(() -> {
                        // Отображаем нужное наименование сбора
                        view.setFeeLabel(feeType == FeeType.PD_AFTER_TRIP ? mResources.getString(R.string.fee_for_registration_at_destination) : mResources.getString(R.string.fee_for_train_category));
                        // Устанавливаем видимость кнопок типа оплаты
                        view.setPaymentTypeVisible(mPrivateSettings.isPosEnabled() && mCommonSettings.isExtraPaymentWithCardAllowed());
                        // Запретим выбор льготы, если ПД-основание без льготы
                        view.setExemptionEnabled(mExtraPaymentParams.getParentPdExemptionExpressCode() != 0);
                    });

                    int parentTicketCategoryCode = mParentTariff.getTicketType(mNsiDaoSession).getTicketCategoryCode();
                    TicketCategoryChecker ticketCategoryChecker = new TicketCategoryChecker();
                    if (ticketCategoryChecker.isTrainSingleTicket(parentTicketCategoryCode) ||
                            ticketCategoryChecker.isTrainSeasonTicket(parentTicketCategoryCode) ||
                            ticketCategoryChecker.isCombinedCountTripsSeasonTicket(parentTicketCategoryCode)) {
                        initializeForTicket();
                    } else {
                        throw new IllegalArgumentException("Unknown ticketCategoryCode: " + parentTicketCategoryCode);
                    }

                    mUiThread.post(view::hideProgressDialog);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    private void setTariffPlans(List<TariffPlan> tariffPlans) {
        mTariffPlans = tariffPlans;
        mUiThread.post(() -> view.setTariffPlans(mTariffPlans));
    }

    private void setTariffPlanByPosition(int position) {
        if (position == -1) {
            mDataSalePd.setTariffPlan(null);
        } else {
            mDataSalePd.setTariffPlan(mTariffPlans.get(position));
        }
        mUiThread.post(() -> view.setSelectedTariffPlanPosition(position));
    }

    private void setTariff(Pair<Tariff, Tariff> tariffsThereAndBack, TicketWayType direction) {
        if (tariffsThereAndBack == null) {
            mDataSalePd.setTariffThere(null);
            mDataSalePd.setTariffBack(null);
            return;
        }
        if (direction == TicketWayType.TwoWay) {
            mDataSalePd.setTariffThere(tariffsThereAndBack.second);
            mDataSalePd.setTariffBack(tariffsThereAndBack.first);
        } else {
            mDataSalePd.setTariffThere(tariffsThereAndBack.first);
            mDataSalePd.setTariffBack(tariffsThereAndBack.second);
        }
    }

    private void initializeForTicket() {
        Station departureStation = mParentTariff.getStationDeparture(mNsiDaoSession);
        Station destinationStation = mParentTariff.getStationDestination(mNsiDaoSession);
        boolean tariffForDepartureStationExists = false;
        boolean tariffForDestinationStationExists = false;
        // Разрешаем редактирование полей со станциями
        mUiThread.post(() -> {
            view.setStationFieldsEnabled(true);
            view.setClearStationsBtnEnabled(true);
        });
        // Пробуем автоматически подставить стацнии отправления/назначения
        // Ищем подходящие тарифные планы для станции отправления родительского ПД
        List<TariffPlan> tariffPlansForDepartureStation = new TariffPlansQuery(
                mNsiDaoSession,
                departureStation.getCode(),
                -1,
                true,
                mDataSalePd.getTrainCategory().code,
                Collections.singletonList((long) mDataSalePd.getTicketType().getCode()),
                mNsiVersion
        ).query();
        if (!tariffPlansForDepartureStation.isEmpty()) {
            // Ищем подходящие тарифы для станции отправления родительского ПД
            Pair<Tariff, Tariff> tariffForDepartureStation = tariffRepository.loadDirectTariffs(
                    Collections.singletonList((long) departureStation.getCode()),
                    null,
                    Collections.singletonList((long) mDataSalePd.getTicketType().getCode()),
                    tariffPlansForDepartureStation,
                    mNsiVersion);
            if (tariffForDepartureStation != null) {
                tariffForDepartureStationExists = true;
            }
        }
        // Ищем подходящие тарифные планы для станции назначения родительского ПД
        List<TariffPlan> tariffPlansForDestinationStation = new TariffPlansQuery(
                mNsiDaoSession,
                -1,
                destinationStation.getCode(),
                true,
                mDataSalePd.getTrainCategory().code,
                Collections.singletonList((long) mDataSalePd.getTicketType().getCode()),
                mNsiVersion
        ).query();
        if (!tariffPlansForDestinationStation.isEmpty()) {
            // Ищем подходящие тарифы для станции назначения родительского ПД
            Pair<Tariff, Tariff> tariffForDestinationStation = tariffRepository.loadDirectTariffs(
                    null,
                    Collections.singletonList((long) destinationStation.getCode()),
                    Collections.singletonList((long) mDataSalePd.getTicketType().getCode()),
                    tariffPlansForDestinationStation,
                    mNsiVersion);
            if (tariffForDestinationStation != null) {
                tariffForDestinationStationExists = true;
            }
        }
        if (tariffForDepartureStationExists && tariffForDestinationStationExists) {
            // Есть тариф доплаты между станциями, подтянем его автоматически
            // Ищем подоходящие тарифные планы
            List<TariffPlan> tariffPlans = findTariffPlans(departureStation, destinationStation);
            setTariffPlans(tariffPlans);
            // см. http://agile.srvdev.ru/browse/CPPKPP-40028
            // 1. Кнопка "доплата" была показана т.к. был хотябы 1 тариф с учётом Большой Москвы,
            // при этом от станции отправления есть тариф доплаты куда то, и до станции назначения есть тариф доплаты откуда то.
            // 2. Раньше этого было достаточно, т.к. при проверке доступности кнопки учатствовали только станции в рамках маршрута, а теперь
            // может возникнуть такая ситуация, когда МЕЖДУ станцией отправления и назначения из билетика тарифа доплаты нет,
            // хотя все условия из п.1 сходятся. Не добавляем станций и ничего не выбираем, пусть тариф выбирается руками.
            if (!tariffPlans.isEmpty()) {
                // Если тарифный план есть, работаем как раньше

                // Устанавливаем станции
                mDataSalePd.setDepartureStation(departureStation);
                mDataSalePd.setDestinationStation(destinationStation);

                for (int i = 0; i < tariffPlans.size(); i++) {
                    // Находим тариф "Туда" и парный ему тариф "Обратно"
                    Pair<Tariff, Tariff> tariffsThereAndBack = findTariffs(tariffPlans.get(i), departureStation, destinationStation);
                    // Устанавливаем данный (первый попавшийся) тарифный план и направление "Туда", если оно есть
                    setTariffPlanByPosition(i);

                    if (TicketWayType.TwoWay.equals(mDataSalePd.getParentTicketInfo().getWayType())) {
                        // Если родительский билет Туда и Обратно, то проверяем, есть ли доплата Туда
                        if (tariffsThereAndBack.first != null) {
                            setTariff(tariffsThereAndBack, TicketWayType.OneWay);
                        } else {
                            // если есть только тариф Обратно, то меняем местами станции и тарифы
                            setTariff(tariffsThereAndBack, TicketWayType.TwoWay);
                            mDataSalePd.setDepartureStation(destinationStation);
                            mDataSalePd.setDestinationStation(departureStation);
                        }
                    } else {
                        // Если родительский билет только Туда, то устанавливаем тариф, как раньше
                        setTariff(tariffsThereAndBack, TicketWayType.OneWay);
                    }
                    break;
                }

                mUiThread.post(() -> {
                    view.setDepartureStationName(mDataSalePd.getDepartureStation().getName());
                    view.setDestinationStationName(mDataSalePd.getDestinationStation().getName());
                });

                // Сбрасываем льготу
                setExemption(null);
                // Разрешаем менять станции местами только если есть оба тарифа
                mUiThread.post(() -> view.setSwapStationsBtnEnabled(mDataSalePd.getTariffBack() != null && mDataSalePd.getTariffThere() != null));
                // Обновляем сбор и цену
                mUiThread.post(() -> {
                    updateFee(true);
                    updatePrice();
                    view.setCostGroupVisible(true);
                });
            }
        } else {
            if (tariffForDepartureStationExists) {
                mDataSalePd.setDepartureStation(departureStation);
                mUiThread.post(() -> view.setDepartureStationName(departureStation.getName()));
            }
            if (tariffForDestinationStationExists) {
                mDataSalePd.setDestinationStation(destinationStation);
                mUiThread.post(() -> view.setDestinationStationName(destinationStation.getName()));
            }
            // Настраиваем интерфейс
            mUiThread.post(() -> view.setCostGroupVisible(false));
        }
        // Обновляем станции в выпадающих списках
        updateDepartureStations("");
        updateDestinationStations("");
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    void onSellBtnClick() {
        Logger.trace(TAG, "onSellBtnClick");
        // true если у парента есть льгота, а у текущей продажи - нет
        boolean nonExemptionPaymentForExemptionPd = mExtraPaymentParams.getParentPdExemptionExpressCode() != 0 && mDataSalePd.getExemption() == null;

        if (nonExemptionPaymentForExemptionPd) {
            view.setNonExemptionPaymentForExemptionPdAttentionDialogVisible(true);
        } else {
            interactionListener.onSellBtnClick();
        }
    }

    void onNonExemptionPaymentForExemptionPdAttentionDialogDismiss() {
        view.setNonExemptionPaymentForExemptionPdAttentionDialogVisible(false);
    }

    void onSendETicketBtnClick() {
        Logger.trace(TAG, "onSendETicketBtnClick");
        interactionListener.onSendETicketBtnClick(mDataSalePd.getETicketDataParams());
    }

    void onTariffPlanSelected(int position) {
        Logger.trace(TAG, "onTariffPlanSelected, position = " + position);
        // Устанавливаем выбранный тарифный план
        TariffPlan tariffPlan = mTariffPlans.get(position);
        setTariffPlanByPosition(position);
        // Сбрасываем льготу
        setExemption(null);
        // Находим тариф "Туда" и парный ему тариф "Обратно"
        Pair<Tariff, Tariff> tariffsThereAndBack = findTariffs(tariffPlan, mDataSalePd.getDepartureStation(), mDataSalePd.getDestinationStation());
        // http://agile.srvdev.ru/browse/CPPKPP-30337
        // Заключение по ответам Рзянкиной Натальи: Не беспокоимся, что тарифа для другого тарифного плана в это направлении может не быть.
        setTariff(tariffsThereAndBack, mDataSalePd.getDirection());
        // Обновляем сбор и цену
        mUiThread.post(() -> {
            updateFee(true);
            updatePrice();
            view.setCostGroupVisible(true);
        });
    }

    private List<TariffPlan> findTariffPlans(Station departureStation, Station destinationStation) {
        if (departureStation != null && destinationStation != null) {
            // Грузим варианты только если выбраны обе станции
            return pdSaleEnv.tariffPlansLoader().loadDirectTariffPlans((long) departureStation.getCode(), (long) destinationStation.getCode());
        } else {
            // Станция не выбрана, чистим категории поездов
            return Collections.emptyList();
        }
    }

    private Pair<Tariff, Tariff> findTariffs(TariffPlan tariffPlan, Station departureStation, Station destinationStation) {
        if (tariffPlan != null && departureStation != null && destinationStation != null) {
            Pair<List<TariffsChain>, List<TariffsChain>> foundTariffs = null;

            if (pdSaleEnv.pdSaleRestrictions().isDirectTariffAllowed((long) departureStation.getCode(), (long) destinationStation.getCode())) {
                foundTariffs = pdSaleEnv.tariffsLoader().loadDirectTariffsThereAndBack(
                        (long) departureStation.getCode(),
                        (long) destinationStation.getCode(),
                        (long) tariffPlan.getCode(),
                        (long) mDataSalePd.getTicketType().getCode()
                );
            }

            if (foundTariffs == null || (foundTariffs.first.isEmpty() && foundTariffs.second.isEmpty())) {
                onUnhandledErrorOccurred("Tariffs not found", departureStation, destinationStation);
                return null;
            }

            if (!foundTariffs.first.isEmpty()) {
                if (foundTariffs.first.size() != 1) {
                    onUnhandledErrorOccurred("Tariff there not unique", departureStation, destinationStation);
                    return null;
                }

            }

            if (!foundTariffs.second.isEmpty()) {
                if (foundTariffs.second.size() != 1) {
                    onUnhandledErrorOccurred("Tariff back not unique", departureStation, destinationStation);
                    return null;
                }
            }

            return new Pair<>(foundTariffs.first.get(0).getTariffs().get(0),
                    foundTariffs.second.isEmpty() ? null : foundTariffs.second.get(0).getTariffs().get(0));
        } else {
            return null;
        }
    }

    void onPaymentTypeChecked(int checkedId) {
        if (checkedId == R.id.paymentTypeCash) {
            mDataSalePd.setPaymentType(PaymentType.INDIVIDUAL_CASH);
        } else if (checkedId == R.id.paymentTypeCard) {
            mDataSalePd.setPaymentType(PaymentType.INDIVIDUAL_BANK_CARD);
        }
        view.setPaymentType(mDataSalePd.getPaymentType());
    }

    void onDepartureStationSelected(int position) {
        Logger.trace(TAG, "onDepartureStationSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    mUiThread.post(() -> view.showProgressDialog());
                    // Устнавливаем выбранную станцию
                    mDataSalePd.setDepartureStation(mDepartureStations.get(position));
                    mUiThread.post(() -> view.setDepartureStationName(mDataSalePd.getDepartureStation().getName()));
                    // Вернем полный список станций отправления
                    updateDepartureStations("");
                    // Очищаем кешированный список станций наначения
                    mDestinationStationsWithoutFilter = null;
                    // Обновляем список станций наначения
                    updateDestinationStations("");
                    // Сбрасываем льготу
                    setExemption(null);
                    // Обновляем список тарифных планов
                    List<TariffPlan> tariffPlans = findTariffPlans(mDataSalePd.getDepartureStation(), mDataSalePd.getDestinationStation());
                    setTariffPlans(tariffPlans);
                    setTariffPlanByPosition(tariffPlans.isEmpty() ? -1 : 0);
                    // Ищем тариф
                    Pair<Tariff, Tariff> tariffsThereAndBack = findTariffs(mDataSalePd.getTariffPlan(), mDataSalePd.getDepartureStation(), mDataSalePd.getDestinationStation());
                    setTariff(tariffsThereAndBack, TicketWayType.OneWay);
                    if (mDataSalePd.getTariffThere() != null) {
                        // Разрешаем менять станции местами только если есть оба тарифа
                        mUiThread.post(() -> view.setSwapStationsBtnEnabled(mDataSalePd.getTariffBack() != null && mDataSalePd.getTariffThere() != null));
                        // Обновляем сбор и цену
                        mUiThread.post(() -> {
                            updateFee(true);
                            updatePrice();
                            view.setCostGroupVisible(true);
                        });
                    } else {
                        // Настраиваем интерфейс
                        mUiThread.post(() -> {
                            view.setSwapStationsBtnEnabled(false);
                            view.setCostGroupVisible(false);
                        });
                    }
                    mUiThread.post(() -> view.hideProgressDialog());
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onDestinationStationSelected(int position) {
        Logger.trace(TAG, "onDestinationStationSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    mUiThread.post(() -> view.showProgressDialog());
                    // Устнавливаем выбранную станцию
                    mDataSalePd.setDestinationStation(mDestinationStations.get(position));
                    mUiThread.post(() -> view.setDestinationStationName(mDataSalePd.getDestinationStation().getName()));
                    // Вернем полный список станций наначения
                    updateDestinationStations("");
                    // Очищаем кешированный список станций отправления
                    mDepartureStationsWithoutFilter = null;
                    // Обновляем список станций отправления
                    updateDepartureStations("");
                    // Сбрасываем льготу
                    setExemption(null);
                    // Обновляем список тарифных планов
                    List<TariffPlan> tariffPlans = findTariffPlans(mDataSalePd.getDepartureStation(), mDataSalePd.getDestinationStation());
                    setTariffPlans(tariffPlans);
                    setTariffPlanByPosition(tariffPlans.isEmpty() ? -1 : 0);
                    // Ищем тариф
                    Pair<Tariff, Tariff> tariffsThereAndBack = findTariffs(mDataSalePd.getTariffPlan(), mDataSalePd.getDepartureStation(), mDataSalePd.getDestinationStation());
                    setTariff(tariffsThereAndBack, TicketWayType.OneWay);
                    if (mDataSalePd.getTariffThere() != null) {
                        // Разрешаем менять станции местами только если есть оба тарифа
                        mUiThread.post(() -> view.setSwapStationsBtnEnabled(mDataSalePd.getTariffBack() != null && mDataSalePd.getTariffThere() != null));
                        // Обновляем сбор и цену
                        mUiThread.post(() -> {
                            updateFee(true);
                            updatePrice();
                            view.setCostGroupVisible(true);
                        });
                    } else {
                        // Настраиваем интерфейс
                        mUiThread.post(() -> {
                            view.setSwapStationsBtnEnabled(false);
                            view.setCostGroupVisible(false);
                        });
                    }
                    mUiThread.post(() -> view.hideProgressDialog());
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    List<Station> onDepartureStationTextChanged(@NonNull String text) {
        Logger.trace(TAG, "onDepartureStationTextChanged, text = " + text);
        updateDepartureStations(text);
        return mDepartureStations;
    }

    List<Station> onDestinationStationTextChanged(@NonNull String text) {
        Logger.trace(TAG, "onDestinationStationTextChanged, text = " + text);
        updateDestinationStations(text);
        return mDestinationStations;
    }

    void onFeeCheckedChanged(boolean isChecked) {
        Logger.trace(TAG, "onFeeCheckedChanged, isChecked = " + isChecked);
        if (mDataSalePd.isIncludeFee() != isChecked) {
            view.setFeeChecked(isChecked);
            mDataSalePd.setIncludeFee(isChecked);
            updatePrice();
        }
    }

    void onExemptionClicked() {
        Logger.trace(TAG, "onExemptionClicked");
        if (mDataSalePd.getExemptionForEvent() == null) {

            //http://agile.srvdev.ru/browse/CPPKPP-33826
            final Calendar pdEndTime = Calendar.getInstance();
            pdEndTime.setTime(new Date(System.currentTimeMillis()));
            pdEndTime.set(Calendar.HOUR_OF_DAY, 23);
            pdEndTime.set(Calendar.MINUTE, 59);
            pdEndTime.set(Calendar.SECOND, 59);
            pdEndTime.set(Calendar.MILLISECOND, 999);

            // Запускаем сценарий выбора льготы
            SelectExemptionParams selectExemptionParams = new SelectExemptionParams();
            selectExemptionParams.setPdStartDateTime(new Date());
            selectExemptionParams.setPdEndDateTime(pdEndTime.getTime());
            selectExemptionParams.setTariffPlanCode(mDataSalePd.getTariffPlan().getCode());
            selectExemptionParams.setTicketTypeCode(mDataSalePd.getTicketType().getCode());
            selectExemptionParams.setTrainCategory(mDataSalePd.getTrainCategory());
            selectExemptionParams.setRegionCodes(Collections.singletonList(mDataSalePd.getDepartureStation().getRegionCode()));
            selectExemptionParams.setTicketCategoryCode((int) TicketCategory.Code.SINGLE);
            selectExemptionParams.setAllowReadFromBsc(true);
            selectExemptionParams.setExceptedExpressCode(mExtraPaymentParams.getParentPdExemptionExpressCode());
            selectExemptionParams.setExceptedSmartCardId(mExtraPaymentParams.getSmartCardId());
            selectExemptionParams.setVersionNsi(mNsiVersion);
            selectExemptionParams.setTimeStamp(new Date());
            selectExemptionParams.setParentPdTicketCategoryCode(mParentTariff.getTicketType(mNsiDaoSession).getTicketCategoryCode());
            interactionListener.navigateToSelectExemption(selectExemptionParams);
        } else {
            // Запускаем сценарий удаления льготы
            ExemptionGroup exemptionGroup = mDataSalePd.getExemption().getExemptionGroup(exemptionGroupRepository, nsiVersionManager.getCurrentNsiVersionId());
            RemoveExemptionParams removeExemptionParams = new RemoveExemptionParams();
            removeExemptionParams.setExpressCode(mDataSalePd.getExemptionForEvent().getExpressCode());
            removeExemptionParams.setGroupName(exemptionGroup == null ? null : exemptionGroup.getGroupName());
            removeExemptionParams.setPercentage(mDataSalePd.getExemption().getPercentage());
            removeExemptionParams.setFio(mDataSalePd.getExemptionForEvent().getFio());
            removeExemptionParams.setDocumentNumber(mDataSalePd.getExemptionForEvent().getNumberOfDocumentWhichApproveExemption());
            removeExemptionParams.setRequireSnilsNumber(mDataSalePd.getExemption().isRequireSnilsNumber());
            SmartCard smartCard = mDataSalePd.getExemptionForEvent().getSmartCardFromWhichWasReadAboutExemption();
            if (smartCard != null) {
                removeExemptionParams.setBscNumber(smartCard.getOuterNumber());
                removeExemptionParams.setBscType(smartCard.getType().getAbbreviation());
            }
            interactionListener.navigateToRemoveExemption(removeExemptionParams);
        }
    }

    void onExemptionSelected(@NonNull SelectExemptionResult selectExemptionResult) {
        setExemption(selectExemptionResult.getExemptionsForEvent().get(0));
        // Обновляем сбор и цену
        updateFee(true);
        updatePrice();
    }

    void onExemptionRemoved() {
        setExemption(null);
        // Обновляем сбор и цену
        updateFee(true);
        updatePrice();
    }

    void onETicketDataSelected(ETicketDataParams eTicketDataParams) {
        mDataSalePd.setETicketDataParams(eTicketDataParams);
    }

    void onClearStationsBtnClick() {
        Completable
                .fromAction(() -> {
                    mUiThread.post(view::showProgressDialog);
                    // Сбрасываем выбранные станции
                    mDataSalePd.setDepartureStation(null);
                    mDataSalePd.setDestinationStation(null);
                    // Сбрасываем тарифный план и тарифы
                    setTariffPlans(Collections.emptyList());
                    setTariffPlanByPosition(-1);
                    setTariff(null, TicketWayType.OneWay);
                    // Запрещаем менять станции местами
                    mUiThread.post(() -> view.setSwapStationsBtnEnabled(false));
                    // Очищаем кешированные списки станций
                    mDepartureStationsWithoutFilter = null;
                    mDestinationStationsWithoutFilter = null;
                    // Обновляем станции в выпадающих списках
                    updateDepartureStations("");
                    updateDestinationStations("");
                    mUiThread.post(() -> {
                        // Обновляем текст в полях ввода
                        view.setDepartureStationName("");
                        view.setDestinationStationName("");
                        // Настраиваем интерфейс
                        view.setCostGroupVisible(false);
                    });
                    mUiThread.post(view::hideProgressDialog);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    private void updateDepartureStations(@NonNull String filter) {

        final String likeQuery = filter.toUpperCase(Locale.getDefault());

        if ("".equals(filter) && mDepartureStationsWithoutFilter != null) {
            // Используем кешированный список
            mDepartureStations = mDepartureStationsWithoutFilter;
        } else {
            mDepartureStations = pdSaleEnv.depStationsLoader().loadAllStations(
                    null,
                    mDataSalePd.getDestinationStation() == null ? null : (long) mDataSalePd.getDestinationStation().getCode(),
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
                    mDataSalePd.getDepartureStation() == null ? null : (long) mDataSalePd.getDepartureStation().getCode(),
                    null,
                    likeQuery);
            if ("".equals(filter)) {
                // Кешируем список станций
                mDestinationStationsWithoutFilter = mDestinationStations;
            }
        }
        mUiThread.post(() -> view.setDestinationStations(mDestinationStations));
    }

    /**
     * Меняет местами стацнии отправления и назначения
     */
    private void swapStations() {
        if (mDataSalePd.getTariffBack() == null) {
            throw new IllegalStateException("Swapping fields is not allowed");
        }

        //http://agile.srvdev.ru/browse/CPPKPP-34069
        //направление для доплаты не меняем

        // Swap
        Station tempStation = mDataSalePd.getDepartureStation();
        mDataSalePd.setDepartureStation(mDataSalePd.getDestinationStation());
        mDataSalePd.setDestinationStation(tempStation);
        Tariff tempTariff = mDataSalePd.getTariffThere();
        mDataSalePd.setTariffThere(mDataSalePd.getTariffBack());
        mDataSalePd.setTariffBack(tempTariff);
    }

    private void setExemption(ExemptionForEvent exemptionForEvent) {
        if (exemptionForEvent != null) {
            Exemption exemption = exemptionRepository.getExemption(exemptionForEvent.getCode(), exemptionForEvent.getActiveFromDate(), exemptionForEvent.getVersionId());
            if (exemption == null) {
                throw new IllegalStateException("Exemption is null");
            }
            mDataSalePd.setExemptionForEvent(exemptionForEvent);
            AdditionalInfoForEtt additionalInfoForEtt = new AdditionalInfoForEtt();
            additionalInfoForEtt.setIssueDateTime(exemptionForEvent.getIssueDate());
            mDataSalePd.setAdditionalInfoForEttManualEntryDateIssue(additionalInfoForEtt);
            mDataSalePd.setExemption(exemption);
            mUiThread.post(() -> {
                // Отображаем информацию о льготе
                view.setExemptionValue(
                        mDataSalePd.getExemption().getExemptionExpressCode(),
                        mDataSalePd.getExemption().getPercentage()
                );
            });
        } else {
            mDataSalePd.setExemptionForEvent(null);
            mDataSalePd.setAdditionalInfoForEttManualEntryDateIssue(null);
            mDataSalePd.setExemption(null);
            mUiThread.post(() -> {
                // Отобажаем отсутствие льготы
                view.setExemptionValue(0, 0);
            });
        }
    }

    void onSwapStationsBtnClick() {
        Completable
                .fromAction(() -> {
                    mUiThread.post(view::showProgressDialog);
                    swapStations();
                    // Очищаем кешированные списки станций
                    mDepartureStationsWithoutFilter = null;
                    mDestinationStationsWithoutFilter = null;
                    // Обновляем станции в выпадающих списках
                    updateDepartureStations("");
                    updateDestinationStations("");
                    // Сбрасываем льготу
                    setExemption(null);
                    mUiThread.post(() -> {
                        // Обновляем текст в полях ввода
                        view.setDepartureStationName(mDataSalePd.getDepartureStation().getName());
                        view.setDestinationStationName(mDataSalePd.getDestinationStation().getName());
                        // Обновляем сбор и цену
                        updateFee(true);
                        updatePrice();
                    });
                    mUiThread.post(view::hideProgressDialog);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onDepartureStationEditCanceled() {
        Station station = mDataSalePd.getDepartureStation();
        view.setDepartureStationName(station == null ? "" : station.getName());
        // Вернем полный список станций
        // За счет кешированного списка без фильтра можем позолить себе сделать операцию в UI
        updateDepartureStations("");
    }

    void onDestinationStationEditCanceled() {
        Station station = mDataSalePd.getDestinationStation();
        view.setDestinationStationName(station == null ? "" : station.getName());
        // Вернем полный список станций
        // За счет кешированного списка без фильтра можем позолить себе сделать операцию в UI
        updateDestinationStations("");
    }

    private boolean isChangeTakeFeeAllowed() {
        // Зафигачим всякие условия
        boolean isWorkModeDisableTakeFee = mPrivateSettings.isMobileCashRegister() && !mPrivateSettings.isOutputMode(); //режим работы блокирует возможность брать сбор
        boolean isExistFee = mDataSalePd.getProcessingFee() != null; //существует ли вообще сбор как таковой

        // Разрешаем вручную ставить/убирать галку, если сбор существует и не блокируется режимом работы
        return !isWorkModeDisableTakeFee && isExistFee;
    }

    private boolean isTakeFeeEnabledByDefault() {
        // Зафигачим всякие условия
        boolean isOutputModeState = mPrivateSettings.isMobileCashRegister() && mPrivateSettings.isOutputMode(); //нажодимся ли мы в режиме работы мобильной кассы на выход
        boolean isWorkModeDisableTakeFee = mPrivateSettings.isMobileCashRegister() && !mPrivateSettings.isOutputMode(); //режим работы блокирует возможность брать сбор
        boolean isExistFee = mDataSalePd.getProcessingFee() != null; //существует ли вообще сбор как таковой
        boolean isExemptionExist = mDataSalePd.getExemption() != null; //существует ли льгота
        boolean isEnabledTakeFeeForExemption = isExemptionExist && mDataSalePd.getExemption().isTakeProcessingFee(); //стоит ли флаг брать сбор для льготы
        boolean isExemptionDisabledTakeFee = isExemptionExist && !isEnabledTakeFeeForExemption; //данная льгота запрещает брать сбор
        boolean isExemptionDisabledTakeFeeConsideringWorkMode = isExemptionDisabledTakeFee && !isOutputModeState; //проверка на запрет брать сбор с учетом льготы и режима работы ПТК
        boolean isDepartureStationCanSaleTickets = mDataSalePd.getDepartureStation().isCanSaleTickets(); //есть ли БПА на станции отправления

        // В режиме мобильной кассы на выход сбор для доплаты по умолчанию не берем!!! https://aj.srvdev.ru/browse/CPPKPP-31739
        // Берем сбор, если он:
        // - не блокируется режимом работы,
        // - существует,
        // - не блокируется настройками льготы, принимая во внимание режим работы
        return !isOutputModeState && !isWorkModeDisableTakeFee && isExistFee && !isExemptionDisabledTakeFeeConsideringWorkMode && isDepartureStationCanSaleTickets;
    }

    private void updateFee(boolean shouldResetFee) {
        boolean isTakeFee = isTakeFeeEnabledByDefault();
        boolean isEnabledCheckBox = isChangeTakeFeeAllowed();
        // Настроим чекбокс, чтобы при обновлении вьюхи не сбрасывался уже взведенный чекбокс
        if (shouldResetFee) {
            mDataSalePd.setIncludeFee(isTakeFee);
        } else {
            mDataSalePd.setIncludeFee(isEnabledCheckBox ? mDataSalePd.isIncludeFee() : isTakeFee);
        }
        view.setFeeChecked(mDataSalePd.isIncludeFee());
        view.setFeeEnabled(isEnabledCheckBox);
        // Если есть вероятность что льготу будем брать, то подпишем стоимость
        if (isTakeFee || isEnabledCheckBox) {
            view.setFeeValue(mDataSalePd.getProcessingFee().getTariff());
        } else {
            view.setFeeValue(null);
        }
    }

    /**
     * Производит обновление цен на экране
     */
    private void updatePrice() {

        BigDecimal price = mDataSalePd.getTariffThere().getPricePd();
        Logger.trace(TAG, "updatePrice, tariffPrice = " + price.toPlainString());
        mDataSalePd.setTicketCostValueWithoutDiscount(price);

        if (mDataSalePd.getExemptionForEvent() != null && mDataSalePd.getExemption() != null) {
            Logger.trace(TAG, "updatePrice, loss sum = " + mDataSalePd.getTicketCostLossSum().toPlainString());
            mDataSalePd.getExemptionForEvent().setLossSumm(mDataSalePd.getTicketCostLossSum());
        }

        if (!mDataSalePd.isMustTakeMoney()) {
            mDataSalePd.setPaymentType(PaymentType.INDIVIDUAL_CASH);
            view.setPaymentType(mDataSalePd.getPaymentType());
            view.setPaymentTypeEnabled(false);
        } else {
            view.setPaymentTypeEnabled(true);
        }

        BigDecimal ticketCostValueWithoutDiscount = mDataSalePd.getTicketCostValueWithoutDiscount();
        Logger.trace(TAG, "updatePrice, ticketCostValueWithoutDiscount = " + ticketCostValueWithoutDiscount.toPlainString());
        view.setOpePdCost(ticketCostValueWithoutDiscount);

        BigDecimal totalCostValueWithDiscount = mDataSalePd.getTotalCostValueWithDiscount();
        Logger.trace(TAG, "updatePrice, totalCostValueWithDiscount = " + totalCostValueWithDiscount.toPlainString());
        view.setTotalCost(totalCostValueWithDiscount);
    }

    void onCriticalNsiBackDialogRead() {
        interactionListener.navigateBack();
    }

    void onCriticalNsiCloseShiftDialogRead() {
        interactionListener.navigateToCloseShiftActivity();
    }

    private void onUnhandledErrorOccurred(@NonNull String message, @Nullable Station departureStation, @Nullable Station destinationStation) {
        Logger.info(TAG + "|ATTENTION, UNHANDLED ERROR OCCURRED|",
                message + "|" + (departureStation == null ? null : departureStation.getName()) + "|" + (destinationStation == null ? null : destinationStation.getName()));
        view.setUnhandledErrorOccurredDialogVisible(true);
        Completable
                .fromAction(() -> {
                    // Сбросим станцию отправления
                    mDataSalePd.setDepartureStation(null);
                    // Сбросим станцию назначения
                    mDataSalePd.setDestinationStation(null);
                    // Вернем полный список станций отправления
                    mDepartureStationsWithoutFilter = null;
                    updateDepartureStations("");
                    // Вернем полный список станций наначения
                    mDestinationStationsWithoutFilter = null;
                    updateDestinationStations("");
                    // Сбросим список тарифных планов
                    setTariffPlans(Collections.emptyList());
                    setTariffPlanByPosition(-1);
                    // Сбросим тариф
                    setTariff(null, TicketWayType.OneWay);
                    // Настраиваем интерфейс
                    mUiThread.post(() -> {
                        view.setCostGroupVisible(false);
                        // Обновляем текст в полях ввода
                        view.setDepartureStationName("");
                        view.setDestinationStationName("");
                        // Запрещаем менять станции местами
                        view.setSwapStationsBtnEnabled(false);
                    });
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onUnhandledErrorOccurredDialogDismiss() {
        view.setUnhandledErrorOccurredDialogVisible(false);
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void onSellBtnClick();

        void onSendETicketBtnClick(ETicketDataParams eTicketDataParams);

        void navigateToSelectExemption(SelectExemptionParams selectExemptionParams);

        void navigateToRemoveExemption(RemoveExemptionParams removeExemptionParams);

        void navigateToCloseShiftActivity();

        void navigateBack();
    }

}
