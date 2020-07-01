package ru.ppr.cppk.sync.writer;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.TicketPaperRollEvent;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.baseEntities.CashRegisterEventWriter;
import ru.ppr.cppk.sync.writer.baseEntities.EventWriter;
import ru.ppr.cppk.sync.writer.model.CashRegisterWriter;
import ru.ppr.cppk.sync.writer.model.CashierWriter;
import ru.ppr.cppk.sync.writer.model.StationDeviceWriter;
import ru.ppr.cppk.sync.writer.model.StationWriter;
import ru.ppr.cppk.sync.writer.model.WorkingShiftWriter;

/**
 * @author Grigoriy Kashka
 */
public class TicketPaperRollEventWriter extends BaseWriter<TicketPaperRollEvent> {

    private final DateFormatter dateFormatter;
    private final EventWriter eventWriter;
    private final CashRegisterEventWriter cashRegisterEventWriter;

    public TicketPaperRollEventWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
        StationDeviceWriter stationDeviceWriter = new StationDeviceWriter();
        StationWriter stationWriter = new StationWriter();
        CashierWriter cashierWriter = new CashierWriter();
        CashRegisterWriter cashRegisterWriter = new CashRegisterWriter();
        WorkingShiftWriter workingShiftWriter = new WorkingShiftWriter(dateFormatter);
        eventWriter = new EventWriter(stationDeviceWriter, stationWriter);
        cashRegisterEventWriter = new CashRegisterEventWriter(cashierWriter, cashRegisterWriter, workingShiftWriter);
    }

    @Override
    public void writeProperties(TicketPaperRollEvent ticketPaperRollEvent, ExportJsonWriter writer) throws IOException {
        //Fields From event
        eventWriter.writeProperties(ticketPaperRollEvent, writer);

        //From CashRegisterEvent
        cashRegisterEventWriter.writeProperties(ticketPaperRollEvent, writer);

        //From TicketPaperRollEvent
        writer.name("Action").value(ticketPaperRollEvent.action);
        writer.name("TicketNumber").value(ticketPaperRollEvent.ticketNumber);
        writer.name("LastTicketNumber").value(ticketPaperRollEvent.lastTicketNumber);
        writer.name("Series").value(ticketPaperRollEvent.series);
        writer.name("Number").value(ticketPaperRollEvent.number);
        writer.name("OperationDateTime").value(dateFormatter.formatDateForExport(ticketPaperRollEvent.operationDateTime));
        writer.name("TotalTicketsCount").value(ticketPaperRollEvent.totalTicketsCount);
        writer.name("TotalReportsCount").value(ticketPaperRollEvent.totalReportsCount);
        writer.name("PaperConsumption").value(ticketPaperRollEvent.paperConsumption);
        writer.name("DeviceLength").value(ticketPaperRollEvent.deviceLength);
        writer.name("TestTicketsCount").value(ticketPaperRollEvent.testTicketsCount);
        writer.name("TicketsCount").value(ticketPaperRollEvent.ticketsCount);
        writer.name("ServiceSaleReceiptsCount").value(ticketPaperRollEvent.serviceSaleReceiptsCount);
        writer.name("CancellationReceiptsCount").value(ticketPaperRollEvent.cancellationReceiptsCount);
        writer.name("TestShiftRegisterReportsCount").value(ticketPaperRollEvent.testShiftRegisterReportsCount);
        writer.name("ShiftRegisterReportsCount").value(ticketPaperRollEvent.shiftRegisterReportsCount);
        writer.name("EttRegisterReportsCount").value(ticketPaperRollEvent.ettRegisterReportsCount);
        writer.name("ExemptionShiftRegisterReportsCount").value(ticketPaperRollEvent.exemptionShiftRegisterReportsCount);
        writer.name("TestMonthRegisterReportsCount").value(ticketPaperRollEvent.testMonthRegisterReportsCount);
        writer.name("MonthRegisterReportsCount").value(ticketPaperRollEvent.monthRegisterReportsCount);
        writer.name("ExemptionMonthRegisterReportsCount").value(ticketPaperRollEvent.exemptionMonthRegisterReportsCount);
        writer.name("ControlRegisterReportsCount").value(ticketPaperRollEvent.controlRegisterReportsCount);
        writer.name("PaperCounterHasRestarted").value(ticketPaperRollEvent.paperCounterHasRestarted);
    }

}
