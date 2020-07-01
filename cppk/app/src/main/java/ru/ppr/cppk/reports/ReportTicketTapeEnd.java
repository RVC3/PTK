package ru.ppr.cppk.reports;

import java.math.BigDecimal;

import ru.ppr.cppk.data.summary.TicketTapeStatisticsBuilder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.printer.rx.operation.tapeEndReport.PrintTapeEndReportOperation;
import ru.ppr.cppk.printer.rx.operation.tapeEndReport.TapeEndReportTpl;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * Отчет "Окончание билетной ленты"
 *
 * @author Brazhkin A.V.
 */
public class ReportTicketTapeEnd extends Report<ReportTicketTapeEnd, PrintTapeEndReportOperation.Result> {

    private TapeEndReportTpl.Params params;

    private String ticketTapeId;
    private boolean buildForLastTicketTape;
    /**
     * Информация о расходе билетной ленты.
     * Нужно устанавливать извне, если этой информации пока ещё нет в БД,
     * т.е. события изъятия билетной ленты пока нет
     */
    private long paperConsumption;

    public ReportTicketTapeEnd() {
        super();
    }

    /**
     * Задать id бобины
     */
    public ReportTicketTapeEnd setTicketTapeId(String ticketTapeId) {
        this.ticketTapeId = ticketTapeId;
        return this;
    }

    public ReportTicketTapeEnd setBuildForLastTicketTape() {
        this.buildForLastTicketTape = true;
        return this;
    }

    /**
     * Задать расход ленты на конец бобины (т.к. события о оконачания ленты в момент печати отчета ещё нет)
     *
     * @param paperConsumption Расход билетной ленты
     */
    public ReportTicketTapeEnd setPaperConsumption(long paperConsumption) {
        this.paperConsumption = paperConsumption;
        return this;
    }

    @Override
    public ReportTicketTapeEnd build() throws Exception {
        params = new TapeEndReportTpl.Params();
        params.clicheParams = buildClicheParams();


        TicketTapeStatisticsBuilder.Statistics ticketTapeStatistics = new TicketTapeStatisticsBuilder(localDaoSession)
                .setTicketTapeId(ticketTapeId)
                .setBuildForLastTicketTape(buildForLastTicketTape)
                .build();

        TicketTapeStatisticsBuilder.TicketTapeInfo ticketTapeInfo = ticketTapeStatistics.ticketTapeEvents.get(0);

        params.startDate = ticketTapeStatistics.fromDate;
        params.tapeSeries = ticketTapeInfo.ticketTapeEvent.getSeries();
        params.tapeNumber = ticketTapeInfo.ticketTapeEvent.getNumber();

        ShiftEvent workingShifts = getShiftDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        if (workingShifts != null && workingShifts.getStatus() != ShiftEvent.Status.ENDED) {
            params.shiftNum = workingShifts.getShiftNumber();
        }

        params.firstDocNumber = ticketTapeInfo.firstDocument == null ? null : ticketTapeInfo.firstDocument.number;
        params.lastDocNumber = ticketTapeInfo.lastDocument == null ? null : ticketTapeInfo.lastDocument.number;

        /**
         * Расход ленты для бобины
         */
        long paperConsumption = ticketTapeStatistics.paperConsumption == null ? this.paperConsumption : ticketTapeStatistics.paperConsumption;
        params.consumptionOfTape = Decimals.divide(new BigDecimal(paperConsumption), new BigDecimal("1000"));

        params.receiptsCount = ticketTapeStatistics.returnsCount + ticketTapeStatistics.salesCount + ticketTapeStatistics.testTicketsCount + ticketTapeStatistics.servicesCount;
        params.testShiftCount = ticketTapeStatistics.reportsCount.testShiftSheetCount;
        params.shiftCount = ticketTapeStatistics.reportsCount.shiftSheetCount;
        params.preferentialShiftCount = ticketTapeStatistics.reportsCount.shiftDiscountSheetCount;
        params.auditLogCount = ticketTapeStatistics.reportsCount.auditTrailCount;
        params.preferentialMonthlyCount = ticketTapeStatistics.reportsCount.monthDiscountSheetCount;
        params.monthlyCount = ticketTapeStatistics.reportsCount.monthSheetCount;
        params.testMonthlySheetsCount = ticketTapeStatistics.reportsCount.testMonthSheetCount;
        params.salesForEttLogCount = ticketTapeStatistics.reportsCount.salesForEttLogCount;

        return this;
    }

    @Override
    protected Observable<PrintTapeEndReportOperation.Result> printImpl(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory()
                .getPrintTapeEndReportOperation(params).call();
    }

    @Override
    protected Observable<Void> addPrintReportEventObservable(PrintTapeEndReportOperation.Result printResult) {
        return Observable.just(null);
    }

}
