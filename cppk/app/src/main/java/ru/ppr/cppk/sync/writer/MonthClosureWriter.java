package ru.ppr.cppk.sync.writer;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.MonthClosure;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.baseEntities.CashRegisterEventWriter;
import ru.ppr.cppk.sync.writer.baseEntities.EventWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.ClosureStatisticsWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.FeeExemptionOperationsSummaryWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.FeeTaxOperationsSummaryWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.FinanceOperationsSummaryWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.MonthClosureStatisticsWriter;
import ru.ppr.cppk.sync.writer.cashRegisterEventData.TaxOperationsSummaryWriter;
import ru.ppr.cppk.sync.writer.model.CashRegisterWriter;
import ru.ppr.cppk.sync.writer.model.CashierWriter;
import ru.ppr.cppk.sync.writer.model.StationDeviceWriter;
import ru.ppr.cppk.sync.writer.model.StationWriter;
import ru.ppr.cppk.sync.writer.model.WorkingShiftWriter;

/**
 * @author Grigoriy Kashka
 */
public class MonthClosureWriter extends BaseWriter<MonthClosure> {

    private final DateFormatter dateFormatter;
    private final EventWriter eventWriter;
    private final CashRegisterEventWriter cashRegisterEventWriter;
    private final MonthClosureStatisticsWriter monthClosureStatisticsWriter;

    public MonthClosureWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
        StationDeviceWriter stationDeviceWriter = new StationDeviceWriter();
        StationWriter stationWriter = new StationWriter();
        CashierWriter cashierWriter = new CashierWriter();
        CashRegisterWriter cashRegisterWriter = new CashRegisterWriter();
        WorkingShiftWriter workingShiftWriter = new WorkingShiftWriter(dateFormatter);
        this.eventWriter = new EventWriter(stationDeviceWriter, stationWriter);
        this.cashRegisterEventWriter = new CashRegisterEventWriter(cashierWriter, cashRegisterWriter, workingShiftWriter);
        FinanceOperationsSummaryWriter financeOperationsSummaryWriter = new FinanceOperationsSummaryWriter();
        TaxOperationsSummaryWriter taxOperationsSummaryWriter = new TaxOperationsSummaryWriter(financeOperationsSummaryWriter);
        FeeTaxOperationsSummaryWriter feeTaxOperationsSummaryWriter = new FeeTaxOperationsSummaryWriter(taxOperationsSummaryWriter);
        FeeExemptionOperationsSummaryWriter feeExemptionOperationsSummaryWriter = new FeeExemptionOperationsSummaryWriter(feeTaxOperationsSummaryWriter);
        ClosureStatisticsWriter closureStatisticsWriter = new ClosureStatisticsWriter(feeExemptionOperationsSummaryWriter, feeTaxOperationsSummaryWriter, taxOperationsSummaryWriter);
        this.monthClosureStatisticsWriter = new MonthClosureStatisticsWriter(closureStatisticsWriter, workingShiftWriter);
    }

    @Override
    public void writeProperties(MonthClosure field, ExportJsonWriter writer) throws IOException {

        //Fields From event
        eventWriter.writeProperties(field, writer);

        //From CashRegisterEvent
        cashRegisterEventWriter.writeProperties(field, writer);

        //From MonthClosure
        writer.name("ClosureDateTime").value(dateFormatter.formatDateForExport(field.closureDateTime));
        writer.name("MonthNumber").value(field.monthNumber);
        monthClosureStatisticsWriter.writeField("Statistics", field.statistics, writer);
    }

}
