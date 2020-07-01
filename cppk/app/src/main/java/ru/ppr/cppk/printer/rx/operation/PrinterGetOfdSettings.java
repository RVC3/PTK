package ru.ppr.cppk.printer.rx.operation;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.model.OfdSettings;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Операция получения настроек связи с ОФД с принтера
 *
 * @author Grigoriy Kashka
 */
public class PrinterGetOfdSettings extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterGetOfdSettings.class);

    public PrinterGetOfdSettings(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    public Observable<OfdSettings> call() {
        return wrap(printer::getOfdSettings);
    }
}
