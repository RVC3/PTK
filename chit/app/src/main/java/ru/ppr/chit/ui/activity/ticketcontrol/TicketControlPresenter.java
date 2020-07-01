package ru.ppr.chit.ui.activity.ticketcontrol;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.domain.boarding.BoardingManager;
import ru.ppr.chit.domain.model.local.Ticket;
import ru.ppr.chit.domain.model.local.TicketControlEvent;
import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.model.nsi.CredentialDocumentType;
import ru.ppr.chit.domain.model.nsi.Station;
import ru.ppr.chit.domain.model.nsi.TicketStorageType;
import ru.ppr.chit.domain.model.nsi.TicketType;
import ru.ppr.chit.domain.provider.NsiVersionProvider;
import ru.ppr.chit.domain.repository.local.PassengerPersonalDataRepository;
import ru.ppr.chit.domain.repository.local.PlaceLocationRepository;
import ru.ppr.chit.domain.repository.local.TicketIdRepository;
import ru.ppr.chit.domain.repository.local.TicketRepository;
import ru.ppr.chit.domain.repository.nsi.CredentialDocumentTypeRepository;
import ru.ppr.chit.domain.repository.nsi.StationRepository;
import ru.ppr.chit.domain.repository.nsi.TicketTypeRepository;
import ru.ppr.chit.domain.ticketcontrol.CompletedTicketControlEventBuilder;
import ru.ppr.chit.domain.ticketcontrol.CreatedTicketControlEventBuilder;
import ru.ppr.chit.domain.ticketcontrol.DataCarrierType;
import ru.ppr.chit.domain.ticketcontrol.StoreCompletedTicketControlEventInteractor;
import ru.ppr.chit.domain.ticketcontrol.StoreCreatedTicketControlEventInteractor;
import ru.ppr.chit.domain.ticketcontrol.TicketControlData;
import ru.ppr.chit.domain.tripservice.TripServiceMode;
import ru.ppr.chit.domain.tripservice.TripServiceModeManager;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.helpers.PdWithPlaceBarcodeStorage;
import ru.ppr.chit.helpers.UiThread;
import ru.ppr.chit.helpers.readbscstorage.PdWithPlaceCardData;
import ru.ppr.chit.helpers.readbscstorage.PdWithPlaceCardDataStorage;
import ru.ppr.chit.ui.activity.ticketcontrol.interactor.DepartureDatesCalculator;
import ru.ppr.chit.ui.activity.ticketcontrol.interactor.TicketValidityChecker;
import ru.ppr.chit.ui.activity.ticketcontrol.model.TicketValidationResult;
import ru.ppr.core.dataCarrier.pd.base.PdForDays;
import ru.ppr.core.dataCarrier.pd.base.PdWithPlace;
import ru.ppr.core.dataCarrier.pd.v9.PdV9;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.core.logic.FioFormatter;
import ru.ppr.core.manager.eds.CheckSignResultState;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ObjectUtils;

/**
 * @author Aleksandr Brazhkin
 */
public class TicketControlPresenter extends BaseMvpViewStatePresenter<TicketControlView, TicketControlViewState> {

    private static final String TAG = Logger.makeLogTag(TicketControlPresenter.class);

