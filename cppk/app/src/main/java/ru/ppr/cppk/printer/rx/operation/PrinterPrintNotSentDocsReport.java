package ru.ppr.cppk.printer.rx.operation;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * @author Grigoriy Kashka
 */
public class PrinterPrintNotSentDocsReport extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterPrintNotSentDocsReport.class);

    public PrinterPrintNotSentDocsReport(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    public Observable<Result> call() {
        return wrap(() -> {
            printer.printNotSentDocsReport();
            printer.printTextInNormalMode("");
            printer.printTextInNormalMode("");
            printer.printTextInNormalMode("");

            Date date = printer.getDate();
            return new Result(date);
        });
    }

    public static class Result {
        private Date operationTime;

        public Result(Date date) {
            operationTime = date;
        }

        public Date getOperationTime() {
            return operationTime;
        }
    }

}