package ru.ppr.cppk.printer.rx.operation;

import java.math.BigDecimal;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

public class PrinterGetCashInFR extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterGetCashInFR.class);

    public PrinterGetCashInFR(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    public Observable<Result> call() {
        return wrap(() -> {
            BigDecimal cashInFR = printer.getCashInFR();
            return new Result(cashInFR);
        });
    }

    public static class Result {

        private BigDecimal cashInFR = BigDecimal.ZERO;

        public Result(BigDecimal cashInFR) {
            this.cashInFR = cashInFR;
        }

        public BigDecimal getCashInFR() {
            return cashInFR;
        }
    }
}
