package ru.ppr.cppk.sync.writer.cashRegisterEventData;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.cashRegisterEventData.FeeTaxOperationsSummary;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class FeeTaxOperationsSummaryWriter extends BaseWriter<FeeTaxOperationsSummary> {

    private final TaxOperationsSummaryWriter taxOperationsSummaryWriter;

    public FeeTaxOperationsSummaryWriter(TaxOperationsSummaryWriter taxOperationsSummaryWriter) {
        this.taxOperationsSummaryWriter = taxOperationsSummaryWriter;
    }

    @Override
    public void writeProperties(FeeTaxOperationsSummary field, ExportJsonWriter writer) throws IOException {
        //fron TaxOperationsSummary
        taxOperationsSummaryWriter.writeProperties(field, writer);
        //from FeeTaxOperationsSummary
        writer.name("Fee").value(field.fee);
        writer.name("FeeTax").value(field.feeTax);
    }
}
