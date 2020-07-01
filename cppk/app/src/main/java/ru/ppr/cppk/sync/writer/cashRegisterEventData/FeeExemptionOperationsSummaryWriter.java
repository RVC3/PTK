package ru.ppr.cppk.sync.writer.cashRegisterEventData;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.cashRegisterEventData.FeeExemptionOperationsSummary;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class FeeExemptionOperationsSummaryWriter extends BaseWriter<FeeExemptionOperationsSummary> {

    private final FeeTaxOperationsSummaryWriter feeTaxOperationsSummaryWriter;

    public FeeExemptionOperationsSummaryWriter(FeeTaxOperationsSummaryWriter feeTaxOperationsSummaryWriter) {
        super();
        this.feeTaxOperationsSummaryWriter = feeTaxOperationsSummaryWriter;
    }


    @Override
    public void writeProperties(FeeExemptionOperationsSummary field, ExportJsonWriter writer) throws IOException {
        //from FeeTaxOperationsSummaryWriter
        feeTaxOperationsSummaryWriter.writeProperties(field, writer);

        //from FeeExemptionOperationsSummary
        writer.name("ExemptionLoss").value(field.exemptionLoss);
    }
}
