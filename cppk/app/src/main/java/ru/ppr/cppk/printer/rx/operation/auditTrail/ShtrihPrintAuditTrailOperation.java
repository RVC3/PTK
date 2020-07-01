package ru.ppr.cppk.printer.rx.operation.auditTrail;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * @author Aleksandr Brazhkin
 */
public class ShtrihPrintAuditTrailOperation extends PrinterBaseOperation implements PrintAuditTrailOperation {

    private static final String TAG = Logger.makeLogTag(ShtrihPrintAuditTrailOperation.class);

    private final AuditTrailTpl.Params params;
    private final TextFormatter textFormatter;

    public ShtrihPrintAuditTrailOperation(IPrinter printer, TextFormatter textFormatter, AuditTrailTpl.Params params, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.textFormatter = textFormatter;
        this.params = params;
    }

    @Override
    public Observable<Result> call() {
        return wrap(() -> {
            Date date = printer.getDate();
            params.date = date;

            new AuditTrailTpl(params, textFormatter).printToDriver(printer);

            return new Result(date);
        });
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }
}