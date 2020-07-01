package ru.ppr.cppk.printer.rx.operation;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.InOpenedShiftOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Операция печати дкбликата последнего чека.
 *
 * @author Aleksandr Brazhkin
 */
public class PrintDuplicateReceiptOperation extends InOpenedShiftOperation {

    private static final String TAG = Logger.makeLogTag(PrintDuplicateReceiptOperation.class);

    public PrintDuplicateReceiptOperation(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    public Observable<Void> call() {
        return wrap(() -> {
            printer.printDuplicateReceipt();
            return null;
        });
    }

}
