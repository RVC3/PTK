package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.PreTicket;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class PreTicketWriter extends BaseWriter<PreTicket> {

    private final DateFormatter dateFormatter;
    private final StationWriter stationWriter;

    public PreTicketWriter(DateFormatter dateFormatter, StationWriter stationWriter) {
        this.dateFormatter = dateFormatter;
        this.stationWriter = stationWriter;
    }

    @Override
    public void writeProperties(PreTicket preTicket, ExportJsonWriter writer) throws IOException {
        writer.name("PreTicketNumber").value(preTicket.preTicketNumber);
        writer.name("PrintDateTime").value(dateFormatter.formatDateForExport(preTicket.printDateTime));
        writer.name("DeviceId").value(preTicket.deviceId);
        stationWriter.writeField("Station", preTicket.station, writer);
    }

}
