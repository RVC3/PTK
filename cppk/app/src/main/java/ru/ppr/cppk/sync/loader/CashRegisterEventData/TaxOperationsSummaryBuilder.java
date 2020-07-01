package ru.ppr.cppk.sync.loader.CashRegisterEventData;


import ru.ppr.cppk.data.summary.FineSaleStatisticsBuilder;
import ru.ppr.cppk.sync.kpp.cashRegisterEventData.TaxOperationsSummary;

/**
 * @author Grigoriy Kashka
 */
public class TaxOperationsSummaryBuilder {

    public TaxOperationsSummary build(FineSaleStatisticsBuilder.Statistics statistics) {
        TaxOperationsSummary taxOperationsSummary = new TaxOperationsSummary();
        taxOperationsSummary.amount = statistics.countAndProfit.profit.total;
        taxOperationsSummary.count = statistics.countAndProfit.count.totalCount;
        taxOperationsSummary.tax = statistics.countAndProfit.profit.totalTax;
        return taxOperationsSummary;
    }
}