    //region Common fields
    private boolean initialized = false;
    //endregion
    //region Di
    private final long ticketId;
    private final boolean fromBsc;
    private final boolean fromBarcode;
    private final TicketRepository ticketRepository;
    private final PlaceLocationRepository placeLocationRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketIdRepository ticketIdRepository;
    private final StationRepository stationRepository;
    private final PassengerPersonalDataRepository passengerPersonalDataRepository;
    private final CredentialDocumentTypeRepository credentialDocumentTypeRepository;
    private final TicketValidityChecker ticketValidityChecker;
    private final NsiVersionProvider nsiVersionProvider;
    private final CreatedTicketControlEventBuilder createdTicketControlEventBuilder;
    private final CompletedTicketControlEventBuilder completedTicketControlEventBuilder;
    private final StoreCreatedTicketControlEventInteractor storeCreatedTicketControlEventInteractor;
    private final StoreCompletedTicketControlEventInteractor storeCompletedTicketControlEventInteractor;
    private final BoardingManager boardingManager;
    private final PdWithPlaceCardDataStorage pdWithPlaceCardDataStorage;
    private final PdWithPlaceBarcodeStorage pdWithPlaceBarcodeStorage;
    private final DepartureDatesCalculator departureDatesCalculator;
    private final UiThread uiThread;
    private final FioFormatter fioFormatter;
    private final TripServiceModeManager tripServiceModeManager;
    //endregion
    //region Other
    private Navigator navigator;
    private Disposable displayDataDisposable = Disposables.disposed();
    private TicketControlEvent ticketControlEvent;
    //endregion

