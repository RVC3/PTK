package ru.ppr.cppk.ui.activity.transfersalestart;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.PtkModeChecker;
import ru.ppr.cppk.logic.pdSale.PdSaleEnv;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParams;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.model.TariffsChain;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.cppk.ui.activity.transfersale.model.TransferSaleParams;
import ru.ppr.cppk.ui.activity.transfersalestart.model.TransferSaleData;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.utils.CollectionUtils;
import rx.Completable;

/**
 * @author Dmitry Nevolin
 */
public class TransferSaleStartPresenter extends BaseMvpViewStatePresenter<TransferSaleStartView, TransferSaleStartViewState> {

    private static final String TAG = Logger.makeLogTag(TransferSaleStartPresenter.class);

    private final NsiVersionManager nsiVersionManager;
    private final StationRepository stationRepository;
    private final UiThread uiThread;
    private final PdSaleEnv pdSaleEnv;
    private final PtkModeChecker ptkModeChecker;
    private final PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder;
    private final PrivateSettings privateSettings;
    private final TransferSaleData transferSaleData;
    /**
     * Версия НСИ для оформления ПД
     */
    private int nsiVersion;
    /**
     * Дата начала процесса оформления ПД
     */
    private Date timestamp;
    /**
     * Стацнии отправления в выпадающем списке
     */
    private List<Station> departureStations = Collections.emptyList();
    /**
     * Станции назначения в выпадающем списке
     */
    private List<Station> destinationStations = Collections.emptyList();
    /**
     * Кешированный список стацний отправления в выпадающем списке без фильтра по введенным символам.
     * Используется для быстрого обновления выпадающего списка напрямую из UI-потока при отмене выбора станции.
     */
    private List<Station> departureStationsWithoutFilter = null;
    /**
     * Кешированный список стацний назначения в выпадающем списке без фильтра по введенным символам.
     * Используется для быстрого обновления выпадающего списка напрямую из UI-потока при отмене выбора станции.
     */
    private List<Station> destinationStationsWithoutFilter = null;
    /**
     * Список типов билетов для выбранных станций
     */
    private List<TicketType> ticketTypes = Collections.emptyList();

    // Other
    private InteractionListener interactionListener;

    @Inject
    TransferSaleStartPresenter(@NonNull TransferSaleStartViewState transferSalePreparationViewState,
                               @NonNull NsiVersionManager nsiVersionManager,
                               @NonNull StationRepository stationRepository,
                               @NonNull UiThread uiThread,
                               @NonNull PdSaleEnvFactory pdSaleEnvFactory,
                               @NonNull PtkModeChecker ptkModeChecker,
                               @NonNull PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder,
                               @NonNull PrivateSettings privateSettings,
                               @NonNull TransferSaleData transferSaleData) {
        super(transferSalePreparationViewState);
        this.nsiVersionManager = nsiVersionManager;
        this.stationRepository = stationRepository;
        this.uiThread = uiThread;
        this.pdSaleEnv = pdSaleEnvFactory.pdSaleEnvForTransfer();
        this.ptkModeChecker = ptkModeChecker;
        this.pdSaleRestrictionsParamsBuilder = pdSaleRestrictionsParamsBuilder;
        this.privateSettings = privateSettings;
        this.transferSaleData = transferSaleData;
    }

