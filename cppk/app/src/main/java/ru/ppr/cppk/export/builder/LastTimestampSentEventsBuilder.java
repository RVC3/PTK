package ru.ppr.cppk.export.builder;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.export.model.EventsDateTime;
import ru.ppr.cppk.model.SentEvents;

/**
 * Билдер таймстемпов для всех событий полученных ЦОДом
 *
 * @author Grigoriy Kashka
 */
public class LastTimestampSentEventsBuilder {

    private final LocalDaoSession localDaoSession;

    public LastTimestampSentEventsBuilder(LocalDaoSession localDaoSession) {
        this.localDaoSession = localDaoSession;
    }

    public EventsDateTime build() {

        SentEvents sentEvents = localDaoSession.getSentEventsDao().load();

        EventsDateTime eventsDateTime = new EventsDateTime();

        eventsDateTime.shiftEvents = sentEvents.getSentShiftEvents();
        eventsDateTime.ticketControls = sentEvents.getSentTicketControls();
        eventsDateTime.ticketSales = sentEvents.getSentTicketSales();
        eventsDateTime.testTickets = sentEvents.getSentTestTickets();
        eventsDateTime.ticketReturns = sentEvents.getSentTicketReturns();
        eventsDateTime.monthClosures = sentEvents.getSentMonthClosures();
        eventsDateTime.ticketPaperRolls = sentEvents.getSentTicketPaperRolls();
        eventsDateTime.bankTransactions = sentEvents.getSentBankTransactions();
        eventsDateTime.ticketReSigns = sentEvents.getSentTicketReSigns();
        eventsDateTime.serviceSales = sentEvents.getSentServiceSales();
        eventsDateTime.finePaidEvents = sentEvents.getSentFinePaidEvents();
        eventsDateTime.serviceTicketControls = sentEvents.getSentServiceTicketControls();

        return eventsDateTime;
    }


}
