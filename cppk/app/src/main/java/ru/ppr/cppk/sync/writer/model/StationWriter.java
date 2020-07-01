package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.Station;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Aleksandr Brazhkin
 */
public class StationWriter extends BaseWriter<Station> {

    @Override
    public void writeProperties(Station station, ExportJsonWriter writer) throws IOException {
        writer.name("Code").value(station.Code);
        writer.name("Name").value(station.Name);
        writer.name("ShortName").value(station.ShortName);
    }
}