    void setInteractionListener(InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    @Override
    protected void onInitialize2() {
        Completable
                .fromAction(() -> {
                    uiThread.post(view::showLoadingDialog);

                    timestamp = new Date();
                    nsiVersion = nsiVersionManager.getCurrentNsiVersionId();

                    PdSaleRestrictionsParams pdSaleRestrictionsParams = pdSaleRestrictionsParamsBuilder.createForTransfer(timestamp, nsiVersion);
                    pdSaleEnv.pdSaleRestrictions().update(pdSaleRestrictionsParams);

                    // Устанавливаем список станций и выбранные станции
                    initializeStations();
                    uiThread.post(view::hideLoadingDialog);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    private void initializeStations() {
        Logger.trace(TAG, "initializeStations");
        // Обновляем станции в выпадающих списках
        updateDepartureStations("");
        updateDestinationStations("");
        if (departureStations.isEmpty() || destinationStations.isEmpty()) {
            Logger.trace(TAG, "initializeStations, no stations for transfer");
            // Если не удалось найти станций отправления/назначения,
            // значит, маршрутов нет
            view.showNoStationsForSaleError();
            return;
        }
        if (ptkModeChecker.isTransferControlMode()) {
            // Если ПТК в режиме контроля трансфера
            // Пробуем подставить станции назначения в поля ввода
            long[] ptkRouteStationCodes = privateSettings.getTransferRouteStationsCodes();
            Logger.trace(TAG, "routeDepStation code = " + ptkRouteStationCodes[0]);
            Logger.trace(TAG, "routeDestStation code = " + ptkRouteStationCodes[1]);
            Station routeDepStation = stationRepository.load(ptkRouteStationCodes[0], nsiVersion);
            Station routeDestStation = stationRepository.load(ptkRouteStationCodes[1], nsiVersion);

            List<TariffPlan> tariffPlansThere = findTariffPlans(routeDepStation, routeDestStation);
            Logger.trace(TAG, "tariffPlanThere.size = " + tariffPlansThere.size());
            if (tariffPlansThere.isEmpty()) {
                List<TariffPlan> tariffPlansBack = findTariffPlans(routeDestStation, routeDepStation);
                Logger.trace(TAG, "tariffPlanBack.size = " + tariffPlansBack.size());
                if (!tariffPlansBack.isEmpty()) {
                    setDepartureStation(routeDestStation);
                    setDestinationStation(routeDepStation);
                }
            } else {
                setDepartureStation(routeDepStation);
                setDestinationStation(routeDestStation);
            }
        }
    }

    List<Station> onDepartureStationTextChanged(String text) {
        Logger.trace(TAG, "onDepartureStationTextChanged, text = " + text);
        updateDepartureStations(text);
        return departureStations;
    }

    List<Station> onDestinationStationTextChanged(String text) {
        Logger.trace(TAG, "onDestinationStationTextChanged, text = " + text);
        updateDestinationStations(text);
        return destinationStations;
    }

    public void onDepartureStationSelected(int position) {
        Logger.trace(TAG, "onDepartureStationSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    uiThread.post(view::showLoadingDialog);
                    setDepartureStation(departureStations.get(position));
                    uiThread.post(view::hideLoadingDialog);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onDestinationStationSelected(int position) {
        Logger.trace(TAG, "onDestinationStationSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    uiThread.post(view::showLoadingDialog);
                    setDestinationStation(destinationStations.get(position));
                    uiThread.post(view::hideLoadingDialog);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    public void onDepartureStationEditCanceled() {
        Station station = transferSaleData.getDepartureStation();
        view.setDepartureStationName(station == null ? "" : station.getName());
        // Вернем полный список станций
        // За счет кешированного списка без фильтра можем позолить себе сделать операцию в UI
        updateDepartureStations("");
    }

    public void onDestinationStationEditCanceled() {
        Station station = transferSaleData.getDestinationStation();
        view.setDestinationStationName(station == null ? "" : station.getName());
        // Вернем полный список станций
        // За счет кешированного списка без фильтра можем позолить себе сделать операцию в UI
        updateDestinationStations("");
    }

    void onTicketTypeSelected(int position) {
        Logger.trace(TAG, "onTicketTypeSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    uiThread.post(view::showLoadingDialog);
                    // Устанавливаем выбранный тип ПД
                    setTicketTypeByPosition(position);
                    // http://agile.srvdev.ru/browse/CPPKPP-30337
                    // Заключение по ответам Рзянкиной Натальи:
                    // Не беспокоимся, что тарифа для другого тарифного плана в этом направлении может не быть.
                    updateTariffs();
                    // Обновляем видимость кнопки "Продолжить"
                    updateContinueBtnVisibility();
                    // Скрываем прогресс
                    uiThread.post(view::hideLoadingDialog);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onContinueBtnClicked() {
        Logger.trace(TAG, "onContinueBtnClicked");

        List<Long> tariffPlanCodes = CollectionUtils.map(transferSaleData.getTariffPlans(), tariffPlan -> (long) tariffPlan.getCode());

        if (isSourceTicketRequiredForAll()) {
            // Если все виды трансфера требуют привязки к родительскому ПД
            // Запускаем экран чтения ШК/БСК без ограничений по TicketType
            ReadForTransferParams params = new ReadForTransferParams();
            params.setDepartureStationCode(transferSaleData.getDepartureStation().getCode());
            params.setDestinationStationCode(transferSaleData.getDestinationStation().getCode());
            params.setTariffPlanCodes(tariffPlanCodes);
            params.setTicketTypeCode(null);
            params.setNsiVersion(nsiVersion);
            params.setTimestamp(timestamp);
            Logger.trace(TAG, "navigateToReadSourceTicket without ticket type restrictions");
            interactionListener.navigateToReadSourceTicket(params);
        } else if (transferSaleData.getTicketType().isRequireSourceTicket()) {
            // Если выбранный вид трансфера требует привязки к родительскому ПД
            // Запускаем экран чтения ШК/БСК c ограничением по выбранному TicketType
            ReadForTransferParams params = new ReadForTransferParams();
            params.setDepartureStationCode(transferSaleData.getDepartureStation().getCode());
            params.setDestinationStationCode(transferSaleData.getDestinationStation().getCode());
            params.setTariffPlanCodes(tariffPlanCodes);
            params.setTicketTypeCode((long) transferSaleData.getTicketType().getCode());
            params.setNsiVersion(nsiVersion);
            params.setTimestamp(timestamp);
            Logger.trace(TAG, "navigateToReadSourceTicket with ticket type code = " + params.getTicketTypeCode());
            interactionListener.navigateToReadSourceTicket(params);
        } else {
            // Выбранный вид трансфера не требует привязки к родительскому ПД
            // Запускаем экран оформления трансфера c ограничением по выбранному TicketType
            TransferSaleParams params = new TransferSaleParams();
            params.setDepartureStationCode(transferSaleData.getDepartureStation().getCode());
            params.setDestinationStationCode(transferSaleData.getDestinationStation().getCode());
            params.setTicketTypeCode((long) transferSaleData.getTicketType().getCode());
            params.setNsiVersion(nsiVersion);
            params.setTimestamp(timestamp);
            Logger.trace(TAG, "navigateToTransferSale with ticket type code = " + params.getTicketTypeCode());
            interactionListener.navigateToTransferSale(params);
        }
    }

    void onCriticalNsiBackDialogRead() {
        interactionListener.navigateBack();
    }

    void onCriticalNsiOkBtnClicked() {
        interactionListener.navigateToCloseShiftActivity();
    }

    private void updateTariffPlan() {
        transferSaleData.setTariffPlans(findTariffPlans(transferSaleData.getDepartureStation(), transferSaleData.getDestinationStation()));
    }

    @NonNull
    private List<TariffPlan> findTariffPlans(@Nullable Station departureStation,
                                             @Nullable Station destinationStation) {
        if (departureStation == null || destinationStation == null) {
            // Ничего не ищем, если по какой-то причине не были заданы все поля
            return Collections.emptyList();
        }

        // http://agile.srvdev.ru/browse/CPPKPP-44096
        // Например, при выборе оформления до Домодедово
        // сюда прилетают 2 тарифных плана:
        // - на пассажирский поезд
        // - на скорый поезд
        List<TariffPlan> tariffPlans = pdSaleEnv.tariffPlansLoader().loadDirectTariffPlans(
                (long) departureStation.getCode(),
                (long) destinationStation.getCode()
        );

        Logger.trace(TAG, "tariffPlans size = " + tariffPlans.size());

        return tariffPlans;
    }

    private void updateTariffs() {
        Logger.trace(TAG, "updateTariffs");
        // Находим тариф "Туда" и парный ему тариф "Обратно"
        Pair<List<Tariff>, List<Tariff>> tariffsThereAndBack = findTariffs(
                transferSaleData.getTariffPlans(),
                transferSaleData.getTicketType(),
                transferSaleData.getDepartureStation(),
                transferSaleData.getDestinationStation()
        );
        if (tariffsThereAndBack == null) {
            transferSaleData.setTariffsThere(null);
        } else {
            transferSaleData.setTariffsThere(tariffsThereAndBack.first);
        }
    }

    @Nullable
    private Pair<List<Tariff>, List<Tariff>> findTariffs(@Nullable List<TariffPlan> tariffPlans,
                                                         @Nullable TicketType ticketType,
                                                         @Nullable Station departureStation,
                                                         @Nullable Station destinationStation) {
        if (tariffPlans == null || ticketType == null || departureStation == null || destinationStation == null) {
            // Ничего не ищем, если по какой-то причине не были заданы все поля
            return null;
        }

        Pair<List<TariffsChain>, List<TariffsChain>> foundTariffs = pdSaleEnv.tariffsLoader().loadDirectTariffsThereAndBack(
                (long) departureStation.getCode(),
                (long) destinationStation.getCode(),
                CollectionUtils.asSet(tariffPlans, tariffPlan -> (long) tariffPlan.getCode()),
                (long) ticketType.getCode()
        );
        if (!foundTariffs.first.isEmpty()) {
            // Есть прямой тариф
            return new Pair<>(foundTariffs.first.get(0).getTariffs(),
                    foundTariffs.second.isEmpty() ? null : foundTariffs.second.get(0).getTariffs());
        } else {
            return null;
        }
    }

    private List<Station> findDepartureStations(@NonNull String filter) {
        String likeQuery = filter.toUpperCase(Locale.getDefault());
        return pdSaleEnv.depStationsLoader().loadDirectStations(
                null,
                null,
                likeQuery);
    }

    private List<Station> findDestinationStations(@Nullable Long fromStationCode, @NonNull String filter) {
        String likeQuery = filter.toUpperCase(Locale.getDefault());
        return pdSaleEnv.destStationsLoader().loadDirectStations(
                fromStationCode,
                null,
                likeQuery);
    }

    private void updateDepartureStations(@NonNull String filter) {
        String likeQuery = filter.toUpperCase(Locale.getDefault());

        if ("".equals(filter) && departureStationsWithoutFilter != null) {
            // Используем кешированный список
            departureStations = departureStationsWithoutFilter;
        } else {
            departureStations = findDepartureStations(likeQuery);

            if ("".equals(filter)) {
                // Кешируем список станций
                departureStationsWithoutFilter = departureStations;
            }
        }

        uiThread.post(() -> view.setDepartureStations(departureStations));
    }

    private void updateDestinationStations(@NonNull String filter) {
        String likeQuery = filter.toUpperCase(Locale.getDefault());

        if ("".equals(filter) && destinationStationsWithoutFilter != null) {
            // Используем кешированный список
            destinationStations = destinationStationsWithoutFilter;
        } else {
            destinationStations = findDestinationStations(
                    transferSaleData.getDepartureStation() == null ? null : (long) transferSaleData.getDepartureStation().getCode(),
                    likeQuery);

            if ("".equals(filter)) {
                // Кешируем список станций
                destinationStationsWithoutFilter = destinationStations;
            }
        }

        uiThread.post(() -> view.setDestinationStations(destinationStations));
    }

    private void setDepartureStation(Station station) {
        // Устнавливаем выбранную станцию
        transferSaleData.setDepartureStation(station);
        uiThread.post(() -> view.setDepartureStationName(transferSaleData.getDepartureStation() == null ? "" : transferSaleData.getDepartureStation().getName()));
        // Возвращаем полный список станций отправления
        updateDepartureStations("");
        // Очищаем кешированный список станций назначения
        destinationStationsWithoutFilter = null;
        // Обновляем список станций наначения
        updateDestinationStations("");
        // Сбрасываем выбранную станцию назначения
        setDestinationStation(null);
    }

    private void setDestinationStation(Station station) {
        // Устнавливаем выбранную станцию
        transferSaleData.setDestinationStation(station);
        uiThread.post(() -> view.setDestinationStationName(transferSaleData.getDestinationStation() == null ? "" : transferSaleData.getDestinationStation().getName()));
        // Возвращаем полный список станций назначения
        updateDestinationStations("");
        // Обновляем тарифный план
        updateTariffPlan();
        // Обновляем список типов ПД
        updateTicketTypes();
        // Обновляем тариф
        updateTariffs();
        // Обновляем видимость кнопки "Продолжить"
        updateContinueBtnVisibility();
    }

    private void updateContinueBtnVisibility() {
            uiThread.post(() -> view.setContinueBtnVisible(transferSaleData.getTariffsThere() != null));
    }

    private void updateTicketTypes() {
        // Обновляем список типов ПД
        ticketTypes = findTicketTypes(transferSaleData.getTariffPlans(), transferSaleData.getDepartureStation(), transferSaleData.getDestinationStation());
        uiThread.post(() -> view.setTicketTypes(ticketTypes));
        // Ищем в новом списке индекс текущего типа ПД
        TicketType currentTicketType = transferSaleData.getTicketType();
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
        // Обновляем видимость спиннера с выбором вида трансфера
        uiThread.post(() -> view.setTicketTypesSelectVisible(!isSourceTicketRequiredForAll()));
    }

    /**
     * Проверяет, требуется ли предъявление ПД на поезд для всех видов трансфера.
     *
     * @return {@code true} если все виды требуют родительский ПД,
     * или список видов ПД пуст, {@code false} - иначе
     */
    private boolean isSourceTicketRequiredForAll() {
        for (TicketType ticketType : ticketTypes) {
            if (!ticketType.isRequireSourceTicket()) {
                return false;
            }
        }
        return true;
    }

    private void setTicketTypeByPosition(int position) {
        if (position == -1) {
            transferSaleData.setTicketType(null);
        } else {
            transferSaleData.setTicketType(ticketTypes.get(position));
        }
        uiThread.post(() -> view.setSelectedTicketTypePosition(position));
    }

    @NonNull
    private List<TicketType> findTicketTypes(@Nullable List<TariffPlan> tariffPlans,
                                             @Nullable Station departureStation,
                                             @Nullable Station destinationStation) {
        if (tariffPlans == null || departureStation == null || destinationStation == null) {
            // Ничего не ищем, если по какой-то причине не были заданы все поля
            return Collections.emptyList();
        }
        // Получаем список типов ПД
        return pdSaleEnv.ticketTypesLoader().loadDirectTicketTypes(
                (long) departureStation.getCode(),
                (long) destinationStation.getCode(),
                CollectionUtils.asSet(tariffPlans, tariffPlan -> (long) tariffPlan.getCode())
        );
    }

    public interface InteractionListener {
        void navigateBack();

        void navigateToCloseShiftActivity();

        void navigateToReadSourceTicket(ReadForTransferParams params);

        void navigateToTransferSale(TransferSaleParams transferSaleParams);
    }
}
