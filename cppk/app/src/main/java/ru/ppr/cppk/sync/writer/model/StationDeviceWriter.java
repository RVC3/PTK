package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.StationDevice;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Aleksandr Brazhkin
 */
public class StationDeviceWriter extends BaseWriter<StationDevice> {

    @Override
    public void writeProperties(StationDevice stationDevice, ExportJsonWriter writer) throws IOException {
        writer.name("Id").value(stationDevice.id);
        writer.name("Model").value(stationDevice.model);
        writer.name("SerialNumber").value(stationDevice.serialNumber);
        writer.name("Type").value(stationDevice.type);
        writer.name("ProductionSectionCode").value(stationDevice.productionSectionCode);
    }
}
