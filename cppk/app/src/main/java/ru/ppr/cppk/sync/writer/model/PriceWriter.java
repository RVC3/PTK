package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.Price;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class PriceWriter extends BaseWriter<Price> {

    @Override
    public void writeProperties(Price price, ExportJsonWriter writer) throws IOException {
        writer.name("Full").value(price.Full);
        writer.name("Nds").value(price.Nds);
        writer.name("Payed").value(price.Payed);
        writer.name("SummForReturn").value(price.SummForReturn);
        writer.name("NdsPercent").value(price.NdsPercent);
    }

}
