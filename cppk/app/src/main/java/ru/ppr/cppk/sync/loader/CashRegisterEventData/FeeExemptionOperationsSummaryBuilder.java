package ru.ppr.cppk.sync.loader.CashRegisterEventData;

import ru.ppr.cppk.data.summary.PdStatisticsBuilder;
import ru.ppr.cppk.sync.kpp.cashRegisterEventData.FeeExemptionOperationsSummary;

/**
 * @author Grigoriy Kashka
 */
public class FeeExemptionOperationsSummaryBuilder {

    public FeeExemptionOperationsSummary build(PdStatisticsBuilder.CountAndProfit countAndProfit) {
        FeeExemptionOperationsSummary feeExemptionOperationsSummary = new FeeExemptionOperationsSummary();
        feeExemptionOperationsSummary.amount = countAndProfit.profit.total.subtract(countAndProfit.profit.totalRepeal);
        feeExemptionOperationsSummary.count = countAndProfit.count.totalCount - countAndProfit.count.totalRepealCount;
        feeExemptionOperationsSummary.exemptionLoss = countAndProfit.profit.lossSum.subtract(countAndProfit.profit.lossSumRepeal);
        feeExemptionOperationsSummary.fee = countAndProfit.profit.fee.subtract(countAndProfit.profit.feeRepeal);
        feeExemptionOperationsSummary.feeTax = countAndProfit.profit.feeVat.subtract(countAndProfit.profit.feeVatRepeal);
        feeExemptionOperationsSummary.tax = countAndProfit.profit.totalVat.subtract(countAndProfit.profit.totalVatRepeal);
        return feeExemptionOperationsSummary;
    }

}
