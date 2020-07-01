package ru.ppr.cppk.export.loader;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ppr.cppk.export.model.request.GetEventsReq;

/**
 * Класс Loader для сущности {@link GetEventsReq}
 *
 * @author Grigoriy Kashka
 */
public class GetEventsReqLoader {

    public GetEventsReq fromJson(JSONObject json) throws JSONException {
        final GetEventsReq getEventsReq = new GetEventsReq();

        getEventsReq.lastTimestampsForEvent.shiftEvents = json.getLong("ShiftEvents");
        getEventsReq.lastTimestampsForEvent.ticketControls = json.getLong("TicketControls");
        getEventsReq.lastTimestampsForEvent.ticketSales = json.getLong("TicketSales");
        getEventsReq.lastTimestampsForEvent.testTickets = json.getLong("TestTickets");
        getEventsReq.lastTimestampsForEvent.ticketReturns = json.getLong("TicketReturns");
        getEventsReq.lastTimestampsForEvent.monthClosures = json.getLong("MonthClosures");
        getEventsReq.lastTimestampsForEvent.ticketPaperRolls = json.getLong("TicketPaperRolls");
        getEventsReq.lastTimestampsForEvent.bankTransactions = json.getLong("BankTransactions");
        getEventsReq.lastTimestampsForEvent.ticketReSigns = json.getLong("TicketReSigns");
        getEventsReq.lastTimestampsForEvent.serviceSales = json.getLong("ServiceSales");
        getEventsReq.lastTimestampsForEvent.finePaidEvents = json.getLong("FinePaidEvents");
        getEventsReq.lastTimestampsForEvent.serviceTicketControls = json.getLong("ServiceTicketControls");

        getEventsReq.lastTimestampsForSentEvent.shiftEvents = json.getLong("SentShiftEvents");
        getEventsReq.lastTimestampsForSentEvent.ticketControls = json.getLong("SentTicketControls");
        getEventsReq.lastTimestampsForSentEvent.ticketSales = json.getLong("SentTicketSales");
        getEventsReq.lastTimestampsForSentEvent.testTickets = json.getLong("SentTestTickets");
        getEventsReq.lastTimestampsForSentEvent.ticketReturns = json.getLong("SentTicketReturns");
        getEventsReq.lastTimestampsForSentEvent.monthClosures = json.getLong("SentMonthClosures");
        getEventsReq.lastTimestampsForSentEvent.ticketPaperRolls = json.getLong("SentTicketPaperRolls");
        getEventsReq.lastTimestampsForSentEvent.bankTransactions = json.getLong("SentBankTransactions");
        getEventsReq.lastTimestampsForSentEvent.ticketReSigns = json.getLong("SentTicketReSigns");
        getEventsReq.lastTimestampsForSentEvent.serviceSales = json.getLong("SentServiceSales");
        getEventsReq.lastTimestampsForSentEvent.finePaidEvents = json.getLong("SentFinePaidEvents");
        getEventsReq.lastTimestampsForSentEvent.serviceTicketControls = json.getLong("SentServiceTicketControls");

        return getEventsReq;
    }
}
