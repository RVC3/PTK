package ru.ppr.cppk.sync.writer;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.CPPKTicketReSign;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.baseEntities.EventWriter;
import ru.ppr.cppk.sync.writer.model.StationDeviceWriter;
import ru.ppr.cppk.sync.writer.model.StationWriter;

/**
 * @author Grigoriy Kashka
 */
public class CPPKTicketReSignWriter extends BaseWriter<CPPKTicketReSign> {

    private final DateFormatter dateFormatter;
    private final EventWriter eventWriter;

    public CPPKTicketReSignWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
        StationDeviceWriter stationDeviceWriter = new StationDeviceWriter();
        StationWriter stationWriter = new StationWriter();
        eventWriter = new EventWriter(stationDeviceWriter, stationWriter);
    }

    @Override
    public void writeProperties(CPPKTicketReSign cppkTicketReSign, ExportJsonWriter writer) throws IOException {
        //Fields From event
        eventWriter.writeProperties(cppkTicketReSign, writer);

        //From CPPKTicketReSign
        writer.name("TicketNumber").value(cppkTicketReSign.ticketNumber);
        writer.name("SaleDateTime").value(dateFormatter.formatDateForExport(cppkTicketReSign.saleDateTime));
        writer.name("TicketDeviceId").value(cppkTicketReSign.ticketDeviceId);
        writer.name("EDSKeyNumber").value(cppkTicketReSign.edsKeyNumber);
        writer.name("ReSignDateTime").value(dateFormatter.formatDateForExport(cppkTicketReSign.reSignDateTime));
    }

}
