package ru.ppr.cppk.sync.writer.cashRegisterEventData;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.cashRegisterEventData.TaxOperationsSummary;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class TaxOperationsSummaryWriter extends BaseWriter<TaxOperationsSummary> {

    private final FinanceOperationsSummaryWriter financeOperationsSummaryWriter;

    public TaxOperationsSummaryWriter(FinanceOperationsSummaryWriter financeOperationsSummaryWriter) {
        super();
        this.financeOperationsSummaryWriter = financeOperationsSummaryWriter;
    }

    @Override
    public void writeProperties(TaxOperationsSummary field, ExportJsonWriter writer) throws IOException {
        //from FinanceOperationsSummary
        financeOperationsSummaryWriter.writeProperties(field, writer);
        //from TaxOperationsSummary
        writer.name("Tax").value(field.tax);
    }
}
