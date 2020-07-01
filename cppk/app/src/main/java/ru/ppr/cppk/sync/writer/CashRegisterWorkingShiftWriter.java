package ru.ppr.cppk.sync.writer;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.CashRegisterWorkingShift;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.baseEntities.CashRegisterEventWriter;
import ru.ppr.cppk.sync.writer.baseEntities.EventWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.ClosureStatisticsWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.FeeExemptionOperationsSummaryWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.FeeTaxOperationsSummaryWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.FinanceOperationsSummaryWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.ShiftClosureStatisticsWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.TaxOperationsSummaryWriter;
import ru.ppr.cppk.sync.writer.model.CashRegisterWriter;
import ru.ppr.cppk.sync.writer.model.CashierWriter;
import ru.ppr.cppk.sync.writer.model.EventsStatisticWriter;
import ru.ppr.cppk.sync.writer.model.StationDeviceWriter;
import ru.ppr.cppk.sync.writer.model.StationWriter;
import ru.ppr.cppk.sync.writer.model.WorkingShiftWriter;

/**
 * @author Grigoriy Kashka
 */
public class CashRegisterWorkingShiftWriter extends BaseWriter<CashRegisterWorkingShift> {

    private final DateFormatter dateFormatter;
    private final EventWriter eventWriter;
    private final CashRegisterEventWriter cashRegisterEventWriter;
    private final EventsStatisticWriter eventsStatisticWriter;
    private final ShiftClosureStatisticsWriter shiftClosureStatisticsWriter;

    public CashRegisterWorkingShiftWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
        StationDeviceWriter stationDeviceWriter = new StationDeviceWriter();
        StationWriter stationWriter = new StationWriter();
        CashierWriter cashierWriter = new CashierWriter();
        CashRegisterWriter cashRegisterWriter = new CashRegisterWriter();
        WorkingShiftWriter workingShiftWriter = new WorkingShiftWriter(dateFormatter);
        this.eventWriter = new EventWriter(stationDeviceWriter, stationWriter);
        this.cashRegisterEventWriter = new CashRegisterEventWriter(cashierWriter, cashRegisterWriter, workingShiftWriter);
        this.eventsStatisticWriter = new EventsStatisticWriter();
        FinanceOperationsSummaryWriter financeOperationsSummaryWriter = new FinanceOperationsSummaryWriter();
        TaxOperationsSummaryWriter taxOperationsSummaryWriter = new TaxOperationsSummaryWriter(financeOperationsSummaryWriter);
        FeeTaxOperationsSummaryWriter feeTaxOperationsSummaryWriter = new FeeTaxOperationsSummaryWriter(taxOperationsSummaryWriter);
        FeeExemptionOperationsSummaryWriter feeExemptionOperationsSummaryWriter = new FeeExemptionOperationsSummaryWriter(feeTaxOperationsSummaryWriter);
        ClosureStatisticsWriter closureStatisticsWriter = new ClosureStatisticsWriter(feeExemptionOperationsSummaryWriter, feeTaxOperationsSummaryWriter, taxOperationsSummaryWriter);
        this.shiftClosureStatisticsWriter = new ShiftClosureStatisticsWriter(closureStatisticsWriter);
    }

    @Override
    public void writeProperties(CashRegisterWorkingShift field, ExportJsonWriter writer) throws IOException {

        //Fields From event
        eventWriter.writeProperties(field, writer);

        //From CashRegisterEvent
        cashRegisterEventWriter.writeProperties(field, writer);

        //From CashRegisterWorkingShift
        writer.name("ShiftEndDateTime").value(dateFormatter.formatDateForExport(field.shiftEndDateTime));
        writer.name("OperationDateTime").value(dateFormatter.formatDateForExport(field.operationDateTime));
        writer.name("Status").value(field.status);
        eventsStatisticWriter.writeField("EventsStatistic", field.eventsStatistic, writer);
        shiftClosureStatisticsWriter.writeField("ShiftClosureStatistics", field.shiftClosureStatistics, writer);
        writer.name("PaperCounterHasRestarted").value(field.paperCounterHasRestarted);
    }

}
