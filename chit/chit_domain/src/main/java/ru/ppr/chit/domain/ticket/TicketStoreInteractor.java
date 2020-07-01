package ru.ppr.chit.domain.ticket;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;
import ru.ppr.chit.domain.model.local.PassengerPersonalData;
import ru.ppr.chit.domain.model.local.PlaceLocation;
import ru.ppr.chit.domain.model.local.Ticket;
import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.repository.local.PassengerPersonalDataRepository;
import ru.ppr.chit.domain.repository.local.PlaceLocationRepository;
import ru.ppr.chit.domain.repository.local.TicketIdRepository;
import ru.ppr.chit.domain.repository.local.TicketRepository;

/**
 * Помещать сущность {@link ru.ppr.chit.domain.model.local.Ticket} в репозиторий
 * нужно исключительно с помощью этого класса, т.к. он имеет вложенные сущности,
 * для которых отключен маппинг, данный класс каскадно вставит все вложенные сущности
 *
 * @author Dmitry Nevolin
 */
public class TicketStoreInteractor {

    private final TicketRepository ticketRepository;
    private final TicketIdRepository ticketIdRepository;
    private final PassengerPersonalDataRepository passengerPersonalDataRepository;
    private final PlaceLocationRepository placeLocationRepository;
    private final LocalDbTransaction localDbTransaction;
    private final TicketManager ticketManager;

    @Inject
    TicketStoreInteractor(TicketRepository ticketRepository,
                          TicketIdRepository ticketIdRepository,
                          PassengerPersonalDataRepository passengerPersonalDataRepository,
                          PlaceLocationRepository placeLocationRepository,
                          LocalDbTransaction localDbTransaction,
                          TicketManager ticketManager) {
        this.ticketRepository = ticketRepository;
        this.ticketIdRepository = ticketIdRepository;
        this.passengerPersonalDataRepository = passengerPersonalDataRepository;
        this.placeLocationRepository = placeLocationRepository;
        this.localDbTransaction = localDbTransaction;
        this.ticketManager = ticketManager;
    }

    public void store(@NonNull Ticket ticket) {
        try {
            localDbTransaction.begin();
            TicketId ticketId = ticket.getTicketId(ticketIdRepository);
            TicketId storedTicketId = ticketIdRepository.loadByIdentity(ticketId.getTicketNumber(), ticketId.getSaleDate(), ticketId.getDeviceId());
            if (storedTicketId != null) {
                Ticket storedTicket = ticketRepository.loadByTicket(storedTicketId.getId());
                // Просто апдейт
                if (storedTicket != null) {
                    prepareTicket(storedTicket, ticket);
                    ticketRepository.update(storedTicket);
                } else {
                    // Айдишник существует, но самого тикета нет - такое теортетически возможно
                    ticket.setTicketIdId(storedTicketId.getId());
                    prepareTicket(ticket, null);
                    ticketRepository.insert(ticket);
                }
            } else {
                ticket.setTicketIdId(ticketIdRepository.insert(ticket.getTicketId(ticketIdRepository)));
                prepareTicket(ticket, null);
                ticketRepository.insert(ticket);
            }
            localDbTransaction.commit();
            ticketManager.clearCache();
        } finally {
            localDbTransaction.end();
        }
    }

    public void storeAll(@NonNull List<Ticket> ticketList) {
        try {
            localDbTransaction.begin();
            List<Ticket> insertTicketList = new ArrayList<>();
            for (Ticket ticket : ticketList) {
                TicketId ticketId = ticket.getTicketId(ticketIdRepository);
                TicketId storedTicketId = ticketIdRepository.loadByIdentity(ticketId.getTicketNumber(), ticketId.getSaleDate(), ticketId.getDeviceId());
                if (storedTicketId != null) {
                    Ticket storedTicket = ticketRepository.loadByTicket(storedTicketId.getId());
                    // Просто апдейт
                    if (storedTicket != null) {
                        prepareTicket(storedTicket, ticket);
                        ticketRepository.update(storedTicket);
                    } else {
                        // Айдишник существует, но самого тикета нет - такое теортетически возможно
                        ticket.setTicketIdId(storedTicketId.getId());
                        prepareTicket(ticket, null);
                        insertTicketList.add(ticket);
                    }
                } else {
                    ticket.setTicketIdId(ticketIdRepository.insert(ticket.getTicketId(ticketIdRepository)));
                    prepareTicket(ticket, null);
                    insertTicketList.add(ticket);
                }
            }
            if (!insertTicketList.isEmpty()) {
                ticketRepository.insertAll(insertTicketList);
            }
            localDbTransaction.commit();
            ticketManager.clearCache();
        } finally {
            localDbTransaction.end();
        }
    }

    private void prepareTicket(@NonNull Ticket ticket, @Nullable Ticket newTicket) {
        if (newTicket != null) {
            PassengerPersonalData passenger = newTicket.getPassenger(passengerPersonalDataRepository);
            if (passenger != null) {
                ticket.setPassengerId(passengerPersonalDataRepository.insert(passenger));
            }
            PlaceLocation placeLocation = newTicket.getPlaceLocation(placeLocationRepository);
            if (placeLocation != null) {
                ticket.setPlaceLocationId(placeLocationRepository.insert(placeLocation));
            }
            PlaceLocation oldPlaceLocation = newTicket.getOldPlaceLocation(placeLocationRepository);
            if (oldPlaceLocation != null) {
                ticket.setOldPlaceLocationId(placeLocationRepository.insert(oldPlaceLocation));
            }
            ticket.setDepartureStationCode(newTicket.getDepartureStationCode());
            ticket.setDestinationStationCode(newTicket.getDestinationStationCode());
            ticket.setDepartureDate(newTicket.getDepartureDate());
            ticket.setTicketTypeCode(newTicket.getTicketTypeCode());
            ticket.setExemptionExpressCode(newTicket.getExemptionExpressCode());
            ticket.setTicketIssueType(newTicket.getTicketIssueType());
            ticket.setTicketState(newTicket.getTicketState());
            ticket.setNsiVersion(newTicket.getNsiVersion());
        } else {
            PassengerPersonalData passenger = ticket.getPassenger(passengerPersonalDataRepository);
            if (passenger != null) {
                ticket.setPassengerId(passengerPersonalDataRepository.insert(passenger));
            }
            PlaceLocation placeLocation = ticket.getPlaceLocation(placeLocationRepository);
            if (placeLocation != null) {
                ticket.setPlaceLocationId(placeLocationRepository.insert(placeLocation));
            }
            PlaceLocation oldPlaceLocation = ticket.getOldPlaceLocation(placeLocationRepository);
            if (oldPlaceLocation != null) {
                ticket.setOldPlaceLocationId(placeLocationRepository.insert(oldPlaceLocation));
            }
        }
    }

}
