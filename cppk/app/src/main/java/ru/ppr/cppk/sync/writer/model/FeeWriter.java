package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.Fee;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class FeeWriter extends BaseWriter<Fee> {

    @Override
    public void writeProperties(Fee fee, ExportJsonWriter writer) throws IOException {
        writer.name("Total").value(fee.Total);
        writer.name("Nds").value(fee.Nds);
        writer.name("FeeType").value(fee.FeeType);
        writer.name("NdsPercent").value(fee.NdsPercent);
    }
}
