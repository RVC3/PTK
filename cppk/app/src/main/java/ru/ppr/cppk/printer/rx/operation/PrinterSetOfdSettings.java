package ru.ppr.cppk.printer.rx.operation;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.model.OfdSettings;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Операция установки насптоек подключения к ОФД
 *
 * @author Grigoriy Kashka
 */
public class PrinterSetOfdSettings extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterGetOfdSettings.class);

    private final OfdSettings ofdSettings;

    public PrinterSetOfdSettings(IPrinter printer, OfdSettings ofdSettings, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.ofdSettings = ofdSettings;
    }

    public Observable<Void> call() {
        return wrap(() -> {
            if (ofdSettings != null) {
                printer.setOfdSettings(ofdSettings);
            } else {
                Logger.trace(TAG, "params.ofdSettings = null");
            }
            return (Void) null;
        });
    }

}
