package ru.ppr.cppk.printer.rx.operation;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.model.OfdDocsState;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Операция получения статуса по неотправленным в ОФД документам с принтера.
 *
 * @author Grigoriy Kashka
 */
public class PrinterGetOfdDocsState extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterGetOfdSettings.class);

    public PrinterGetOfdDocsState(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    public Observable<OfdDocsState> call() {
        return wrap(printer::getOfdDocsState);
    }
}
