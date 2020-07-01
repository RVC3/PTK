package ru.ppr.cppk.reports;

import java.math.BigDecimal;

import ru.ppr.cppk.data.summary.MonthInfoStatisticsBuilder;
import ru.ppr.cppk.data.summary.PdStatisticsBuilder;
import ru.ppr.cppk.data.summary.ShiftInfoStatisticsBuilder;
import ru.ppr.cppk.data.summary.TicketTapeStatisticsBuilder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.printer.rx.operation.clearingSheet.ClearingSheetTpl;
import ru.ppr.cppk.printer.rx.operation.clearingSheet.PrintClearingSheetOperation;
import ru.ppr.cppk.printer.rx.operation.clearingSheet.SheetInfoTpl;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * Ведомость гашения смены
 * <p>
 * Ведомость гашения месяца
 *
 * @author Brazhkin A.V.
 */
public class ReportShiftOrMonthlyClearingSheet extends Report<ReportShiftOrMonthlyClearingSheet, PrintClearingSheetOperation.Result> {

    /**
     * Тип используемого отчета. По умолчанию "Ведомость гашения смены"
     */
    private ReportType sheetType = ReportType.SheetShiftBlanking;

    private ClearingSheetTpl.Params mainParams;

    private String shiftId;
    private String monthId;
    private boolean buildForLastShift;
    private boolean buildForLastMonth;
    /**
     * Информация о расходе билетной ленты.
     * Нужно устанавливать извне, если этой информации пока ещё нет в БД,
     * т.е. смена ещё не закрыта
     */
    private long paperConsumption;

    public ReportShiftOrMonthlyClearingSheet() {
        super();
    }

    /**
     * Задать тип отчета: "Сменная ведомость"
     */
    public ReportShiftOrMonthlyClearingSheet setSheetTypeShift() {
        this.sheetType = ReportType.SheetShiftBlanking;
        return this;
    }

    /**
     * Задать тип отчета: "Месячная ведомость"
     */
    public ReportShiftOrMonthlyClearingSheet setSheetTypeMonth() {
        this.sheetType = ReportType.SheetBlankingMonth;
        return this;
    }

    /**
     * Задать id смены
     */
    public ReportShiftOrMonthlyClearingSheet setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public ReportShiftOrMonthlyClearingSheet setBuildForLastShift() {
        this.buildForLastShift = true;
        return this;
    }

    /**
     * Задать id месяца
     */
    public ReportShiftOrMonthlyClearingSheet setMonthId(String monthId) {
        this.monthId = monthId;
        return this;
    }

    public ReportShiftOrMonthlyClearingSheet setBuildForLastMonth() {
        this.buildForLastMonth = true;
        return this;
    }

    /**
     * Задать расход ленты на конец смены (т.к. события о закрытии смены в момент печати ведомоости гашения ещё нет)
     *
     * @param paperConsumption Расход билетной ленты, мм
     */
    public ReportShiftOrMonthlyClearingSheet setPaperConsumption(long paperConsumption) {
        this.paperConsumption = paperConsumption;
        return this;
    }

    @Override
    public ReportShiftOrMonthlyClearingSheet build() throws Exception {

        mainParams = new ClearingSheetTpl.Params();
        mainParams.clicheParams = buildClicheParams();

        ShiftInfoStatisticsBuilder.Statistics shiftInfoStatistics = null;
        if (shiftId != null || buildForLastShift) {
            shiftInfoStatistics = new ShiftInfoStatisticsBuilder()
                    .setShiftId(shiftId)
                    .setBuildForLastShift(buildForLastShift)
                    .build();
        }

        MonthInfoStatisticsBuilder.Statistics monthInfoStatistics = null;
        if (monthId != null || buildForLastMonth) {
            monthInfoStatistics = new MonthInfoStatisticsBuilder()
                    .setMonthId(monthId)
                    .setBuildForLastMonth(buildForLastMonth)
                    .build();
        }

        PdStatisticsBuilder.Statistics pdStatistics = new PdStatisticsBuilder(getNsiVersionManager().getCurrentNsiVersionId())
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .setMonthId(monthId)
                .setBuildForLastMonth(buildForLastMonth)
                .build();

        TicketTapeStatisticsBuilder.Statistics ticketTapeStatistics = new TicketTapeStatisticsBuilder(localDaoSession)
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .setMonthId(monthId)
                .setBuildForLastMonth(buildForLastMonth)
                .build();

        /**
         * Данные по отчету
         */
        SheetInfoTpl.Params sheetInfo = new SheetInfoTpl.Params();
        mainParams.sheetInfo = sheetInfo;
        if (sheetType == ReportType.SheetBlankingMonth) {
            sheetInfo.monthNum = monthInfoStatistics.monthNum;
        } else {
            sheetInfo.shiftNum = shiftInfoStatistics.shiftNum;
        }

        /**
         * Продажи
         */
        sheetInfo.sum = pdStatistics.monthCountAndProfit.profit.total;
        sheetInfo.clearingCount = 0;
        sheetInfo.clearingSum = BigDecimal.ZERO;

        /**
         * Сумма в ФР
         */
        if (sheetType == ReportType.SheetShiftBlanking) {
            sheetInfo.cashInFR = shiftInfoStatistics.cashInFR == null ? getCashInFR() : shiftInfoStatistics.cashInFR;
        }

        /**
         * Расход ленты за смену
         */
        long paperConsumption = ticketTapeStatistics.paperConsumption == null ? this.paperConsumption : ticketTapeStatistics.paperConsumption;
        sheetInfo.consumptionOfTape = Decimals.divide(new BigDecimal(paperConsumption), new BigDecimal("1000"));

        /**
         * Количество оставшихся свободных записей в фискальной памяти
         */
        sheetInfo.availableRecordsInFRCount = 0;
        return this;

    }

    public ClearingSheetTpl.Params getMainParams() {
        return mainParams;
    }

    @Override
    protected Observable<PrintClearingSheetOperation.Result> printImpl(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory()
                .getPrintClearingSheetOperation(mainParams).call();
    }

    @Override
    protected Observable<Void> addPrintReportEventObservable(PrintClearingSheetOperation.Result printResult) {
        return addPrintReportEventObservable(sheetType, printResult.getOperationTime());
    }
}
