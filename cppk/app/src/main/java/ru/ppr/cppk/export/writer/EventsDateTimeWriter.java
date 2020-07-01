package ru.ppr.cppk.export.writer;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ppr.cppk.export.model.EventsDateTime;

/**
 * Конвертер в json для модельки {@link EventsDateTime}
 *
 * @author Grigoriy Kashka
 */
public class EventsDateTimeWriter {

    public JSONObject getJson(EventsDateTime eventsDateTime) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("ShiftEvents", eventsDateTime.shiftEvents);
        json.put("TicketControls", eventsDateTime.ticketControls);
        json.put("TicketSales", eventsDateTime.ticketSales);
        json.put("TestTickets", eventsDateTime.testTickets);
        json.put("TicketReturns", eventsDateTime.ticketReturns);
        json.put("MonthClosures", eventsDateTime.monthClosures);
        json.put("TicketPaperRolls", eventsDateTime.ticketPaperRolls);
        json.put("BankTransactions", eventsDateTime.bankTransactions);
        json.put("TicketReSigns", eventsDateTime.ticketReSigns);
        json.put("ServiceSales", eventsDateTime.serviceSales);
        json.put("FinePaidEvents", eventsDateTime.finePaidEvents);
        json.put("ServiceTicketControls", eventsDateTime.serviceTicketControls);
        return json;
    }
}
