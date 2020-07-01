package ru.ppr.cppk.sync.writer.cashRegisterEventData;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.cashRegisterEventData.ShiftClosureStatistics;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class ShiftClosureStatisticsWriter extends BaseWriter<ShiftClosureStatistics> {

    private final ClosureStatisticsWriter closureStatisticsWriter;

    public ShiftClosureStatisticsWriter(ClosureStatisticsWriter closureStatisticsWriter) {
        this.closureStatisticsWriter = closureStatisticsWriter;
    }


    @Override
    public void writeProperties(ShiftClosureStatistics field, ExportJsonWriter writer) throws IOException {
        //from ClosureStatistics
        closureStatisticsWriter.writeProperties(field, writer);

        //from ShiftClosureStatistics
        writer.name("TotalEventsCount").value(field.totalEventsCount);
        writer.name("FirstDocumentNumber").value(field.firstDocumentNumber);
        writer.name("LastDocumentNumber").value(field.lastDocumentNumber);
        writer.name("CurrentTapeLengthInMillimeters").value(field.currentTapeLengthInMillimeters);
    }
}
