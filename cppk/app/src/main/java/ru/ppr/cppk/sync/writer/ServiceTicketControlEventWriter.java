package ru.ppr.cppk.sync.writer;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.ServiceTicketControlEvent;
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
public class ServiceTicketControlEventWriter extends BaseWriter<ServiceTicketControlEvent> {

    private final DateFormatter dateFormatter;
    private final EventWriter eventWriter;
    private final CashRegisterEventWriter cashRegisterEventWriter;

    public ServiceTicketControlEventWriter(DateFormatter dateFormatter) {
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
    public void writeProperties(ServiceTicketControlEvent serviceTicketControlEvent, ExportJsonWriter writer) throws IOException {
        //Fields From event
        eventWriter.writeProperties(serviceTicketControlEvent, writer);

        //From CashRegisterEvent
        cashRegisterEventWriter.writeProperties(serviceTicketControlEvent, writer);

        //From ServiceTicketControlEvent
        writer.name("ControlDateTime").value(dateFormatter.formatDateForExport(serviceTicketControlEvent.controlDateTime));
        writer.name("EDSKeyNumber").value(serviceTicketControlEvent.edsKeyNumber);
        writer.name("StopListId").value(serviceTicketControlEvent.stopListId);
        writer.name("ValidationResult").value(serviceTicketControlEvent.validationResult);
        writer.name("CardNumber").value(serviceTicketControlEvent.cardNumber);
        writer.name("CardCristalId").value(serviceTicketControlEvent.cardCristalId);
        writer.name("CardType").value(serviceTicketControlEvent.cardType);
        writer.name("ValidFromUtc").value(dateFormatter.formatDateForExport(serviceTicketControlEvent.validFromUtc));
        writer.name("ValidToUtc").value(dateFormatter.formatDateForExport(serviceTicketControlEvent.validToUtc));
        writer.name("ZoneType").value(serviceTicketControlEvent.zoneType);
        writer.name("ZoneValue").value(serviceTicketControlEvent.zoneValue);
        writer.name("CanTravel").value(serviceTicketControlEvent.canTravel);
        writer.name("RequirePersonification").value(serviceTicketControlEvent.requirePersonification);
        writer.name("RequireCheckDocument").value(serviceTicketControlEvent.requireCheckDocument);
        writer.name("TicketNumber").value(serviceTicketControlEvent.ticketNumber);
        writer.name("TicketWriteDateTime").value(dateFormatter.formatDateForExport(serviceTicketControlEvent.ticketWriteDateTime));
        writer.name("SmartCardUsageCount").value(serviceTicketControlEvent.smartCardUsageCount);
        writer.name("PassageSign").value(serviceTicketControlEvent.passageSign);
        writer.name("TicketDeviceId").value(serviceTicketControlEvent.ticketDeviceId);
    }


}