    @Inject
    TicketControlPresenter(TicketControlViewState passengerListViewState,
                           @Named("ticketId") long ticketId,
                           @Named("fromBsc") boolean fromBsc,
                           @Named("fromBarcode") boolean fromBarcode,
                           TicketRepository ticketRepository,
                           PlaceLocationRepository placeLocationRepository,
                           TicketTypeRepository ticketTypeRepository,
                           TicketIdRepository ticketIdRepository,
                           StationRepository stationRepository,
                           PassengerPersonalDataRepository passengerPersonalDataRepository,
                           CredentialDocumentTypeRepository credentialDocumentTypeRepository,
                           TicketValidityChecker ticketValidityChecker,
                           NsiVersionProvider nsiVersionProvider,
                           CreatedTicketControlEventBuilder createdTicketControlEventBuilder,
                           CompletedTicketControlEventBuilder completedTicketControlEventBuilder,
                           StoreCreatedTicketControlEventInteractor storeCreatedTicketControlEventInteractor,
                           StoreCompletedTicketControlEventInteractor storeCompletedTicketControlEventInteractor,
                           BoardingManager boardingManager,
                           PdWithPlaceCardDataStorage pdWithPlaceCardDataStorage,
                           PdWithPlaceBarcodeStorage pdWithPlaceBarcodeStorage,
                           DepartureDatesCalculator departureDatesCalculator,
                           UiThread uiThread,
                           FioFormatter fioFormatter,
                           TripServiceModeManager tripServiceModeManager) {
        super(passengerListViewState);
        this.ticketId = ticketId;
        this.fromBsc = fromBsc;
        this.fromBarcode = fromBarcode;
        this.ticketRepository = ticketRepository;
        this.placeLocationRepository = placeLocationRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.ticketIdRepository = ticketIdRepository;
        this.stationRepository = stationRepository;
        this.passengerPersonalDataRepository = passengerPersonalDataRepository;
        this.credentialDocumentTypeRepository = credentialDocumentTypeRepository;
        this.ticketValidityChecker = ticketValidityChecker;
        this.nsiVersionProvider = nsiVersionProvider;
        this.createdTicketControlEventBuilder = createdTicketControlEventBuilder;
        this.completedTicketControlEventBuilder = completedTicketControlEventBuilder;
        this.storeCreatedTicketControlEventInteractor = storeCreatedTicketControlEventInteractor;
        this.storeCompletedTicketControlEventInteractor = storeCompletedTicketControlEventInteractor;
        this.boardingManager = boardingManager;
        this.pdWithPlaceCardDataStorage = pdWithPlaceCardDataStorage;
        this.pdWithPlaceBarcodeStorage = pdWithPlaceBarcodeStorage;
        this.departureDatesCalculator = departureDatesCalculator;
        this.uiThread = uiThread;
        this.fioFormatter = fioFormatter;
        this.tripServiceModeManager = tripServiceModeManager;
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");

        displayDataDisposable = Completable
                .fromAction(() -> {
                    uiThread.post(() -> view.setState(TicketControlView.State.PREPARING));

                    TicketControlData ticketControlData;
                    PdWithPlace pdWithPlace = null;
                    byte eds[] = null;
                    CardInformation cardInformation = null;

                    Logger.trace(TAG, "state.fromBarcode: " + fromBarcode);
                    Logger.trace(TAG, "state.fromBsc: " + fromBsc);

                    if (fromBarcode) {
                        Logger.trace(TAG, "state.fromList: " + false);

                        pdWithPlace = pdWithPlaceBarcodeStorage.getLastData();
                        if (pdWithPlace == null) {
                            uiThread.post(() -> view.setState(TicketControlView.State.NO_DATA));
                            return;
                        }
                        ticketControlData = buildFromPd(pdWithPlace, null);
                        if (pdWithPlace instanceof PdV9) {
                            // Получаем данные ЭЦП
                            eds = ((PdV9) pdWithPlace).getEds();
                        }
                    } else if (fromBsc) {
                        Logger.trace(TAG, "state.fromList: " + false);

                        PdWithPlaceCardData pdWithPlaceCardData = pdWithPlaceCardDataStorage.getLastCardData();
                        if (pdWithPlaceCardData == null) {
                            uiThread.post(() -> view.setState(TicketControlView.State.NO_DATA));
                            return;
                        }
                        pdWithPlace = pdWithPlaceCardData.getPdWithPlace();
                        if (pdWithPlace == null) {
                            uiThread.post(() -> view.setState(TicketControlView.State.NO_DATA));
                            return;
                        }
                        cardInformation = pdWithPlaceCardData.getCardInformation();
                        if (cardInformation == null) {
                            uiThread.post(() -> view.setState(TicketControlView.State.NO_DATA));
                            return;
                        }
                        ticketControlData = buildFromPd(pdWithPlace, cardInformation);
                        // Получаем данные ЭЦП
                        eds = pdWithPlaceCardData.getEds();
                    } else {
                        Logger.trace(TAG, "state.fromList: " + true);

                        Ticket ticket = ticketRepository.load(ticketId);
                        if (ticket == null) {
                            uiThread.post(() -> view.setState(TicketControlView.State.NO_DATA));
                            return;
                        }
                        ticketControlData = buildFromTicket(ticket);
                    }

                    int versionId = nsiVersionProvider.getCurrentNsiVersion();

                    TicketType ticketType = ticketTypeRepository.load(ticketControlData.getTicketTypeCode(), versionId);
                    Station departureStation = stationRepository.load(ticketControlData.getDepartureStationCode(), versionId);
                    Station destinationStation = stationRepository.load(ticketControlData.getDestinationStationCode(), versionId);
                    CredentialDocumentType documentType = credentialDocumentTypeRepository.load(ticketControlData.getDocumentTypeCode(), versionId);
                    String fio = fioFormatter.getFullNameAsSurnameWithInitials(
                            ticketControlData.getLastName(),
                            ticketControlData.getFirstName(),
                            ticketControlData.getSecondName()
                    );
                    Logger.trace(TAG, "view.setTicketNumber: " + ticketControlData.getTicketNumber());
                    Logger.trace(TAG, "view.setTicketType: " + (ticketType == null ? null : ticketType.getShortName()));
                    Logger.trace(TAG, "view.setDataCarrierType: " + ticketControlData.getDataCarrierType());
                    Logger.trace(TAG, "view.setDate: " + ticketControlData.getDepartureDates().get(0));
                    Logger.trace(TAG, "view.setTrainNumber: " + ticketControlData.getTrainNumber());
                    Logger.trace(TAG, "view.setDepStationName: " + (departureStation == null ? null : departureStation.getName()));
                    Logger.trace(TAG, "view.setDestStationName: " + (destinationStation == null ? null : destinationStation.getName()));
                    Logger.trace(TAG, "view.setExemptionExpressCode: " + ticketControlData.getExemptionExpressCode());
                    Logger.trace(TAG, "view.setPassengerFio: " + fio);
                    Logger.trace(TAG, "view.setDocumentType: " + (documentType == null ? null : documentType.getName()));
                    Logger.trace(TAG, "view.setDocumentNumber: " + ticketControlData.getDocumentNumber());

                    // Отображаем данные
                    uiThread.post(() -> {
                        view.setTicketNumber(ticketControlData.getTicketNumber());
                        view.setTicketType(ticketType == null ? null : ticketType.getShortName());
                        view.setDataCarrierType(ticketControlData.getDataCarrierType());
                        view.setDate(ticketControlData.getDepartureDates().get(0));
                        view.setTrainNumber(ticketControlData.getTrainNumber());
                        view.setDepStationName(departureStation == null ? null : departureStation.getName());
                        view.setDestStationName(destinationStation == null ? null : destinationStation.getName());
                        view.setExemptionExpressCode(ticketControlData.getExemptionExpressCode());
                        view.setPassengerFio(fio);
                        view.setDocumentType(documentType == null ? null : documentType.getName());
                        view.setDocumentNumber(ticketControlData.getDocumentNumber());
                    });

                    // Создание события контроля необходимо, если:
                    boolean needCreateControlEvent =
                            // - Сейчас режим посадки, т.е. посадка началась (только в ONLINE)
                            boardingManager.isBoardingStarted() ||
                                    // - или мы находимся в оффлайн режиме
                                    tripServiceModeManager.detectTripServiceMode() == TripServiceMode.OFFLINE;

                    Logger.trace(TAG, "needCreateControlEvent = " + needCreateControlEvent);

                    // Проверяем билет всегда
                    TicketValidityChecker.Result validityResult = ticketValidityChecker.check(ticketControlData, pdWithPlace, eds, cardInformation);
                    Logger.trace(TAG, "validityResult = " + validityResult);

                    // Обновляем id устройства продажи
                    ticketControlData.setDeviceId(validityResult.getDeviceId());

                    // Обновляем флаг валидности ЭЦП
                    // Igor Chikalev: KEY_REVOKED -> false
                    ticketControlData.setEdsValid(validityResult.getCheckSignResultState() == CheckSignResultState.VALID);

                    // Обновляем флаг наличия билета в белом списке
                    ticketControlData.setInWhiteList(validityResult.isInWhiteList());

                    if (needCreateControlEvent) {
                        // Если требуется создание события контроля, сохраняем его в БД со статусом STARTED
                        // Событие останется в таком статусе в с следующих случаях:
                        // - ПД валиден, пользователь нажимает "Назад"
                        // - Резкое прекращение работы приложения (выключение питания)
                        // Данные события не должны выгружаться на базовую станцию
                        ticketControlEvent = createdTicketControlEventBuilder.setTicketControlData(ticketControlData).build();
                        storeCreatedTicketControlEventInteractor.store(ticketControlEvent);
                        if (!validityResult.isValid()) {
                            // Если ПД не валиден, сразу обновляем событие до статуса COMPLETED
                            // Кнопки "Подтвердить" и "Отказ" будут недоступны
                            // http://agile.srvdev.ru/browse/CPPKPP-40572
                            updateEventToCompletedState(false);
                        }
                    }

                    // Определяем значения ожидаемых и фактических номеров вагона и места
                    String expectedCarNumber;
                    String actualCarNumber;
                    String expectedSeatNumber;
                    String actualSeatNumber;

                    if (fromBarcode || fromBsc) {
                        expectedCarNumber = ticketControlData.getCarNumber();
                        expectedSeatNumber = ticketControlData.getSeatNumber();

                        // Получаем идентификатор билета
                        TicketId ticketIdInDb = ticketIdRepository.loadByIdentity(
                                ticketControlData.getTicketNumber(),
                                ticketControlData.getSaleDateTime(),
                                String.valueOf(ticketControlData.getDeviceId())
                        );
                        // Получаем билет
                        Ticket ticket = null;
                        if (ticketIdInDb != null) {
                            ticket = ticketRepository.loadByTicket(ticketIdInDb.getId());
                        }

                        if (ticket != null) {
                            actualCarNumber = ticket.getPlaceLocation(placeLocationRepository).getCarNumber();
                            actualSeatNumber = ticket.getPlaceLocation(placeLocationRepository).getPlaceNumber();
                        } else {
                            actualCarNumber = expectedCarNumber;
                            actualSeatNumber = expectedSeatNumber;
                        }
                    } else {
                        actualCarNumber = expectedCarNumber = ticketControlData.getCarNumber();
                        actualSeatNumber = expectedSeatNumber = ticketControlData.getSeatNumber();
                    }

                    // Определяем изменение номера вагона
                    boolean carNumberChanged = isCarNumberChanged(expectedCarNumber, actualCarNumber);

                    // Определяем изменение номера места
                    boolean seatNumberChanged = isSeatNumberChanged(expectedSeatNumber, actualSeatNumber);

                    Logger.trace(TAG, "view.setExpectedCarNumber: " + expectedCarNumber);
                    Logger.trace(TAG, "view.setActualCarNumber: " + actualCarNumber);
                    Logger.trace(TAG, "view.setNewCarInfoVisible: " + carNumberChanged);
                    Logger.trace(TAG, "view.setExpectedSeatNumber: " + expectedSeatNumber);
                    Logger.trace(TAG, "view.setActualSeatNumber: " + actualSeatNumber);
                    Logger.trace(TAG, "view.setNewSeatInfoVisible: " + seatNumberChanged);

                    // Отображаем данные
                    uiThread.post(() -> {
                        view.setCarNumber(expectedCarNumber, actualCarNumber);
                        view.setNewCarInfoVisible(carNumberChanged);
                        view.setSeatNumber(expectedSeatNumber, actualSeatNumber);
                        view.setNewSeatInfoVisible(seatNumberChanged);
                    });

                    // Отображаем результат проверки
                    uiThread.post(() -> {
                        TicketControlView.DateValidity dateValidity = TicketControlView.DateValidity.NOT_VALID;
                        if (validityResult.isDepartureDateFullyValid()) {
                            dateValidity = TicketControlView.DateValidity.FULLY_VALID;
                        } else if (validityResult.isDepartureDateProbablyValid()) {
                            dateValidity = TicketControlView.DateValidity.PROBABLY_VALID;
                        }
                        view.setDateValid(dateValidity);
                        view.setTrainValid(validityResult.isTrainNumberValid());
                        view.setDepStationValid(validityResult.isDepStationValid());
                        view.setDestinationStationValid(validityResult.isDestinationStationValid());
                        if (validityResult.isValid()) {
                            // Если дата валидна только на усмотрение проводника, показываем уведомление
                            if (validityResult.isDepartureDateProbablyValid()) {
                                view.setValidationResult(TicketValidationResult.PROBABLY_SUCCESS_BY_DATE);
                            } else {
                                view.setValidationResult(TicketValidationResult.SUCCESS);
                            }
                        } else {
                            if (validityResult.getCheckSignResultState() == CheckSignResultState.INVALID) {
                                view.setValidationResult(TicketValidationResult.INVALID_EDS_KEY);
                            } else if (validityResult.getCheckSignResultState() == CheckSignResultState.KEY_REVOKED && !validityResult.isInWhiteList()) {
                                // Показываем в UI, что ключ отозван, только если билета нет в белом списке
                                view.setValidationResult(TicketValidationResult.REVOKED_EDS_KEY);
                            } else if (validityResult.isTicketCancelled()) {
                                view.setValidationResult(TicketValidationResult.CANCELLED);
                            } else if (validityResult.isTicketReturned()) {
                                view.setValidationResult(TicketValidationResult.RETURNED);
                            } else {
                                view.setValidationResult(TicketValidationResult.INVALID_DATA);
                            }
                        }
                        if (needCreateControlEvent && validityResult.isValid()) {
                            // Если требуется создание события контроля и ПД валиден
                            // Отображаем кнопки "Подтвердить" и "Отказ"
                            view.setApproveBtnVisible(true);
                            view.setDenyBtnVisible(true);
                        } else {
                            // Если не требуется создание события контроля или ПД невалиден
                            // Скрываем кнопки "Подтвердить" и "Отказ"
                            view.setApproveBtnVisible(false);
                            view.setDenyBtnVisible(false);
                        }

                        view.setState(TicketControlView.State.DATA);
                    });
                })
                .subscribeOn(AppSchedulers.background())
                .subscribe(() -> Logger.trace(TAG, "displayDataDisposable onComplete"),
                        throwable -> {
                            Logger.error(TAG, "displayDataDisposable onError", throwable);
                            uiThread.post(() -> view.setState(TicketControlView.State.NO_DATA));
                        });
    }

