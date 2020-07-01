package ru.ppr.cppk.export.builder;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.repository.ServiceTicketControlEventRepository;
import ru.ppr.cppk.export.model.EventsDateTime;

/**
 * Билдер последних таймстемпов для всех событий
 *
 * @author Grigoriy Kashka
 */
public class LastTimestampEventsBuilder {

    private final LocalDaoSession localDaoSession;
    private final ServiceTicketControlEventRepository serviceTicketControlEventRepository;

    public LastTimestampEventsBuilder(LocalDaoSession localDaoSession,
                                      ServiceTicketControlEventRepository serviceTicketControlEventRepository) {
        this.localDaoSession = localDaoSession;
        this.serviceTicketControlEventRepository = serviceTicketControlEventRepository;
    }

    public EventsDateTime build() {
        EventsDateTime eventsDateTime = new EventsDateTime();

        eventsDateTime.shiftEvents = localDaoSession.getShiftEventDao().getLastShiftEventCreationTimeStamp();
        eventsDateTime.ticketControls = localDaoSession.getCppkTicketControlsDao().getLastControlEventCreationTimeStamp();
        eventsDateTime.ticketSales = localDaoSession.getCppkTicketSaleDao().getLastSaleEventCreationTimeStamp();
        eventsDateTime.testTickets = localDaoSession.getTestTicketDao().getLastTestTicketEventCreationTimeStamp();
        eventsDateTime.ticketReturns = localDaoSession.getCppkTicketReturnDao().getLastTicketReturnEventCreationTimeStamp();
        eventsDateTime.monthClosures = localDaoSession.getMonthEventDao().getLastMonthEventCreationTimeStamp();
        eventsDateTime.ticketPaperRolls = localDaoSession.getTicketTapeEventDao().getLastTicketPaperRollEventCreationTimeStamp();
        eventsDateTime.bankTransactions = localDaoSession.getBankTransactionDao().getLastBankTransactionEventCreationTimeStamp();
        eventsDateTime.ticketReSigns = localDaoSession.getCppkTicketReSignDao().getLastTicketReSignCreationTimeStamp();
        eventsDateTime.serviceSales = localDaoSession.getCppkServiceSaleDao().getLastServiceSaleEventCreationTimeStamp();
        eventsDateTime.finePaidEvents = localDaoSession.getFineSaleEventDao().getLastFinePaidEventCreationTimeStamp();
        eventsDateTime.serviceTicketControls = serviceTicketControlEventRepository.getLastServiceTicketControlCreationTimeStamp();

        return eventsDateTime;
    }
}
