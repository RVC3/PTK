package ru.ppr.chit.domain.ticketcontrol;

import javax.inject.Inject;

import ru.ppr.chit.domain.boarding.BoardingStatusChecker;
import ru.ppr.chit.domain.event.EventBuilder;
import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.Location;
import ru.ppr.chit.domain.model.local.Passenger;
import ru.ppr.chit.domain.model.local.SmartCard;
import ru.ppr.chit.domain.model.local.TicketBoarding;
import ru.ppr.chit.domain.model.local.TicketControlEvent;
import ru.ppr.chit.domain.model.local.TicketData;
import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.model.local.User;
import ru.ppr.chit.domain.provider.NsiVersionProvider;
import ru.ppr.chit.domain.repository.local.BoardingEventRepository;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.tripservice.TripServiceMode;
import ru.ppr.chit.domain.tripservice.TripServiceModeManager;

/**
 * Билдер события {@link TicketControlEvent} в статусе {@link TicketControlEvent.Status#CREATED}.
 *
 * @author Aleksandr Brazhkin
 */
public class CreatedTicketControlEventBuilder {

    private final EventBuilder eventBuilder;
    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final BoardingEventRepository boardingEventRepository;
    private final BoardingStatusChecker boardingStatusChecker;
    private final NsiVersionProvider nsiVersionProvider;
    private final TripServiceModeManager tripServiceModeManager;
    //////////////////////////////////////////////////////////////////
    private TicketControlData ticketControlData;

    @Inject
    CreatedTicketControlEventBuilder(EventBuilder eventBuilder,
                                     TripServiceInfoStorage tripServiceInfoStorage,
                                     BoardingEventRepository boardingEventRepository,
                                     BoardingStatusChecker boardingStatusChecker,
                                     NsiVersionProvider nsiVersionProvider,
                                     TripServiceModeManager tripServiceModeManager) {
        this.eventBuilder = eventBuilder;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.boardingEventRepository = boardingEventRepository;
        this.boardingStatusChecker = boardingStatusChecker;
        this.nsiVersionProvider = nsiVersionProvider;
        this.tripServiceModeManager = tripServiceModeManager;
    }

    public CreatedTicketControlEventBuilder setTicketControlData(TicketControlData ticketControlData) {
        this.ticketControlData = ticketControlData;
        return this;
    }

    public TicketControlEvent build() {
        // Билдим события с разным наполнением, в зависимости от режима обслуживания
        if (tripServiceModeManager.detectTripServiceMode() == TripServiceMode.ONLINE) {
            return buildForOnline();
        } else {
            return buildForOffline();
        }
    }

