package ru.ppr.cppk.sync.writer.cashRegisterEventData;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.cashRegisterEventData.ClosureStatistics;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class ClosureStatisticsWriter extends BaseWriter<ClosureStatistics> {

    private final FeeExemptionOperationsSummaryWriter feeExemptionOperationsSummaryWriter;
    private final FeeTaxOperationsSummaryWriter feeTaxOperationsSummaryWriter;
    private final TaxOperationsSummaryWriter taxOperationsSummaryWriter;

    public ClosureStatisticsWriter(FeeExemptionOperationsSummaryWriter feeExemptionOperationsSummaryWriter,
                                   FeeTaxOperationsSummaryWriter feeTaxOperationsSummaryWriter,
                                   TaxOperationsSummaryWriter taxOperationsSummaryWriter) {
        super();
        this.feeExemptionOperationsSummaryWriter = feeExemptionOperationsSummaryWriter;
        this.feeTaxOperationsSummaryWriter = feeTaxOperationsSummaryWriter;
        this.taxOperationsSummaryWriter = taxOperationsSummaryWriter;
    }

    @Override
    public void writeProperties(ClosureStatistics field, ExportJsonWriter writer) throws IOException {

        feeExemptionOperationsSummaryWriter.writeField("TwoWayChildTicketSaleExceptAnnulled", field.twoWayChildTicketSaleExceptAnnulled, writer);
        feeExemptionOperationsSummaryWriter.writeField("OneWayTicketSaleExceptAnnulled", field.oneWayTicketSaleExceptAnnulled, writer);
        feeExemptionOperationsSummaryWriter.writeField("TwoWayTicketSaleExceptAnnulled", field.twoWayTicketSaleExceptAnnulled, writer);

        writer.name("TotalSmartCardExceptAnnulled").value(field.totalSmartCardExceptAnnulled);

        feeExemptionOperationsSummaryWriter.writeField("OneWayChildTicketSaleExceptAnnulled", field.oneWayChildTicketSaleExceptAnnulled, writer);

        writer.name("TapeLength").value(field.tapeLength);
        writer.name("FinishedTapesCount").value(field.finishedTapesCount);

        feeTaxOperationsSummaryWriter.writeField("LuggageTicketExceptAnnulled", field.luggageTicketExceptAnnulled, writer);

        taxOperationsSummaryWriter.writeField("FinesPaid", field.finesPaid, writer);
        feeExemptionOperationsSummaryWriter.writeField("Transfers", field.transfers, writer);

        writer.name("FeesTax").value(field.feesTax);
        writer.name("FeesAmount").value(field.feesAmount);
        writer.name("TotalIncome").value(field.totalIncome);
        writer.name("TotalIncomeExceptAnnulled").value(field.totalIncomeExceptAnnulled);
        writer.name("TotalCashExceptAnnulled").value(field.totalCashExceptAnnulled);

    }
}
