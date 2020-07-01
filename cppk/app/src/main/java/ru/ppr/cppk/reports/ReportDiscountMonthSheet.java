package ru.ppr.cppk.reports;

import java.util.Map;

import ru.ppr.cppk.data.summary.MonthInfoStatisticsBuilder;
import ru.ppr.cppk.data.summary.PdStatisticsBuilder;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.printer.rx.operation.discountMonthSheet.ExemptionInfoTpl;
import ru.ppr.cppk.printer.rx.operation.discountMonthSheet.MonthInfoTpl;
import ru.ppr.cppk.printer.rx.operation.discountMonthSheet.MonthSheetTpl;
import ru.ppr.cppk.printer.rx.operation.discountMonthSheet.MonthStateTpl;
import ru.ppr.cppk.printer.rx.operation.discountMonthSheet.PrintDiscountMonthSheetOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.nsi.entity.Exemption;
import rx.Observable;

/**
 * Льготная месячная ведомость
 *
 * @author Brazhkin A.V.
 */
public class ReportDiscountMonthSheet extends Report<ReportDiscountMonthSheet, PrintDiscountMonthSheetOperation.Result> {

    private MonthSheetTpl.Params mainParams;

    private String monthId;
    private boolean buildForLastMonth;

    public ReportDiscountMonthSheet() {
        super();
    }

    /**
     * Задать id месяца
     */
    public ReportDiscountMonthSheet setMonthId(String monthId) {
        this.monthId = monthId;
        return this;
    }

    public ReportDiscountMonthSheet setBuildForLastMonth() {
        this.buildForLastMonth = true;
        return this;
    }

    @Override
    public ReportDiscountMonthSheet build() throws Exception {

        mainParams = new MonthSheetTpl.Params();
        mainParams.clicheParams = buildClicheParams();

        MonthInfoStatisticsBuilder.Statistics monthInfoStatistics = new MonthInfoStatisticsBuilder()
                .setMonthId(monthId)
                .setBuildForLastMonth(buildForLastMonth)
                .setBuildForClosedShiftsOnly(true)
                .build();

        PdStatisticsBuilder.Statistics pdStatistics = new PdStatisticsBuilder(getNsiVersionManager().getCurrentNsiVersionId())
                .setMonthId(monthId)
                .setBuildForLastMonth(buildForLastMonth)
                .setBuildForClosedShiftsOnly(true)
                .build();

        /**
         * Данные по отчету
         */
        mainParams.sheetInfo = new MonthInfoTpl.Params();
        mainParams.sheetInfo.monthNum = monthInfoStatistics.monthNum;
        mainParams.exemptions = new MonthSheetTpl.Exemptions();
        mainParams.lossSum = pdStatistics.countAndProfit.profit.lossSum;
        mainParams.totalRepealCount = pdStatistics.countAndProfit.count.totalRepealCount;
        mainParams.lossSumAnnulled = pdStatistics.countAndProfit.profit.lossSumRepeal;

        // Состояние месяца
        MonthStateTpl.Params monthStates = new MonthStateTpl.Params();
        mainParams.monthStates = monthStates;
        if (monthInfoStatistics.firstShiftStatistics != null) {
            monthStates.startShiftNum = monthInfoStatistics.firstShiftStatistics.shiftNum;
            monthStates.startOpenShiftDate = monthInfoStatistics.firstShiftStatistics.fromDate;
            monthStates.startCloseShiftDate = monthInfoStatistics.firstShiftStatistics.toDate;
        }
        if (monthInfoStatistics.lastShiftStatistics != null) {
            monthStates.endShiftNum = monthInfoStatistics.lastShiftStatistics.shiftNum;
            monthStates.endOpenShiftDate = monthInfoStatistics.lastShiftStatistics.fromDate;
            monthStates.endCloseShiftDate = monthInfoStatistics.lastShiftStatistics.toDate;
        }
        monthStates.firstDocNumber = monthInfoStatistics.firstDocument == null ? null : monthInfoStatistics.firstDocument.number;
        monthStates.lastDocNumber = monthInfoStatistics.lastDocument == null ? null : monthInfoStatistics.lastDocument.number;

        /**
         * Продажи
         */
        for (Map.Entry<Exemption, PdStatisticsBuilder.ExemptionStatistics> entry : pdStatistics.exemptionsStatistics.entrySet()) {
            PdStatisticsBuilder.ExemptionStatistics exemptionStatistics = entry.getValue();
            if (exemptionStatistics.countAndProfit.count.totalCount == exemptionStatistics.countAndProfit.count.totalRepealCount) {
                // Все аннулированны, не нужно выводить в отчет
                continue;
            }
            Exemption exemption = entry.getKey();
            ExemptionInfoTpl.Params exemptionInfo = new ExemptionInfoTpl.Params();
            exemptionInfo.number = exemption.getExemptionExpressCode();
            exemptionInfo.totalCount = exemptionStatistics.countAndProfit.count.totalCount;
            exemptionInfo.totalCountAnnulled = exemptionStatistics.countAndProfit.count.totalRepealCount;
            exemptionInfo.childCount = exemptionStatistics.childCountAndProfit.count.totalCount;
            exemptionInfo.childCountAnnulled = exemptionStatistics.childCountAndProfit.count.totalRepealCount;
            exemptionInfo.lossSum = exemptionStatistics.countAndProfit.profit.lossSum;
            exemptionInfo.lossSumAnnulled = exemptionStatistics.countAndProfit.profit.lossSumRepeal;
            mainParams.exemptions.put(exemptionInfo.number, exemptionInfo);
        }
        return this;
    }

    @Override
    protected Observable<PrintDiscountMonthSheetOperation.Result> printImpl(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory()
                .getPrintDiscountMonthSheetOperation(mainParams).call();
    }

    @Override
    protected Observable<Void> addPrintReportEventObservable(PrintDiscountMonthSheetOperation.Result printResult) {
        return addPrintReportEventObservable(ReportType.DiscountedMonthlySheet, printResult.getOperationTime());
    }
}