    private TicketControlEvent buildForOnline() {
        if (ticketControlData == null) {
            throw new IllegalStateException("Ticket control data should not be null");
        }
        ControlStation controlStation = tripServiceInfoStorage.getControlStation();
        if (controlStation == null) {
            throw new IllegalStateException("Control station should not be null");
        }
        User user = tripServiceInfoStorage.getUser();
        if (user == null) {
            throw new IllegalStateException("User should not be null");
        }
        TrainInfo trainInfo = tripServiceInfoStorage.getTrainInfo();
        if (trainInfo == null) {
            throw new IllegalStateException("TrainInfo should not be null");
        }
        TicketControlEvent ticketControlEvent = new TicketControlEvent();
        ticketControlEvent.setStatus(TicketControlEvent.Status.CREATED);
        // Заполняем базовое событие
        Event event = eventBuilder.build();
        ticketControlEvent.setEvent(event);
        // Заполняем идентификатор билета
        TicketId ticketId = new TicketId();
        ticketId.setSaleDate(ticketControlData.getSaleDateTime());
        ticketId.setTicketNumber(ticketControlData.getTicketNumber());
        ticketId.setDeviceId(String.valueOf(ticketControlData.getDeviceId()));
        // Заполняем собственные поля посадки пассажира
        TicketBoarding ticketBoarding = new TicketBoarding();
        ticketBoarding.setTicketId(ticketId);
        ticketBoarding.setTrainNumber(ticketControlData.getTrainNumber());
        ticketBoarding.setTrainThreadId(trainInfo.getTrainThreadId());
        ticketBoarding.setTerminalDeviceId(String.valueOf(ticketControlData.getDeviceId()));
        ticketBoarding.setOperatorName(user.getName());
        ticketBoarding.setControlStationCode(controlStation.getCode());
        ticketBoarding.setEdsKeyNumber(ticketControlData.getEdsKeyNumber());
        ticketBoarding.setEdsValid(ticketControlData.isEdsValid());
        ticketBoarding.setInWhiteList(ticketControlData.isInWhiteList());
        ticketBoarding.setBoardingByList(ticketControlData.getDataCarrierType() == DataCarrierType.TICKET_LIST);
        ticketBoarding.setCheckDate(event.getCreatedAt());
        ticketBoarding.setTicketDataId(null);
        // Заполняем информацию о посадке пассажира
        ticketControlEvent.setTicketBoarding(ticketBoarding);
        // Заполняем информацию о пассажире
        Passenger passenger = new Passenger();
        passenger.setFirstName(ticketControlData.getFirstName());
        passenger.setLastName(ticketControlData.getLastName());
        passenger.setMiddleName(ticketControlData.getSecondName());
        passenger.setDocumentTypeCode(ticketControlData.getDocumentTypeCode());
        passenger.setDocumentNumber(ticketControlData.getDocumentNumber());
        // Заполняем информацию о месте пассажира
        //http://agile.srvdev.ru/browse/CPPKPP-44331
        Location location = null;
        if (ticketControlData.getCarNumber() != null || ticketControlData.getSeatNumber() != null) {
            location = new Location();
            location.setCarNumber(ticketControlData.getCarNumber());
            location.setPlaceNumber(ticketControlData.getSeatNumber());
        }
        // Заполняем информацию об источнике данных
        TicketData ticketData = new TicketData();
        ticketData.setTicketTypeCode(ticketControlData.getTicketTypeCode());
        ticketData.setPassenger(passenger);
        ticketData.setLocation(location);
        ticketData.setDepartureStationCode(ticketControlData.getDepartureStationCode());
        ticketData.setDestinationStationCode(ticketControlData.getDestinationStationCode());
        ticketData.setDepartureDate(ticketControlData.getDepartureDates().get(0));
        ticketData.setExemptionExpressCode(ticketControlData.getExemptionExpressCode());
        // Заполняем информацию о смарт-карте
        if (ticketControlData.getDataCarrierType() == DataCarrierType.SMART_CARD) {
            SmartCard smartCard = new SmartCard();
            smartCard.setOuterNumber(ticketControlData.getCardOuterNumber());
            smartCard.setCrystalSerialNumber(ticketControlData.getCardCrystalSerialNumber());
            smartCard.setType(ticketControlData.getTicketStorageType());
            ticketData.setSmartCard(smartCard);
        } else {
            ticketData.setSmartCard(null);
        }
        // Заполняем информацию об источнике данных
        ticketBoarding.setTicketData(ticketData);
        // Заполняем информацию о версии НСИ
        // В будущем возможно неправильная логика, со слов Александра Корчака пока делаем так
        ticketData.setNsiVersion(nsiVersionProvider.getNsiVersionForDate(ticketControlData.getSaleDateTime()));
        // Заполняем информацию о посадке
        BoardingEvent boardingEvent = boardingEventRepository.loadLast();
        if (!boardingStatusChecker.isStarted(boardingEvent)) {
            throw new IllegalStateException("Ticket control event could not be created out of boarding");
        }
        ticketControlEvent.setBoardingEvent(boardingEvent);

        return ticketControlEvent;
    }

