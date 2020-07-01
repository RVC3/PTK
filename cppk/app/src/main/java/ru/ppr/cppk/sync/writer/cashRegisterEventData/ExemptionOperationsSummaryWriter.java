package ru.ppr.cppk.sync.writer.cashRegisterEventData;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.cashRegisterEventData.ExemptionOperationsSummary;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class ExemptionOperationsSummaryWriter extends BaseWriter<ExemptionOperationsSummary> {

    private final FinanceOperationsSummaryWriter financeOperationsSummaryWriter;

    public ExemptionOperationsSummaryWriter(FinanceOperationsSummaryWriter financeOperationsSummaryWriter) {
        super();
        this.financeOperationsSummaryWriter = financeOperationsSummaryWriter;
    }


    @Override
    public void writeProperties(ExemptionOperationsSummary field, ExportJsonWriter writer) throws IOException {
        //Fields from FinanceOperationsSummary
        financeOperationsSummaryWriter.writeProperties(field, writer);

        //from ExemptionOperationsSummary
        writer.name("ExemptionLoss").value(field.exemptionLoss);
    }
}
