package ru.ppr.cppk.printer.rx.operation.barcode;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * @author Grigoriy Kashka
 */
public class ShtrihPrintBarcodeOperation extends PrintBarcodeOperation {

    private static final String TAG = Logger.makeLogTag(ShtrihPrintBarcodeOperation.class);

    public ShtrihPrintBarcodeOperation(IPrinter printer, byte[] barcodeData, PrinterResourcesManager printerResourcesManager) {
        super(printer, barcodeData, printerResourcesManager);
    }

    @Override
    public Observable<Void> call() {
        return wrap(() -> {
            if (barcodeData != null) {
                printer.printTextInNormalMode("");
                printer.printBarcode(barcodeData);
                printer.printTextInNormalMode("");
                printer.printTextInNormalMode("");
                printer.printTextInNormalMode("");
                printer.waitPendingOperations();
            } else {
                Logger.trace(TAG, "params.barcodeData = null");
            }
            return (Void) null;
        });
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }
}