    void onApproveBtnClicked() {
        Logger.trace(TAG, "onApproveBtnClicked");
        updateEventToCompletedState(true);
        navigator.navigateBack();
    }

    void onDenyBtnClicked() {
        Logger.trace(TAG, "onDenyBtnClicked");
        updateEventToCompletedState(false);
        navigator.navigateBack();
    }

    private TicketControlData buildFromPd(@NonNull PdWithPlace pdWithPlace, @Nullable CardInformation cardInformation) {
        TicketControlData ticketControlData = new TicketControlData();
        ticketControlData.setTicketNumber(pdWithPlace.getOrderNumber());
        ticketControlData.setSaleDateTime(pdWithPlace.getSaleDateTime());
        ticketControlData.setTicketTypeCode(pdWithPlace.getTicketTypeCode());
        ticketControlData.setDataCarrierType(cardInformation == null ? DataCarrierType.BARCODE : DataCarrierType.SMART_CARD);
        ticketControlData.setDepartureDates(departureDatesCalculator.calc(
                pdWithPlace.getSaleDateTime(),
                pdWithPlace.getDepartureDayOffset(),
                pdWithPlace.getDepartureTime(),
                pdWithPlace instanceof PdForDays ? ((PdForDays) pdWithPlace).getForDays() : 1
        ));
        ticketControlData.setTrainNumber(String.valueOf(pdWithPlace.getTrainNumber()));
        ticketControlData.setDepartureStationCode(pdWithPlace.getDepartureStationCode());
        ticketControlData.setDestinationStationCode(pdWithPlace.getDestinationStationCode());
        ticketControlData.setExemptionExpressCode(pdWithPlace.getExemptionCode() == 0 ? null : pdWithPlace.getExemptionCode());
        ticketControlData.setCarNumber(String.valueOf(pdWithPlace.getWagonNumber()));
        ticketControlData.setSeatNumber(String.valueOf(pdWithPlace.getPlaceNumber()));
        ticketControlData.setLastName(trimNeedlessSymbols(pdWithPlace.getLastName()));
        ticketControlData.setFirstName(pdWithPlace.getFirstNameInitial());
        ticketControlData.setSecondName(pdWithPlace.getSecondNameInitial());
        ticketControlData.setDocumentTypeCode(pdWithPlace.getDocumentTypeCode());
        ticketControlData.setDocumentNumber(pdWithPlace.getDocumentNumber());
        ticketControlData.setEdsKeyNumber(pdWithPlace.getEdsKeyNumber());

        if (cardInformation != null) {
            ticketControlData.setCardOuterNumber(cardInformation.getOuterNumberAsFormattedString());
            ticketControlData.setCardCrystalSerialNumber(cardInformation.getCrystalSerialNumberAsString());
            TicketStorageType ticketStorageType = TicketStorageType.valueOf(cardInformation.getCardType().getNsiCode());
            ticketControlData.setTicketStorageType(ticketStorageType);
        }

        return ticketControlData;
    }

