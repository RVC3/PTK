package ru.ppr.cppk.sync.loader.CashRegisterEventData;

import ru.ppr.cppk.data.summary.PdStatisticsBuilder;
import ru.ppr.cppk.sync.kpp.cashRegisterEventData.FeeTaxOperationsSummary;

/**
 * @author Grigoriy Kashka
 */
public class FeeTaxOperationsSummaryBuilder {

    public FeeTaxOperationsSummary build(PdStatisticsBuilder.CountAndProfit countAndProfit) {
        FeeTaxOperationsSummary feeTaxOperationsSummary = new FeeTaxOperationsSummary();
        feeTaxOperationsSummary.amount = countAndProfit.profit.total.subtract(countAndProfit.profit.totalRepeal);
        feeTaxOperationsSummary.count = countAndProfit.count.totalCount - countAndProfit.count.totalRepealCount;
        feeTaxOperationsSummary.tax = countAndProfit.profit.totalVat.subtract(countAndProfit.profit.totalVatRepeal);
        feeTaxOperationsSummary.fee = countAndProfit.profit.fee.subtract(countAndProfit.profit.feeRepeal);
        feeTaxOperationsSummary.feeTax = countAndProfit.profit.feeVat.subtract(countAndProfit.profit.feeVatRepeal);
        return feeTaxOperationsSummary;
    }
}
