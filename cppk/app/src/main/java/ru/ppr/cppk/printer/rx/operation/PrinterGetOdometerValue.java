package ru.ppr.cppk.printer.rx.operation;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

public class PrinterGetOdometerValue extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterGetOdometerValue.class);

    public PrinterGetOdometerValue(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    public Observable<Result> call() {
        return wrap(() -> {
            long odometerValue = printer.getOdometerValue();
            return new Result(odometerValue);
        });
    }

    public static class Result {

        private long odometerValue;

        public Result(long odometerValue) {
            this.odometerValue = odometerValue;
        }

        public long getOdometerValue() {
            return odometerValue;
        }
    }
}
