package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.EventsStatistic;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class EventsStatisticWriter extends BaseWriter<EventsStatistic> {
    @Override
    public void writeProperties(EventsStatistic field, ExportJsonWriter writer) throws IOException {
        writer.name("TicketControlEventsCount").value(field.ticketControlEventsCount);
        writer.name("TicketAnnulledEventsCount").value(field.ticketAnnulledEventsCount);
        writer.name("TicketSaleEventsCount").value(field.ticketSaleEventsCount);
        writer.name("TestTicketsEventsCount").value(field.testTicketsEventsCount);
        writer.name("ShiftEventsCount").value(field.shiftEventsCount);
        writer.name("BankTransactionCashRegisterEventsCount").value(field.bankTransactionCashRegisterEventsCount);
        writer.name("TicketPaperRollEventsCountsCount").value(field.ticketPaperRollEventsCountsCount);
        writer.name("FinePaidEventsCount").value(field.finePaidEventsCount);
        writer.name("ServiceTicketControlEventsCount").value(field.serviceTicketControlEventsCount);
    }
}