    private TicketControlData buildFromTicket(Ticket ticket) {
        TicketControlData ticketControlData = new TicketControlData();
        ticketControlData.setTicketNumber(ticket.getTicketId(ticketIdRepository).getTicketNumber());
        ticketControlData.setSaleDateTime(ticket.getTicketId(ticketIdRepository).getSaleDate());
        ticketControlData.setTicketTypeCode(ticket.getTicketTypeCode());
        ticketControlData.setDataCarrierType(DataCarrierType.TICKET_LIST);
        ticketControlData.setDepartureDates(Collections.singletonList(ticket.getDepartureDate()));
        ticketControlData.setTrainNumber(ticket.getTrainNumber());
        ticketControlData.setDepartureStationCode(ticket.getDepartureStationCode());
        ticketControlData.setDestinationStationCode(ticket.getDestinationStationCode());
        ticketControlData.setExemptionExpressCode(ticket.getExemptionExpressCode());
        ticketControlData.setCarNumber(ticket.getPlaceLocation(placeLocationRepository).getCarNumber());
        ticketControlData.setSeatNumber(ticket.getPlaceLocation(placeLocationRepository).getPlaceNumber());
        ticketControlData.setLastName(ticket.getPassenger(passengerPersonalDataRepository).getLastName());
        ticketControlData.setFirstName(ticket.getPassenger(passengerPersonalDataRepository).getFirstName());
        ticketControlData.setSecondName(ticket.getPassenger(passengerPersonalDataRepository).getMiddleName());
        ticketControlData.setDocumentTypeCode(ticket.getPassenger(passengerPersonalDataRepository).getDocumentTypeCode());
        ticketControlData.setDocumentNumber(ticket.getPassenger(passengerPersonalDataRepository).getDocumentNumber());
        ticketControlData.setDeviceId(Long.valueOf(ticket.getTicketId(ticketIdRepository).getDeviceId()));
        return ticketControlData;
    }

