package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.WorkingShift;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Aleksandr Brazhkin
 */
public class WorkingShiftWriter extends BaseWriter<WorkingShift> {

    private final DateFormatter dateFormatter;

    public WorkingShiftWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    @Override
    public void writeProperties(WorkingShift field, ExportJsonWriter writer) throws IOException {
        writer.name("StartDateTime").value(dateFormatter.formatDateForExport(field.StartDateTime));
        writer.name("Number").value(field.Number);
    }
}
