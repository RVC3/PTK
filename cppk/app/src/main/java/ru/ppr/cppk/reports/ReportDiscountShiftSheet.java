package ru.ppr.cppk.reports;

import java.math.BigDecimal;
import java.util.Map;

import ru.ppr.cppk.data.summary.PdStatisticsBuilder;
import ru.ppr.cppk.data.summary.ShiftInfoStatisticsBuilder;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.printer.rx.operation.discountShiftSheet.ExemptionInfoTpl;
import ru.ppr.cppk.printer.rx.operation.discountShiftSheet.PrintDiscountShiftSheetOperation;
import ru.ppr.cppk.printer.rx.operation.discountShiftSheet.SheetInfoTpl;
import ru.ppr.cppk.printer.rx.operation.discountShiftSheet.ShiftSheetTpl;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ExemptionGroup;
import rx.Observable;

/**
 * Льготная сменная ведомость
 *
 * @author Brazhkin A.V.
 */
public class ReportDiscountShiftSheet extends Report<ReportDiscountShiftSheet, PrintDiscountShiftSheetOperation.Result> {

    private ShiftSheetTpl.Params mainParams;

    private String shiftId;
    private boolean buildForLastShift;

    /**
     * Задать id смены
     */
    public ReportDiscountShiftSheet setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public ReportDiscountShiftSheet setBuildForLastShift() {
        this.buildForLastShift = true;
        return this;
    }

    @Override
    public ReportDiscountShiftSheet build() throws Exception {

        mainParams = new ShiftSheetTpl.Params();
        mainParams.clicheParams = buildClicheParams();

        ShiftInfoStatisticsBuilder.Statistics shiftInfoStatistics = new ShiftInfoStatisticsBuilder()
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .build();

        PdStatisticsBuilder.Statistics pdStatistics = new PdStatisticsBuilder(getNsiVersionManager().getCurrentNsiVersionId())
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .build();

        /**
         * Данные по отчету
         */
        mainParams.sheetInfo = new SheetInfoTpl.Params();
        mainParams.sheetInfo.shiftNum = shiftInfoStatistics.shiftNum;
        mainParams.lossSum = pdStatistics.countAndProfit.profit.lossSum;
        mainParams.totalRepealCount = pdStatistics.countAndProfit.count.totalRepealCount;
        mainParams.lossSumAnnulled = pdStatistics.countAndProfit.profit.lossSumRepeal;

        /**
         * Продажи
         */
        mainParams.exemptionRoutes = new ShiftSheetTpl.ExemptionRoutes();

        for (Map.Entry<Integer, PdStatisticsBuilder.RouteStatistics> routeEntry : pdStatistics.routesStatistics.entrySet()) {
            int routeNumber = routeEntry.getKey();
            for (Map.Entry<ExemptionGroup, PdStatisticsBuilder.ExemptionGroupStatistics> exemptionGroupEntry : routeEntry.getValue().exemptionGroupsStatistics.entrySet()) {
                ExemptionGroup exemptionGroup = exemptionGroupEntry.getKey();
                String exemptionGroupName = exemptionGroup.getGroupName();

                PdStatisticsBuilder.ExemptionGroupStatistics exemptionGroupStatistics = exemptionGroupEntry.getValue();

                for (Map.Entry<Exemption, PdStatisticsBuilder.ExemptionStatistics> exemptionEntry : exemptionGroupStatistics.exemptionsStatistics.entrySet()) {
                    PdStatisticsBuilder.ExemptionStatistics exemptionStatistics = exemptionEntry.getValue();
                    if (exemptionStatistics.countAndProfit.count.totalCount == exemptionStatistics.countAndProfit.count.totalRepealCount) {
                        // Все аннулированны, не нужно выводить в отчет
                        continue;
                    }
                    Exemption exemption = exemptionEntry.getKey();
                    ExemptionInfoTpl.Params exemptionInfo = new ExemptionInfoTpl.Params();
                    exemptionInfo.number = exemption.getExemptionExpressCode();
                    exemptionInfo.totalCount = exemptionStatistics.countAndProfit.count.totalCount;
                    exemptionInfo.totalCountAnnulled = exemptionStatistics.countAndProfit.count.totalRepealCount;
                    exemptionInfo.childCount = exemptionStatistics.childCountAndProfit.count.totalCount;
                    exemptionInfo.childCountAnnulled = exemptionStatistics.childCountAndProfit.count.totalRepealCount;
                    exemptionInfo.lossSum = exemptionStatistics.countAndProfit.profit.lossSum;
                    exemptionInfo.lossSumAnnulled = exemptionStatistics.countAndProfit.profit.lossSumRepeal;

                    ShiftSheetTpl.ExemptionGroups exemptionGroups = null;
                    if (mainParams.exemptionRoutes.containsKey(routeNumber)) {
                        exemptionGroups = mainParams.exemptionRoutes.get(routeNumber);
                    } else {
                        exemptionGroups = new ShiftSheetTpl.ExemptionGroups();
                        mainParams.exemptionRoutes.put(routeNumber, exemptionGroups);
                    }

                    ShiftSheetTpl.Exemptions exemptions = null;
                    if (exemptionGroups.containsKey(exemptionGroupName)) {
                        exemptions = exemptionGroups.get(exemptionGroupName);
                    } else {
                        exemptions = new ShiftSheetTpl.Exemptions();
                        exemptionGroups.put(exemptionGroupName, exemptions);
                        exemptions.totalCountExceptAnnulled =  exemptionGroupStatistics.countAndProfit.count.totalCount - exemptionGroupStatistics.countAndProfit.count.totalRepealCount;
                        exemptions.lossSumExceptAnnulled = exemptionGroupStatistics.countAndProfit.profit.lossSum.subtract(exemptionGroupStatistics.countAndProfit.profit.lossSumRepeal);
                    }

                    exemptions.put(exemptionInfo.number, exemptionInfo);
                }
            }
        }

        return this;

    }

    @Override
    protected Observable<PrintDiscountShiftSheetOperation.Result> printImpl(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory()
                .getPrintDiscountShiftSheetOperation(mainParams).call();
    }

    @Override
    protected Observable<Void> addPrintReportEventObservable
            (PrintDiscountShiftSheetOperation.Result printResult) {
        return addPrintReportEventObservable(ReportType.DiscountedShiftShit, printResult.getOperationTime());
    }
}
