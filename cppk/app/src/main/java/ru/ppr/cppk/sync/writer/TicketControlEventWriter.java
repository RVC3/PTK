package ru.ppr.cppk.sync.writer;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.CPPKTicketControl;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.baseEntities.CashRegisterEventWriter;
import ru.ppr.cppk.sync.writer.baseEntities.EventWriter;
import ru.ppr.cppk.sync.writer.baseEntities.TicketEventBaseWriter;
import ru.ppr.cppk.sync.writer.model.CashRegisterWriter;
import ru.ppr.cppk.sync.writer.model.CashierWriter;
import ru.ppr.cppk.sync.writer.model.SmartCardWriter;
import ru.ppr.cppk.sync.writer.model.StationDeviceWriter;
import ru.ppr.cppk.sync.writer.model.StationWriter;
import ru.ppr.cppk.sync.writer.model.TariffWriter;
import ru.ppr.cppk.sync.writer.model.WorkingShiftWriter;

/**
 * @author Aleksandr Brazhkin
 */
public class TicketControlEventWriter extends BaseWriter<CPPKTicketControl> {

    private final DateFormatter dateFormatter;
    private final EventWriter eventWriter;
    private final CashRegisterEventWriter cashRegisterEventWriter;
    private final TicketEventBaseWriter ticketEventBaseWriter;

    public TicketControlEventWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
        SmartCardWriter smartCardWriter = new SmartCardWriter(dateFormatter);
        StationDeviceWriter stationDeviceWriter = new StationDeviceWriter();
        StationWriter stationWriter = new StationWriter();
        TariffWriter tariffWriter = new TariffWriter();
        CashierWriter cashierWriter = new CashierWriter();
        CashRegisterWriter cashRegisterWriter = new CashRegisterWriter();
        WorkingShiftWriter workingShiftWriter = new WorkingShiftWriter(dateFormatter);
        eventWriter = new EventWriter(stationDeviceWriter, stationWriter);
        cashRegisterEventWriter = new CashRegisterEventWriter(cashierWriter, cashRegisterWriter, workingShiftWriter);
        ticketEventBaseWriter = new TicketEventBaseWriter(dateFormatter, smartCardWriter, stationWriter, tariffWriter);
    }

    @Override
    public void writeProperties(CPPKTicketControl cppkTicketControl, ExportJsonWriter writer) throws IOException {
        //Fields From event
        eventWriter.writeProperties(cppkTicketControl, writer);

        //From CashRegisterEvent
        cashRegisterEventWriter.writeProperties(cppkTicketControl, writer);

        //Fields from TicketEventBase
        ticketEventBaseWriter.writeProperties(cppkTicketControl, writer);

        //From CPPKTicketControl
        writer.name("ControlDateTime").value(dateFormatter.formatDateForExport(cppkTicketControl.ControlDateTime));
        writer.name("EdsKeyNumber").value(cppkTicketControl.EdsKeyNumber);
        writer.name("IsRevokedEds").value(cppkTicketControl.IsRevokedEds);
        writer.name("ExemptionCode").value(cppkTicketControl.ExemptionCode);
        writer.name("ValidationResult").value(cppkTicketControl.ValidationResult);
        writer.name("TripsSpend").value(cppkTicketControl.TripsSpend);
        writer.name("Trips7000Spend").value(cppkTicketControl.trips7000Spend);
        writer.name("TripsCount").value(cppkTicketControl.tripsCount);
        writer.name("Trips7000Count").value(cppkTicketControl.trips7000Count);
        writer.name("SellTicketDeviceId").value(cppkTicketControl.SellTicketDeviceId);
        writer.name("IsRestoredTicket").value(cppkTicketControl.isRestoredTicket);
        writer.name("DeparturePoint").value(cppkTicketControl.DeparturePoint);
        writer.name("DestinationPoint").value(cppkTicketControl.DestinationPoint);
        writer.name("TransferDepartureDateTime").value(dateFormatter.formatDateForExport(cppkTicketControl.TransferDepartureDateTime));
    }

}
