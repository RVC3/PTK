package ru.ppr.cppk.reports;

import ru.ppr.cppk.data.summary.BTMonthStatisticsBuilder;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.BtMonthReportTpl;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.PrintBtMonthReportOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * Created by Dmitry Nevolin on 17.02.2016.
 */
public class ReportBTMonthlySheet extends Report<ReportBTMonthlySheet, PrintBtMonthReportOperation.Result> {

    private ReportType sheetType = ReportType.BTMonthlySheet;

    private String monthId;
    private boolean buildForLastMonth;

    private BtMonthReportTpl.Params params;

    /**
     * Задать id месяца
     */
    public ReportBTMonthlySheet setMonthId(String monthId) {
        this.monthId = monthId;

        return this;
    }

    /**
     * Устанавливает флаг формирования отчёта за последнйи месяц. Если задан, то setMonthId(String monthId) будет проигнорирован
     */
    public ReportBTMonthlySheet buildForLastMonth() {
        this.buildForLastMonth = true;

        return this;
    }

    @Override
    protected ReportBTMonthlySheet build() throws Exception {

        BTMonthStatisticsBuilder btMonthStatisticsBuilder = BTMonthStatisticsBuilder.newBuilder()
                .setBuildForLastMonth(buildForLastMonth)
                .setMonthId(monthId)
                .build();

        params = new BtMonthReportTpl.Params();
        params.setClicheParams(buildClicheParams());
        params.setTerminalNumber(btMonthStatisticsBuilder.getTerminalNumber());
        params.setMonthNumber(btMonthStatisticsBuilder.getMonthNumber());
        params.setTransactionsQuantity(btMonthStatisticsBuilder.getTransactions().getQuantity());
        params.setTransactionsTotal(btMonthStatisticsBuilder.getTransactions().getTotal());
        params.setCancelsQuantity(btMonthStatisticsBuilder.getCancels().getQuantity());
        params.setCancelsTotal(btMonthStatisticsBuilder.getCancels().getTotal());
        params.setTransactionsWithoutSaleQuantity(btMonthStatisticsBuilder.getTransactionsWithoutSale().getQuantity());
        params.setTransactionsWithoutSaleTotal(btMonthStatisticsBuilder.getTransactionsWithoutSale().getTotal());
        params.setTransactionsWithoutSaleAndCancellationQuantity(btMonthStatisticsBuilder.getTransactionsWithoutSaleAndCancellation().getQuantity());
        params.setTransactionsWithoutSaleAndCancellationTotal(btMonthStatisticsBuilder.getTransactionsWithoutSaleAndCancellation().getTotal());
        params.setCompletedTransactionsQuantity(btMonthStatisticsBuilder.getCompletedTransactions().getQuantity());
        params.setCompletedTransactionsTotal(btMonthStatisticsBuilder.getCompletedTransactions().getTotal());

        return this;
    }

    @Override
    protected Observable<PrintBtMonthReportOperation.Result> printImpl(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory()
                .getPrintBtMonthReportOperation(params).call();
    }

    @Override
    protected Observable<Void> addPrintReportEventObservable(PrintBtMonthReportOperation.Result printResult) {
        return addPrintReportEventObservable(sheetType, printResult.getOperationTime());
    }

}
