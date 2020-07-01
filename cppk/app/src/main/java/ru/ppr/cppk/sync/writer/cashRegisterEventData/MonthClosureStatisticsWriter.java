package ru.ppr.cppk.sync.writer.cashRegisterEventData;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.cashRegisterEventData.MonthClosureStatistics;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.model.WorkingShiftWriter;

/**
 * @author Grigoriy Kashka
 */
public class MonthClosureStatisticsWriter extends BaseWriter<MonthClosureStatistics> {

    private final ClosureStatisticsWriter closureStatisticsWriter;
    private final WorkingShiftWriter workingShiftWriter;

    public MonthClosureStatisticsWriter(ClosureStatisticsWriter closureStatisticsWriter, WorkingShiftWriter workingShiftWriter) {
        this.closureStatisticsWriter = closureStatisticsWriter;
        this.workingShiftWriter = workingShiftWriter;
    }

    @Override
    public void writeProperties(MonthClosureStatistics field, ExportJsonWriter writer) throws IOException {
        //from ClosureStatistics
        closureStatisticsWriter.writeProperties(field, writer);

        //from MonthClosureStatistics
        workingShiftWriter.writeField("MonthOpenShift", field.monthOpenShift, writer);
        workingShiftWriter.writeField("MonthCloseShift", field.monthCloseShift, writer);
    }
}
