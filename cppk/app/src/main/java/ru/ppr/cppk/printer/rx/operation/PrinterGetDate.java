package ru.ppr.cppk.printer.rx.operation;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Created by Dmitry Nevolin on 08.04.2016.
 */
public class PrinterGetDate extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterGetDate.class);

    public PrinterGetDate(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    public Observable<Date> call() {
        return wrap(printer::getDate);
    }

}