    private TicketControlEvent buildForOffline() {
        if (ticketControlData == null) {
            throw new IllegalStateException("Ticket control data should not be null");
        }
        User user = tripServiceInfoStorage.getUser();
        if (user == null) {
            throw new IllegalStateException("User should not be null");
        }

        TicketControlEvent ticketControlEvent = new TicketControlEvent();
        ticketControlEvent.setStatus(TicketControlEvent.Status.CREATED);
        // Заполняем базовое событие
        Event event = eventBuilder.build();
        ticketControlEvent.setEvent(event);
        // Заполняем идентификатор билета
        TicketId ticketId = new TicketId();
        ticketId.setSaleDate(ticketControlData.getSaleDateTime());
        ticketId.setTicketNumber(ticketControlData.getTicketNumber());
        ticketId.setDeviceId(String.valueOf(ticketControlData.getDeviceId()));
        // Заполняем собственные поля посадки пассажира
        TicketBoarding ticketBoarding = new TicketBoarding();
        ticketBoarding.setTicketId(ticketId);
        ticketBoarding.setTrainNumber(ticketControlData.getTrainNumber());
        ticketBoarding.setTrainThreadId(null);
        ticketBoarding.setTerminalDeviceId(String.valueOf(ticketControlData.getDeviceId()));
        ticketBoarding.setOperatorName(user.getName());
        // В режиме offline считаем, что станция контроля = станции посадки из билета
        ticketBoarding.setControlStationCode(ticketControlData.getDepartureStationCode());
        ticketBoarding.setEdsKeyNumber(ticketControlData.getEdsKeyNumber());
        ticketBoarding.setEdsValid(ticketControlData.isEdsValid());
        ticketBoarding.setInWhiteList(ticketControlData.isInWhiteList());
        ticketBoarding.setBoardingByList(ticketControlData.getDataCarrierType() == DataCarrierType.TICKET_LIST);
        ticketBoarding.setCheckDate(event.getCreatedAt());
        ticketBoarding.setTicketDataId(null);
        // Заполняем информацию о посадке пассажира
        ticketControlEvent.setTicketBoarding(ticketBoarding);
        // Заполняем информацию о пассажире
        Passenger passenger = new Passenger();
        passenger.setFirstName(ticketControlData.getFirstName());
        passenger.setLastName(ticketControlData.getLastName());
        passenger.setMiddleName(ticketControlData.getSecondName());
        passenger.setDocumentTypeCode(ticketControlData.getDocumentTypeCode());
        passenger.setDocumentNumber(ticketControlData.getDocumentNumber());
        // Заполняем информацию о месте пассажира
        Location location = null;
        //http://agile.srvdev.ru/browse/CPPKPP-44331
        if (ticketControlData.getCarNumber() != null || ticketControlData.getSeatNumber() != null) {
            location = new Location();
            location.setCarNumber(ticketControlData.getCarNumber());
            location.setPlaceNumber(ticketControlData.getSeatNumber());
        }
        // Заполняем информацию об источнике данных
        TicketData ticketData = new TicketData();
        ticketData.setTicketTypeCode(ticketControlData.getTicketTypeCode());
        ticketData.setPassenger(passenger);
        ticketData.setLocation(location);
        ticketData.setDepartureStationCode(ticketControlData.getDepartureStationCode());
        ticketData.setDestinationStationCode(ticketControlData.getDestinationStationCode());
        ticketData.setDepartureDate(ticketControlData.getDepartureDates().get(0));
        ticketData.setExemptionExpressCode(ticketControlData.getExemptionExpressCode());
        // Заполняем информацию о смарт-карте
        if (ticketControlData.getDataCarrierType() == DataCarrierType.SMART_CARD) {
            SmartCard smartCard = new SmartCard();
            smartCard.setOuterNumber(ticketControlData.getCardOuterNumber());
            smartCard.setCrystalSerialNumber(ticketControlData.getCardCrystalSerialNumber());
            smartCard.setType(ticketControlData.getTicketStorageType());
            ticketData.setSmartCard(smartCard);
        } else {
            ticketData.setSmartCard(null);
        }

        // Заполняем информацию об источнике данных
        ticketBoarding.setTicketData(ticketData);
        // Заполняем информацию о версии НСИ
        // В будущем возможно неправильная логика, со слов Александра Корчака пока делаем так
        ticketData.setNsiVersion(nsiVersionProvider.getNsiVersionForDate(ticketControlData.getSaleDateTime()));

        return ticketControlEvent;
    }

}
