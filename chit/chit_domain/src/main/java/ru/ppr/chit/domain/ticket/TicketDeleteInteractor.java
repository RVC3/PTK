package ru.ppr.chit.domain.ticket;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.PassengerPersonalData;
import ru.ppr.chit.domain.model.local.PlaceLocation;
import ru.ppr.chit.domain.model.local.Ticket;
import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.repository.local.PassengerPersonalDataRepository;
import ru.ppr.chit.domain.repository.local.PlaceLocationRepository;
import ru.ppr.chit.domain.repository.local.TicketIdRepository;
import ru.ppr.chit.domain.repository.local.TicketRepository;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;

/**
 * Удалять сущность {@link ru.ppr.chit.domain.model.local.Ticket} из репозитория
 * нужно исключительно с помощью этого класса, т.к. он имеет вложенные сущности,
 * для которых отключен маппинг, данный класс каскадно удалит все вложенные сущности
 *
 * @author Dmitry Nevolin
 */
public class TicketDeleteInteractor {

    private final TicketRepository ticketRepository;
    private final TicketIdRepository ticketIdRepository;
    private final PassengerPersonalDataRepository passengerPersonalDataRepository;
    private final PlaceLocationRepository placeLocationRepository;
    private final LocalDbTransaction localDbTransaction;
    private final TicketManager ticketManager;

    @Inject
    TicketDeleteInteractor(TicketRepository ticketRepository,
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

    public void delete(@NonNull Ticket ticket) {
        try {
            localDbTransaction.begin();
            deleteCascade(ticket);
            localDbTransaction.commit();
            ticketManager.clearCache();
        } finally {
            localDbTransaction.end();
        }
    }

    public void deleteAll() {
        try {
            localDbTransaction.begin();
            List<Ticket> ticketList = ticketManager.getTicketList();
            for (Ticket ticket : ticketList) {
                deleteCascade(ticket);
            }
            localDbTransaction.commit();
            ticketManager.clearCache();
        } finally {
            localDbTransaction.end();
        }
    }

    /**
     * Удаляет билет из базы и каскадно все связанные с ним сущности (кроме TicketId)
     * (Нелья удалять TicketId, т.к. на них ссылаются другие объекты)
     *
     * @param ticket билет для удаления
     */
    private void deleteCascade(@NonNull Ticket ticket) {
        PassengerPersonalData passenger = ticket.getPassenger(passengerPersonalDataRepository);
        if (passenger != null) {
            passengerPersonalDataRepository.delete(passenger);
        }
        PlaceLocation placeLocation = ticket.getPlaceLocation(placeLocationRepository);
        if (placeLocation != null) {
            placeLocationRepository.delete(placeLocation);
        }
        PlaceLocation oldPlaceLocation = ticket.getOldPlaceLocation(placeLocationRepository);
        if (oldPlaceLocation != null) {
            placeLocationRepository.delete(oldPlaceLocation);
        }
        ticketRepository.delete(ticket);
    }

}
