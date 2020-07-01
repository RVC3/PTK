package ru.ppr.cppk.printer.rx.operation;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

public class PrintMonthSheetFooterOperation extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrintMonthSheetFooterOperation.class);

    private final Params params;
    private final TextFormatter textFormatter;

    public PrintMonthSheetFooterOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public Observable<Result> call() {
        return wrap(() -> {
            if (params.isMonthClosed) {
                printer.printTextInNormalMode("ГАШЕНИЕ МЕС. 0.00  (ФИСК.)");
                printer.printTextInNormalMode(textFormatter.bigDelimiter());
            }
            printer.printTextInNormalMode(textFormatter.alignCenterText("ПЕЧАТЬ ЗАКОНЧЕНА"));
            printer.printTextInNormalMode(textFormatter.bigDelimiter());
            printer.printTextInNormalMode(" ");
            printer.printTextInNormalMode(" ");
            printer.printTextInNormalMode(" ");
            printer.waitPendingOperations();

            Date date = printer.getDate();

            return new Result(date);
        });
    }

    public static class Params {

        /**
         * Месяц закрыт (ведомость гашения месяца напечатана (или будет напечатана следом))
         */
        public boolean isMonthClosed;

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
