package ru.ppr.cppk.printer.rx.operation.clearingSheet;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

public class PrintClearingSheetOperation extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrintClearingSheetOperation.class);

    private final ClearingSheetTpl.Params params;
    private final TextFormatter textFormatter;

    public PrintClearingSheetOperation(IPrinter printer, ClearingSheetTpl.Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public Observable<Result> call() {
        return wrap(() -> {
            params.sheetInfo.availableRecordsInFRCount = printer.getAvailableSpaceForShifts();
            new ClearingSheetTpl(params, textFormatter).printToDriver(printer);

            Date date = printer.getDate();

            return new Result(date);
        });
    }

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
