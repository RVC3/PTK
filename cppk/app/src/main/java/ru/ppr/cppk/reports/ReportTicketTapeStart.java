package ru.ppr.cppk.reports;

import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport.PrintTapeStartReportOperation;
import ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport.TapeStartReportTpl;
import ru.ppr.ikkm.IPrinter;
import rx.Completable;
import rx.Observable;

/**
 * Отчет "Установка билетной ленты"
 *
 * @author Brazhkin A.V.
 */
public class ReportTicketTapeStart extends Report<ReportTicketTapeStart, PrintTapeStartReportOperation.Result> {

    private TapeStartReportTpl.Params params;
    private Integer shiftNum;
    private Integer firstDocNumber;
    private String tapeSeries;
    private int tapeNumber;

    public ReportTicketTapeStart() {
        super();
    }

    public ReportTicketTapeStart setShiftNum(Integer shiftNum) {
        this.shiftNum = shiftNum;
        return this;
    }

    public ReportTicketTapeStart setFirstDocNumber(Integer firstDocNumber) {
        this.firstDocNumber = firstDocNumber;
        return this;
    }

    public ReportTicketTapeStart setTapeSeries(String tapeSeries) {
        this.tapeSeries = tapeSeries;
        return this;
    }

    public ReportTicketTapeStart setTapeNumber(int tapeNumber) {
        this.tapeNumber = tapeNumber;
        return this;
    }

    @Override
    public ReportTicketTapeStart build() throws Exception {

        params = new TapeStartReportTpl.Params();
        params.clicheParams = buildClicheParams();
        params.shiftNum = shiftNum;
        params.firstDocNumber = firstDocNumber;
        params.tapeSeries = tapeSeries;
        params.tapeNumber = tapeNumber;

        return this;
    }

    @Override
    protected Observable<PrintTapeStartReportOperation.Result> printImpl(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory()
                .getPrintTapeStartReportOperation(params).call();
    }

    @Override
    protected Observable<Void> addPrintReportEventObservable(PrintTapeStartReportOperation.Result printResult) {
        return Observable.just(null);
    }

    @Override
    protected Completable checkTicketTapeIsSet() {
        // Потому что только этот отчет можно печатать без установленной билетной ленты
        return Completable.complete();
    }

}