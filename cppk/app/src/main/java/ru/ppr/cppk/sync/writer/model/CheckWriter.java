package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.Check;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class CheckWriter extends BaseWriter<Check> {

    private final DateFormatter dateFormatter;

    public CheckWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    @Override
    public void writeProperties(Check check, ExportJsonWriter writer) throws IOException {
        writer.name("Number").value(check.Number);
        writer.name("AdditionalInfo").value(check.AdditionalInfo);
        writer.name("PrintDateTime").value(dateFormatter.formatDateForExport(check.PrintDateTime));
    }

}
