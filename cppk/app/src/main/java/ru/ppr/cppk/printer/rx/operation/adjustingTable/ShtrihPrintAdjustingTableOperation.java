package ru.ppr.cppk.printer.rx.operation.adjustingTable;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * @author Grigoriy Kashka
 */
public class ShtrihPrintAdjustingTableOperation extends PrinterBaseOperation implements PrinterPrintAdjustingTableOperation {

    public ShtrihPrintAdjustingTableOperation(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    public Observable<? extends Void> call() {
        return wrap(() -> {
            printer.printAdjustingTable();
            printer.printTextInNormalMode("");
            printer.printTextInNormalMode("");
            printer.printTextInNormalMode("");
            printer.printTextInNormalMode("");
            return (Void) null;
        });
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

}