    private void updateEventToCompletedState(boolean wasBoarded) {
        Logger.trace(TAG, "updateEventToCompletedState, wasBoarded = " + wasBoarded);
        // Сохраняем событие контроля в БД со статусом COMPLETED
        ticketControlEvent = completedTicketControlEventBuilder
                .setTicketControlEvent(ticketControlEvent)
                .setWasBoarded(wasBoarded)
                .build();
        storeCompletedTicketControlEventInteractor.store(ticketControlEvent);
    }

    /**
     * Удалять ненужные символы из строки, как правило это пустые символы
     */
    private String trimNeedlessSymbols(String str) {
        if (str == null) {
            return null;
        }
        // Вырезаем ненужные символы
        str = str.replace("\u0000", "");
        str = str.replace("\\u0000", "");
        str = str.trim();
        return str;
    }

    private boolean isCarNumberChanged(@Nullable String expectedCarNumber, @Nullable String actualCarNumber) {
        if (TextUtils.isEmpty(expectedCarNumber) && TextUtils.isEmpty(actualCarNumber)) {
            return false;
        }
        return !ObjectUtils.equals(expectedCarNumber, actualCarNumber);
    }

    private boolean isSeatNumberChanged(@Nullable String expectedSeatNumber, @Nullable String actualSeatNumber) {
        if (TextUtils.isEmpty(expectedSeatNumber) && TextUtils.isEmpty(actualSeatNumber)) {
            return false;
        }
        return !ObjectUtils.equals(expectedSeatNumber, actualSeatNumber);
    }

    @Override
    public void destroy() {
        super.destroy();
        displayDataDisposable.dispose();
    }

    interface Navigator {

        void navigateBack();
    }
}
