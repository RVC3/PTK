package ru.ppr.cppk.sync.writer.baseEntities;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.baseEntities.Event;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.model.StationDeviceWriter;
import ru.ppr.cppk.sync.writer.model.StationWriter;

/**
 * @author Grigoriy Kashka
 */
public class EventWriter extends BaseWriter<Event> {

    private final StationDeviceWriter stationDeviceWriter;
    private final StationWriter stationWriter;

    public EventWriter(
            StationDeviceWriter stationDeviceWriter,
            StationWriter stationWriter) {
        this.stationDeviceWriter = stationDeviceWriter;
        this.stationWriter = stationWriter;
    }

    @Override
    public void writeProperties(Event event, ExportJsonWriter writer) throws IOException {
        writer.name("Id").value(event.Id);
        writer.name("CreationTimestamp").value(event.CreationTimestamp);
        writer.name("VersionId").value(event.VersionId);
        writer.name("SoftwareVersion").value(event.SoftwareVersion);
        stationDeviceWriter.writeField("Device", event.Device, writer);
        stationWriter.writeField("Station", event.Station, writer);
    }
}
