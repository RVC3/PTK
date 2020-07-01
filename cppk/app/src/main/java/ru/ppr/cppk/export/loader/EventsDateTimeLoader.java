package ru.ppr.cppk.export.loader;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ppr.cppk.export.model.EventsDateTime;

/**
 * Класс Loader позволяющий получить объект {@link EventsDateTime} из json-а
 * Пока не используется из-за того что касса не может переделать структуру класса {@link ru.ppr.cppk.export.model.request.GetEventsReq} смотри {@link GetEventsReqLoader}
 *
 * @author Grigoriy Kashka
 */
public class EventsDateTimeLoader {
    public EventsDateTime fromJson(JSONObject json) throws JSONException {
        final EventsDateTime eventsDateTime = new EventsDateTime();
        eventsDateTime.shiftEvents = json.getLong("ShiftEvents");
        eventsDateTime.ticketControls = json.getLong("TicketControls");
        eventsDateTime.ticketSales = json.getLong("TicketSales");
        eventsDateTime.testTickets = json.getLong("TestTickets");
        eventsDateTime.ticketReturns = json.getLong("TicketReturns");
        eventsDateTime.monthClosures = json.getLong("MonthClosures");
        eventsDateTime.ticketPaperRolls = json.getLong("TicketPaperRolls");
        eventsDateTime.bankTransactions = json.getLong("BankTransactions");
        eventsDateTime.ticketReSigns = json.getLong("TicketReSigns");
        eventsDateTime.serviceSales = json.getLong("ServiceSales");
        eventsDateTime.finePaidEvents = json.getLong("FinePaidEvents");
        eventsDateTime.serviceTicketControls = json.getLong("ServiceTicketControls");
        return eventsDateTime;
    }
}
