package ru.ppr.chit.domain.ticketcontrol;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.Location;
import ru.ppr.chit.domain.model.local.Passenger;
import ru.ppr.chit.domain.model.local.SmartCard;
import ru.ppr.chit.domain.model.local.TicketBoarding;
import ru.ppr.chit.domain.model.local.TicketControlEvent;
import ru.ppr.chit.domain.model.local.TicketData;
import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.LocationRepository;
import ru.ppr.chit.domain.repository.local.PassengerRepository;
import ru.ppr.chit.domain.repository.local.SmartCardRepository;
import ru.ppr.chit.domain.repository.local.TicketBoardingRepository;
import ru.ppr.chit.domain.repository.local.TicketControlEventRepository;
import ru.ppr.chit.domain.repository.local.TicketDataRepository;
import ru.ppr.chit.domain.repository.local.TicketIdRepository;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;

/**
 * @author Aleksandr Brazhkin
 */
public class StoreCreatedTicketControlEventInteractor {

    private final TicketControlEventRepository ticketControlEventRepository;
    private final EventRepository eventRepository;
    private final TicketIdRepository ticketIdRepository;
    private final TicketBoardingRepository ticketBoardingRepository;
    private final TicketDataRepository ticketDataRepository;
    private final PassengerRepository passengerRepository;
    private final LocationRepository locationRepository;
    private final SmartCardRepository smartCardRepository;
    private final LocalDbTransaction localDbTransaction;

    @Inject
    StoreCreatedTicketControlEventInteractor(TicketControlEventRepository ticketControlEventRepository,
                                             EventRepository eventRepository,
                                             TicketIdRepository ticketIdRepository,
                                             TicketBoardingRepository ticketBoardingRepository,
                                             TicketDataRepository ticketDataRepository,
                                             PassengerRepository passengerRepository,
                                             LocationRepository locationRepository,
                                             SmartCardRepository smartCardRepository,
                                             LocalDbTransaction localDbTransaction) {
        this.ticketControlEventRepository = ticketControlEventRepository;
        this.eventRepository = eventRepository;
        this.ticketIdRepository = ticketIdRepository;
        this.ticketBoardingRepository = ticketBoardingRepository;
        this.ticketDataRepository = ticketDataRepository;
        this.passengerRepository = passengerRepository;
        this.locationRepository = locationRepository;
        this.smartCardRepository = smartCardRepository;
        this.localDbTransaction = localDbTransaction;
    }

    public void store(TicketControlEvent ticketControlEvent) {
        try {
            localDbTransaction.begin();

            // Добавляем в БД базовое событие
            Event event = ticketControlEvent.getEvent(eventRepository);
            eventRepository.insert(event);
            ticketControlEvent.setEventId(event.getId());

            TicketBoarding ticketBoarding = ticketControlEvent.getTicketBoarding(ticketBoardingRepository);

            // Добавляем в БД id билета
            TicketId ticketId = ticketBoarding.getTicketId(ticketIdRepository);
            ticketBoarding.setTicketIdId(storeTicketId(ticketId));

            // Добавляем в БД информацию об источнике данных
            TicketData ticketData = ticketBoarding.getTicketData(ticketDataRepository);
            ticketBoarding.setTicketDataId(storeTicketData(ticketData));

            // Добавляем в БД информацию о посадке
            ticketBoardingRepository.insert(ticketBoarding);
            ticketControlEvent.setTicketBoardingId(ticketBoarding.getId());

            // Добавляем в БД событие контроля
            ticketControlEventRepository.insert(ticketControlEvent);

            localDbTransaction.commit();
        } finally {
            localDbTransaction.end();
        }
    }

    private Long storeTicketId(TicketId ticketId) {
        TicketId ticketIdInDb = ticketIdRepository.loadByIdentity(ticketId.getTicketNumber(), ticketId.getSaleDate(), ticketId.getDeviceId());
        if (ticketIdInDb == null) {
            ticketIdRepository.insert(ticketId);
            return ticketId.getId();
        }
        return ticketIdInDb.getId();
    }

    private Long storeTicketData(@Nullable TicketData ticketData) {
        if (ticketData == null) {
            return null;
        }
        // Добавляем в БД  информацию о пассажире
        Passenger passenger = ticketData.getPassenger(passengerRepository);
        ticketData.setPassengerId(storePassenger(passenger));

        // Добавляем в БД информацию о месте пассажира
        Location location = ticketData.getLocation(locationRepository);
        ticketData.setLocationId(storeLocation(location));

        // Добавляем в БД информацию о смарт-карте
        SmartCard smartCard = ticketData.getSmartCard(smartCardRepository);
        ticketData.setSmartCardId(storeSmartCard(smartCard));

        // Добавляем в БД  информацию об источнике данных
        ticketDataRepository.insert(ticketData);
        return ticketData.getId();
    }

    private Long storePassenger(@NonNull Passenger passenger) {
        // Добавляем в БД информацию о пассажире
        passengerRepository.insert(passenger);
        return passenger.getId();
    }

    private Long storeLocation(@Nullable Location location) {
        if (location == null) {
            return null;
        }
        // Добавляем в БД информацию о месте пассажира
        locationRepository.insert(location);
        return location.getId();
    }

    private Long storeSmartCard(@Nullable SmartCard smartCard) {
        if (smartCard == null) {
            return null;
        }
        // Добавляем в БД информацию о смарт-карте
        smartCardRepository.insert(smartCard);
        return smartCard.getId();
    }
}
