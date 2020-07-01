package ru.ppr.cppk.printer.rx.operation.repealCheck;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.common.ShtrihFiscalHeaderSetter;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Completable;

/**
 * Операция повтора печати чека аннулирования.
 *
 * @author Aleksandr Brazhkin
 */
public class ShtrihPrintRepeatRepealCheckOperation extends PrintRepeatRepealCheckOperation {

    private static final String TAG = Logger.makeLogTag(ZebraPrintRepealCheckOperation.class);

    public ShtrihPrintRepeatRepealCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public Completable call() {
        return wrap(() -> {
            new ShtrihFiscalHeaderSetter(printer, textFormatter).setHeader(params.headerParams);

            printer.printDuplicateReceipt();

            return (Void) null;
        }).flatMap(result -> wrap(() -> {
            // Добавим пустых строк на отрыв
            printer.printTextInNormalMode("");
            printer.printTextInNormalMode("");
            printer.printTextInNormalMode("");
            printer.waitPendingOperations();
            return result;
        }))
                .toCompletable();
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

}
