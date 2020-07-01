package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

public class PrintShiftOrMonthSheetOperation extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrintShiftOrMonthSheetOperation.class);

    private final ShiftOrMonthSheetTpl.Params params;
    private final TextFormatter textFormatter;

    public PrintShiftOrMonthSheetOperation(IPrinter printer, ShiftOrMonthSheetTpl.Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public Observable<Result> call() {
        return wrap(() -> {
            Date date = printer.getDate();
            params.date = date;

            new ShiftOrMonthSheetTpl(params, textFormatter).printToDriver(printer);

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
