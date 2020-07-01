package ru.ppr.cppk.printer.rx.operation.barcode;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * Операция печати ШК
 */
public abstract class PrintBarcodeOperation extends PrinterBaseOperation {

    final byte[] barcodeData;

    PrintBarcodeOperation(IPrinter printer, byte[] barcodeData, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.barcodeData = barcodeData;
    }

    public abstract Observable<Void> call();
}
