package ru.ppr.cppk.ui.activity.transfersale.preparation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.TicketTypeChecker;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.PtkModeChecker;
import ru.ppr.cppk.logic.TicketStorageTypeToTicketTypeChecker;
import ru.ppr.cppk.logic.base.PdCostCalculator;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.logic.pdSale.PdSaleEnv;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParams;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.logic.utils.DateUtils;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.model.TariffsChain;
import ru.ppr.cppk.ui.activity.transfersale.interactor.TransferPdCostCalculator;
import ru.ppr.cppk.ui.activity.transfersale.model.TransferSaleData;
import ru.ppr.cppk.ui.activity.transfersale.model.TransferSaleParams;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.FeeType;
import ru.ppr.nsi.entity.ProcessingFee;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;
import rx.Completable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Dmitry Nevolin
 */
public class PreparationPresenter extends BaseMvpViewStatePresenter<PreparationView, PreparationViewState> {

    private static final String TAG = Logger.makeLogTag(PreparationPresenter.class);

    private static final byte[] BYTES_FOR_CHECK_SIGN = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};

    private final NsiVersionManager nsiVersionManager;
    private final NsiDaoSession nsiDaoSession;
    private final TariffRepository tariffRepository;
    private final StationRepository stationRepository;
    private final TariffPlanRepository tariffPlanRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UiThread uiThread;
    private final PtkModeChecker ptkModeChecker;
    private final PrivateSettings privateSettings;
    private final EdsManagerWrapper edsManager;
    private final PdCostCalculator pdCostCalculator;
    private final PdValidityPeriodCalculator pdValidityPeriodCalculator;
    private final TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker;
    private final PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder;
    private final PdSaleEnv pdSaleEnv;
    private final TransferSaleData transferSaleData;
    private final TransferSaleParams transferSaleParams;
    private final TicketTypeChecker ticketTypeChecker;

    // Other
    private InteractionListener interactionListener;
    private Subscription checkSignSubscription = null;
    /**
     * Список типов билетов для выбранных станций
     */
    private List<TicketType> ticketTypes = Collections.emptyList();

    @Inject
    PreparationPresenter(@NonNull PreparationViewState transferSalePreparationViewState,
                         @NonNull NsiVersionManager nsiVersionManager,
                         @NonNull NsiDaoSession nsiDaoSession,
                         @NonNull TariffRepository tariffRepository,
                         @NonNull StationRepository stationRepository,
                         @NonNull TariffPlanRepository tariffPlanRepository,
                         @NonNull TicketTypeRepository ticketTypeRepository,
                         @NonNull UiThread uiThread,
                         @NonNull PtkModeChecker ptkModeChecker,
                         @NonNull PrivateSettings privateSettings,
                         @NonNull EdsManagerWrapper edsManager,
                         @NonNull TransferPdCostCalculator pdCostCalculator,
                         @NonNull PdValidityPeriodCalculator pdValidityPeriodCalculator,
                         @NonNull TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker,
                         @NonNull PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder,
                         @NonNull PdSaleEnvFactory pdSaleEnvFactory,
                         @NonNull TransferSaleData transferSaleData,
                         @NonNull TransferSaleParams transferSaleParams,
                         @NonNull TicketTypeChecker ticketTypeChecker) {
        super(transferSalePreparationViewState);
        this.nsiVersionManager = nsiVersionManager;
        this.nsiDaoSession = nsiDaoSession;
        this.tariffRepository = tariffRepository;
        this.stationRepository = stationRepository;
        this.tariffPlanRepository = tariffPlanRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.uiThread = uiThread;
        this.ptkModeChecker = ptkModeChecker;
        this.privateSettings = privateSettings;
        this.edsManager = edsManager;
        this.pdCostCalculator = pdCostCalculator;
        this.pdValidityPeriodCalculator = pdValidityPeriodCalculator;
        this.ticketStorageTypeToTicketTypeChecker = ticketStorageTypeToTicketTypeChecker;
        this.pdSaleRestrictionsParamsBuilder = pdSaleRestrictionsParamsBuilder;
        this.pdSaleEnv = pdSaleEnvFactory.newPdSaleEnvForTransfer();
        this.transferSaleData = transferSaleData;
        this.transferSaleParams = transferSaleParams;
        this.ticketTypeChecker = ticketTypeChecker;
    }

    void setInteractionListener(InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    @Override
    protected void onInitialize2() {
        Completable
                .fromAction(() -> {
                    uiThread.post(view::showLoadingDialog);

                    Logger.trace(TAG, "transferSaleParams: " + transferSaleParams);

                    if (transferSaleParams.isWithParentPd()) {
                        // Есть информация о родительском ПД
                        // Заполняем информацию о связи с ПД на поезд
                        fillParentTicketInfo();
                        // Отображаем в UI информацию о родительском ПД
                        showParentPdInfo();
                    }

                    PdSaleRestrictionsParams pdSaleRestrictionsParams = pdSaleRestrictionsParamsBuilder.createForTransfer(transferSaleParams.getTimestamp(), transferSaleParams.getNsiVersion());
                    if (transferSaleParams.isWithParentPd()) {
                        PdSaleRestrictionsParams.TransferSaleData transferSaleData = pdSaleRestrictionsParams.getTransferSaleData();
                        transferSaleData.setWithParentPd(true);
                        transferSaleData.setParentPdSaleDateTime(transferSaleParams.getParentPdSaleDateTime());
                        transferSaleData.setParentPdStartDateTime(transferSaleParams.getParentPdStartDateTime());
                        transferSaleData.setParentPdTariffCode(transferSaleParams.getParentPdTariffCode());
                        transferSaleData.setParentPdDirection(transferSaleParams.getParentPdDirection());
                        pdSaleEnv.pdSaleRestrictions().update(pdSaleRestrictionsParams);
                    }
                    pdSaleEnv.pdSaleRestrictions().update(pdSaleRestrictionsParams);

                    // Получаем станцию отправления
                    Station depStation = stationRepository.load(transferSaleParams.getDepartureStationCode(), transferSaleParams.getNsiVersion());
                    transferSaleData.setDepartureStation(depStation);
                    // Получаем станцию назначения
                    Station destStation = stationRepository.load(transferSaleParams.getDestinationStationCode(), transferSaleParams.getNsiVersion());
                    transferSaleData.setDestinationStation(destStation);
                    // Получаем тарифный план
                    TariffPlan tariffPlan = findTariffPlan();
                    transferSaleData.setTariffPlan(tariffPlan);
                    // Обновляем информацию о сборе
                    updateProcessingFee();
                    // Обновляем список типов ПД
                    updateTicketTypes();
                    // Устанавливаем способ оплаты по умолчанию
                    transferSaleData.setPaymentType(PaymentType.INDIVIDUAL_CASH);
                    uiThread.post(() -> {
                        // Устанавливаем видимость кнопок типа оплаты
                        view.setPaymentTypeVisible(privateSettings.isPosEnabled());
                        // Отображаем станции маршрута
                        view.setStations(transferSaleData.getDepartureStation().getName(),
                                transferSaleData.getDestinationStation().getName());
                        // Скрываем прогресс
                        uiThread.post(view::hideLoadingDialog);
                    });
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    /**
     * Заполняет информацию о связи с ПД на поезд
     */
    private void fillParentTicketInfo() {
        // Заполняем ParentTicketInfo
        ParentTicketInfo parentTicketInfo = new ParentTicketInfo();
        parentTicketInfo.setSaleDateTime(transferSaleParams.getParentPdSaleDateTime());
        parentTicketInfo.setTicketNumber(transferSaleParams.getParentPdNumber());
        parentTicketInfo.setWayType(transferSaleParams.getParentPdDirection());
        parentTicketInfo.setCashRegisterNumber(transferSaleParams.getParentPdDeviceId());
        // Устанавливаем данные о родительском билете
        transferSaleData.setParentTicketInfo(parentTicketInfo);
        // Устанавливаем тип связи с родительским билетом
        transferSaleData.setConnectionType(ConnectionType.TRANSFER);
    }

    /**
     * Отображает в UI информацию о родительском ПД
     */
    private void showParentPdInfo() {
        // Ищем родительский тариф
        Tariff parentTariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                transferSaleParams.getParentPdTariffCode(),
                nsiVersionManager.getNsiVersionIdForDate(transferSaleParams.getParentPdSaleDateTime()));
        // Получаем тип родительского ПД
        TicketType parentTicketType = ticketTypeRepository.load(parentTariff.getTicketTypeCode(), parentTariff.getVersionId());
        // Заполняем ParentPdInfo
        PreparationView.ParentPdInfo parentPdInfo = new PreparationView.ParentPdInfo();
        parentPdInfo.ticketType = parentTicketType.toString();
        parentPdInfo.pdNumber = transferSaleParams.getParentPdNumber();
        parentPdInfo.departureStation = parentTariff.getStationDeparture(nsiDaoSession).getName();
        parentPdInfo.destinationStation = parentTariff.getStationDestination(nsiDaoSession).getName();
        parentPdInfo.trainCategory = parentTariff.getTariffPlan(tariffPlanRepository).getTrainCategory(nsiDaoSession).name;
        parentPdInfo.exemptionCode = transferSaleParams.getParentPdExemptionExpressCode();
        parentPdInfo.direction = transferSaleParams.getParentPdDirection();
        parentPdInfo.startDate = transferSaleParams.getParentPdStartDateTime();
        parentPdInfo.endDate = calculatePdEndDate(parentTicketType, transferSaleParams.getParentPdStartDateTime());
        // Отображаем информацию в UI
        uiThread.post(() -> view.setParentPdInfo(parentPdInfo));
    }

    /**
     * Выполнет поиск тарифного плана, по которому будет осуществляться оформление трансфера.
     * Кирдяпкин Михаил: при случайном обнаружении 2-ух тарифных планов брать первый попавшийся.
     * Задача метода - безопасно найти {@link TariffPlan}.
     * Т.е. для случая, когда есть 2 тарифа t1 (plan1, type1) и t2 (plan2, type2)
     * обеспечивает защиту от случайного выбора plan1, если извне пришло ограничение на type2
     */
    @Nullable
    private TariffPlan findTariffPlan() {
        TicketType ticketType = null;
        if (transferSaleParams.getTicketTypeCode() != null) {
            // Если есть ограничения на тип ПД извне, получаем TicketType
            ticketType = ticketTypeRepository.load((int) (long) transferSaleParams.getTicketTypeCode(), transferSaleParams.getNsiVersion());
        }

        // Находим тарифы для выбранных станций с учетом ticketType
        List<Tariff> tariffsThere = findTariffs(
                null,
                ticketType,
                transferSaleData.getDepartureStation(),
                transferSaleData.getDestinationStation()
        );

        TariffPlan tariffPlan = null;
        if (tariffsThere != null) {
            // Если тариф существует, берем из него тарифный план
            tariffPlan = tariffPlanRepository.load((int) (long) tariffsThere.get(0).getTariffPlanCode(), transferSaleParams.getNsiVersion());
        }

        Logger.trace(TAG, "findTariffPlan, res = " + (tariffPlan == null ? null : tariffPlan.getCode()));
        return tariffPlan;
    }

    private void updateTicketTypes() {
        Logger.trace(TAG, "updateTicketTypes");
        // Обновляем список типов ПД
        ticketTypes = findTicketTypes(
                transferSaleData.getTariffPlan(),
                transferSaleData.getDepartureStation(),
                transferSaleData.getDestinationStation()
        );
        uiThread.post(() -> view.setTicketTypes(ticketTypes));
        // Устанавливаем текущий тип ПД
        if (ticketTypes.isEmpty()) {
            Logger.trace(TAG, "updateTicketTypes - no ticketType");
            setTicketTypeByPosition(-1);
        } else {
            Logger.trace(TAG, "updateTicketTypes - first ticketType");
            setTicketTypeByPosition(0);
        }
        // Настраиваем возможность выбора типа ПД в UI
        uiThread.post(() -> view.setTicketTypeSelectionEnabled(transferSaleParams.getTicketTypeCode() == null));
        // Обновляем информацию о тарифах
        updateTariffs();
        // Обновляем сбор
        updateFee();
        // Обновляем цену
        updatePrice();
        // Обновляем состояние кнопок продажи
        updateSaleButtonsState();
    }

    @NonNull
    private List<TicketType> findTicketTypes(@Nullable TariffPlan tariffPlan,
                                             @Nullable Station departureStation,
                                             @Nullable Station destinationStation) {
        if (tariffPlan == null || departureStation == null || destinationStation == null) {
            // Ничего не ищем, если по какой-то причине не были заданы все поля
            return Collections.emptyList();
        }

        // Получаем список типов ПД
        List<TicketType> ticketTypes = pdSaleEnv.ticketTypesLoader().loadDirectTicketTypes(
                (long) departureStation.getCode(),
                (long) destinationStation.getCode(),
                (long) tariffPlan.getCode()
        );

        if (transferSaleParams.getTicketTypeCode() == null) {
            // Если нет ограничений на тип ПД извне,
            // возвращаем полный список
            return ticketTypes;
        }
        // Если есть ограничение на тип ПД извне,
        // оставляем только разрешенный вариант
        for (TicketType ticketType : ticketTypes) {
            if (ticketType.getCode() == transferSaleParams.getTicketTypeCode()) {
                return Collections.singletonList(ticketType);
            }
        }
        return Collections.emptyList();
    }

    private void setTicketTypeByPosition(int position) {
        if (position == -1) {
            transferSaleData.setTicketType(null);
        } else {
            transferSaleData.setTicketType(ticketTypes.get(position));
        }
        // Обновляем выбранный элемент списка в UI
        uiThread.post(() -> view.setSelectedTicketTypePosition(position));
        // Задаем направление
        setDirection();
        // Отображаем сроки действия трансфера
        updateTransferValidityDates();
    }

    private void updateTariffs() {
        Logger.trace(TAG, "updateTariffs");
        // Находим тариф "Туда" и парный ему тариф "Обратно"
        List<Tariff> tariffsThere;
        if (transferSaleData.getTariffPlan() == null
                || transferSaleData.getTicketType() == null
                || transferSaleData.getDepartureStation() == null
                || transferSaleData.getDestinationStation() == null) {
            // Ничего не ищем, если по какой-то причине не были заданы все поля
            tariffsThere = null;
        } else {
            tariffsThere = findTariffs(
                    transferSaleData.getTariffPlan(),
                    transferSaleData.getTicketType(),
                    transferSaleData.getDepartureStation(),
                    transferSaleData.getDestinationStation()
            );
        }

        transferSaleData.setTariffsThere(tariffsThere);
    }

    /**
     * Ищет тарифы туда
     *
     * @param tariffPlan         - тарифный план
     * @param ticketType
     * @param departureStation
     * @param destinationStation
     * @return - список тарифов туда
     */
    @Nullable
    private List<Tariff> findTariffs(@Nullable TariffPlan tariffPlan,
                                     @Nullable TicketType ticketType,
                                     @Nullable Station departureStation,
                                     @Nullable Station destinationStation) {
        Pair<List<TariffsChain>, List<TariffsChain>> foundTariffs = pdSaleEnv.tariffsLoader().loadDirectTariffsThereAndBack(
                departureStation == null ? null : (long) departureStation.getCode(),
                destinationStation == null ? null : (long) destinationStation.getCode(),
                tariffPlan == null ? null : (long) tariffPlan.getCode(),
                ticketType == null ? null : (long) ticketType.getCode()
        );
        if (!foundTariffs.first.isEmpty()) {
            // Есть прямой тариф
            return foundTariffs.first.get(0).getTariffs();
        } else {
            return null;
        }
    }

    void onTicketTypeSelected(int position) {
        Logger.trace(TAG, "onTicketTypeSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    uiThread.post(view::showLoadingDialog);
                    // Устанавливаем выбранный тип ПД
                    setTicketTypeByPosition(position);
                    // Обновляем информацию о тарифах
                    updateTariffs();
                    // Обновляем сбор
                    updateFee();
                    // Обновляем цену
                    updatePrice();
                    // Обновляем состояние кнопок продажи
                    updateSaleButtonsState();
                    uiThread.post(view::hideLoadingDialog);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onFeeCheckedChanged(boolean checked) {
        Logger.trace(TAG, "onFeeCheckedChanged, checked = " + checked);
        if (transferSaleData.isIncludeFee() != checked) {
            transferSaleData.setIncludeFee(checked);
            view.setFeeChecked(checked);
            updatePrice();
        }
    }

    void onPaymentTypeSelected(PaymentType paymentType) {
        Logger.trace(TAG, "onPaymentTypeSelected, paymentType = " + paymentType);
        transferSaleData.setPaymentType(paymentType);
        view.setPaymentType(paymentType);
    }

    void onWriteToCardBtnClicked() {
        Logger.trace(TAG, "onWriteToCardBtnClicked");
        callRunnableAfterCheckingSign(() -> interactionListener.writePd());
    }

    void onPrintPdBtnClicked() {
        Logger.trace(TAG, "onPrintPdBtnClicked, checked");
        callRunnableAfterCheckingSign(() -> interactionListener.printPd());
    }

    void onCheckEdsFailedDialogClosed() {
        // nop
    }

    private void updateTransferValidityDates() {
        Date transferStartDate;
        int startDayOffset;

        if (transferSaleParams.isWithParentPd() &&
                transferSaleParams.getParentPdStartDateTime().after(transferSaleParams.getTimestamp())) {
            // Если дата начала действия родительского ПД после текущей даты оформления,
            // считаем датой начала действия трансфера дату начала действия родительского ПД
            // http://agile.srvdev.ru/browse/CPPKPP-42672
            transferStartDate = transferSaleParams.getParentPdStartDateTime();
            long startOfCurrentDate = DateUtils.getStartOfDay(transferSaleParams.getTimestamp()).getTimeInMillis();
            long startOfParentPdDate = DateUtils.getStartOfDay(transferSaleParams.getParentPdStartDateTime()).getTimeInMillis();
            long startOffsetInMillis = startOfParentPdDate - startOfCurrentDate;
            Logger.trace(TAG, "startOffsetInMillis = " + startOffsetInMillis);
            startDayOffset = (int) TimeUnit.MILLISECONDS.toDays(startOffsetInMillis);
        } else {
            // ПД начинает действовать с момента продажи, term = 0
            transferStartDate = transferSaleParams.getTimestamp();
            startDayOffset = 0;
        }

        Logger.trace(TAG, "transferStartDate = " + transferStartDate);
        transferSaleData.setStartDate(transferStartDate);

        Logger.trace(TAG, "startDayOffset = " + startDayOffset);
        transferSaleData.setStartDayOffset(startDayOffset);

        if (transferSaleData.getTicketType() != null) {
            transferSaleData.setEndDate(calculatePdEndDate(transferSaleData.getTicketType(), transferStartDate));
        } else {
            //http://agile.srvdev.ru/browse/CPPKPP-38280
            transferSaleData.setEndDate(transferStartDate);
        }
        Logger.trace(TAG, "transferEndDate = " + transferSaleData.getEndDate());

        uiThread.post(() -> view.setTransferValidityDates(transferSaleData.getStartDate(), transferSaleData.getEndDate()));
    }

    /**
     * Задает направление по типу билета.
     * Для трансферов само поле не имеет никакого смысла,
     * но нужно для генерации события продажи
     */
    private void setDirection() {
        if (transferSaleData.getTicketType() != null) {
            if (ticketTypeChecker.isOneWayTransfer(transferSaleData.getTicketType().getCode())) {
                transferSaleData.setDirection(TicketWayType.OneWay);
            } else if (ticketTypeChecker.isTwoWayTransfer(transferSaleData.getTicketType().getCode())) {
                transferSaleData.setDirection(TicketWayType.TwoWay);
            } else {
                transferSaleData.setDirection(null);
            }
        } else {
            transferSaleData.setDirection(null);
        }
    }

    @NonNull
    private Date calculatePdEndDate(@NonNull TicketType ticketType, @NonNull Date pdStartDate) {
        // Определяем направление ПД
        TicketWayType wayType = ticketTypeChecker.isTwoWayTransfer(ticketType.getCode()) ? TicketWayType.TwoWay : TicketWayType.OneWay;
        // Определяем срок действия ПД в днях
        int validityPeriod = pdValidityPeriodCalculator.calcValidityPeriod(pdStartDate, wayType, ticketType, transferSaleParams.getNsiVersion());
        // Рассчитываем дату окончания действия ПД
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(pdStartDate);
        calendar.add(Calendar.DAY_OF_MONTH, validityPeriod - 1);
        return calendar.getTime();
    }

    /**
     * Метод для определения возможности оформления ПД на выбранный носитель.
     *
     * @return результат проверки.
     */
    private boolean canCurrentTicketTypeBeSoldOnTicketStorageType(TicketStorageType ticketStorageType) {
        if (transferSaleData.getTicketType() == null) return false;
        return ticketStorageTypeToTicketTypeChecker.check(ticketStorageType, transferSaleData.getTicketType());
    }

    private boolean isTakeFeeEnabledByDefault() {
        // "Сбор за оформление трансфера в поезде" применять, когда в ПТК ОТКЛЮЧЕН режим "Мобильная касса" и "Контроль трансфера в автобусе"
        // "Сбор за оформление трансфера в микроавтобусе" применять, когда ВКЛЮЧЕН режим "Контроль трансфера в автобусе"
        // Т.е. нам нужны режимы "нормальный" и "контроль трансфера в автобусе"
        boolean isWorkModeAllowsTakeFee = ptkModeChecker.isTrainControlMode() || ptkModeChecker.isTransferControlMode();
        boolean isExistFee = transferSaleData.getProcessingFee() != null; //существует ли вообще сбор как таковой
        // Наличие ККТ на станции отправления и/или назначения маршрута трансфера не должно проверяться при оформлении трансфера,
        // т.к. на всех остановочных пунктах из маршрута трансфера отсутствуют ККТ (по НСИ),
        // но при этом от заказчика получено требование, что при оформлении в поезде трансфера должен взиматься сбор.

        // Берем сбор, если:
        // - сбор не блокируется режимом работы,
        // - сбор существует
        return isWorkModeAllowsTakeFee && isExistFee;
    }

    private boolean isChangeTakeFeeAllowed() {
        // Разрешаем вручную ставить/убирать галку, если:
        // - сбор существует
        return transferSaleData.getProcessingFee() != null;
    }

    /**
     * Обновляет статус использования сбора
     */
    private void updateFee() {
        boolean isTakeFee = isTakeFeeEnabledByDefault();
        Logger.trace(TAG, "updateFee, isTakeFee = " + isTakeFee);
        boolean isEnabledCheckBox = isChangeTakeFeeAllowed();
        Logger.trace(TAG, "updateFee, isTakeFee = " + isEnabledCheckBox);
        transferSaleData.setIncludeFee(isTakeFee);

        uiThread.post(() -> {
            view.setFeeChecked(transferSaleData.isIncludeFee());
            view.setFeeEnabled(isEnabledCheckBox);

            if (isTakeFee || isEnabledCheckBox) {
                view.setFeeValue(transferSaleData.getProcessingFee().getTariff());
            } else {
                view.setFeeValue(null);
            }
        });
    }

    /**
     * Обновляет информацию о сборе
     */
    private void updateProcessingFee() {
        // Определяем тип сбора
        FeeType feeType = ptkModeChecker.isTransferControlMode() ? FeeType.TRANSFER_IN_BUS : FeeType.TRANSFER_IN_TRAIN;
        Logger.trace(TAG, "updateProcessingFee, feeType: " + feeType);
        // Отображаем тип сбора
        uiThread.post(() -> view.setFeeType(feeType));
        // Определяем сбор
        ProcessingFee processingFee = null;
        TariffPlan tariffPlan = transferSaleData.getTariffPlan();
        TrainCategory trainCategory = tariffPlan.getTrainCategory(nsiDaoSession);
        if (trainCategory != null) {
            processingFee = nsiDaoSession.getProcessingFeeDao().getProcessingFee(trainCategory.code, feeType, nsiVersionManager.getCurrentNsiVersionId());
        }
        Logger.trace(TAG, "updateProcessingFee, processingFee: " + processingFee);
        transferSaleData.setProcessingFee(processingFee);
    }

    /**
     * Обновляет цены на экране
     */
    private void updatePrice() {
        BigDecimal price = pdCostCalculator.getOneTicketCostValueWithoutDiscountForAllTariffs();
        Logger.trace(TAG, "updatePrice, tariffPrice = " + price.toPlainString());

        BigDecimal ticketCostValueWithoutDiscount = pdCostCalculator.getOneTicketCostValueWithoutDiscountForAllTariffs();
        Logger.trace(TAG, "updatePrice, ticketCostValueWithoutDiscount = " + ticketCostValueWithoutDiscount.toPlainString());

        BigDecimal totalCostValueWithDiscount = pdCostCalculator.getAllTicketsTotalCostValueWithDiscount();
        Logger.trace(TAG, "updatePrice, totalCostValueWithDiscount = " + totalCostValueWithDiscount.toPlainString());

        boolean zeroPrice = totalCostValueWithDiscount.compareTo(BigDecimal.ZERO) == 0;
        if (zeroPrice) {
            // Для безденежных ПД принудительно сбрасываем тип оплаты в наличные
            transferSaleData.setPaymentType(PaymentType.INDIVIDUAL_CASH);
        }

        uiThread.post(() -> {
            // Отображаем стоимость без учета скидки
            view.setTransferCost(ticketCostValueWithoutDiscount);
            // Отображаем общую стоимость
            view.setTotalCost(totalCostValueWithDiscount);
            // Настраиваем кнопки способа оплаты
            if (zeroPrice) {
                view.setPaymentType(transferSaleData.getPaymentType());
                view.setPaymentTypeEnabled(false);
            } else {
                view.setPaymentTypeEnabled(true);
            }
        });
    }

    /**
     * Обновляет состояние кнопок печати/записи на БСК
     */
    private void updateSaleButtonsState() {
        if (canCurrentTicketTypeBeSoldOnTicketStorageType(TicketStorageType.Paper)) {
            uiThread.post(() -> view.setSaleButtonsState(PreparationView.SaleButtonsState.WRITE_AND_PRINT));
        } else {
            uiThread.post(() -> view.setSaleButtonsState(PreparationView.SaleButtonsState.WRITE_ONLY));
        }
    }

    /**
     * Метод для проверки подписи.
     */
    private void callRunnableAfterCheckingSign(Runnable runnable) {
        if (checkSignSubscription != null) {
            return;
        }

        Logger.trace(TAG, "callRunnableAfterCheckingSign start");
        uiThread.post(view::showLoadingDialog);

        checkSignSubscription = Single
                .fromCallable(() -> {
                    SignDataResult signDataResult = edsManager.signBlocking(BYTES_FOR_CHECK_SIGN, new Date());
                    Logger.trace(TAG, "callRunnableAfterCheckingSign finish, signDataResult = " + signDataResult);
                    if (!signDataResult.isSuccessful()) {
                        throw new Exception("Could not to sign test data");
                    }
                    return signDataResult;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(SchedulersCPPK.background())
                .subscribe(new SingleSubscriber<SignDataResult>() {
                    @Override
                    public void onSuccess(SignDataResult signDataResult) {
                        checkSignSubscription = null;
                        uiThread.post(view::hideLoadingDialog);
                        runnable.run();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Logger.error(TAG, error);
                        checkSignSubscription = null;
                        uiThread.post(view::hideLoadingDialog);
                        view.setEdsFailedErrorDialogVisible(true);
                    }
                });
    }

    public interface InteractionListener {
        void writePd();

        void printPd();
    }
}
