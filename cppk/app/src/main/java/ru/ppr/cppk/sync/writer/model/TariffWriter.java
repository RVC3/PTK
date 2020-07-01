package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.Tariff;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Aleksandr Brazhkin
 */
public class TariffWriter extends BaseWriter<Tariff> {

    @Override
    public void writeProperties(Tariff tariff, ExportJsonWriter writer) throws IOException {
        writer.name("TariffCode").value(tariff.TariffCode);
        writer.name("TariffPlanCode").value(tariff.TariffPlanCode);
        writer.name("RouteCode").value(tariff.RouteCode);
    }
}
