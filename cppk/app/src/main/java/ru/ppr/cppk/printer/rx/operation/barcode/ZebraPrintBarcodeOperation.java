package ru.ppr.cppk.printer.rx.operation.barcode;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * @author Grigoriy Kashka
 */
public class ZebraPrintBarcodeOperation extends PrintBarcodeOperation {

    private static final String TAG = Logger.makeLogTag(ZebraPrintBarcodeOperation.class);

    public ZebraPrintBarcodeOperation(IPrinter printer, byte[] barcodeData, PrinterResourcesManager printerResourcesManager) {
        super(printer, barcodeData, printerResourcesManager);
    }

    @Override
    public Observable<Void> call() {
        return wrap(() -> {
            if (barcodeData != null) {
                printer.printBarcode(barcodeData);
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