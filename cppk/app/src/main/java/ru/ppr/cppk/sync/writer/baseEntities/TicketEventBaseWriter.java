package ru.ppr.cppk.sync.writer.baseEntities;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.baseEntities.TicketEventBase;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.model.SmartCardWriter;
import ru.ppr.cppk.sync.writer.model.StationWriter;
import ru.ppr.cppk.sync.writer.model.TariffWriter;

/**
 * @author Grigoriy Kashka
 */
public class TicketEventBaseWriter extends BaseWriter<TicketEventBase> {

    private final DateFormatter dateFormatter;
    private final SmartCardWriter smartCardWriter;
    private final StationWriter stationWriter;
    private final TariffWriter tariffWriter;

    public TicketEventBaseWriter(DateFormatter dateFormatter,
                                 SmartCardWriter smartCardWriter,
                                 StationWriter stationWriter,
                                 TariffWriter tariffWriter) {
        this.dateFormatter = dateFormatter;
        this.smartCardWriter = smartCardWriter;
        this.stationWriter = stationWriter;
        this.tariffWriter = tariffWriter;
    }

    @Override
    public void writeProperties(TicketEventBase ticketEventBase, ExportJsonWriter writer) throws IOException {
        writer.name("TicketNumber").value(ticketEventBase.TicketNumber);
        writer.name("SaleDateTime").value(dateFormatter.formatDateForExport(ticketEventBase.SaleDateTime));
        writer.name("ValidFromDateTime").value(dateFormatter.formatDateForExport(ticketEventBase.ValidFromDateTime));
        writer.name("ValidTillDateTime").value(dateFormatter.formatDateForExport(ticketEventBase.ValidTillDateTime));
        stationWriter.writeField("DepartureStation", ticketEventBase.DepartureStation, writer);
        stationWriter.writeField("DestinationStation", ticketEventBase.DestinationStation, writer);
        tariffWriter.writeField("Tariff", ticketEventBase.Tariff, writer);
        writer.name("WayType").value(ticketEventBase.WayType);
        writer.name("Type").value(ticketEventBase.Type);
        writer.name("TypeCode").value(ticketEventBase.TypeCode);
        smartCardWriter.writeField("SmartCard", ticketEventBase.SmartCard, writer);
    }
}
