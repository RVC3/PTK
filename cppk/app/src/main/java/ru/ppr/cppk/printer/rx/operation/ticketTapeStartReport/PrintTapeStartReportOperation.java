package ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

public abstract class PrintTapeStartReportOperation extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrintTapeStartReportOperation.class);

    public PrintTapeStartReportOperation(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    public abstract Observable<Result> call();

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

    public static class Result {
        private Date operationTime;

        public Result(Date date) {
            this.operationTime = date;
        }

        public Date getOperationTime() {
            return operationTime;
        }
    }
}
