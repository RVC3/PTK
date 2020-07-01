package ru.ppr.cppk.printer.rx.operation.fineCheck;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.common.ShtrihFiscalHeaderSetter;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.TextStyle;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.logger.Logger;
import rx.Completable;

/**
 * Операция повтора печати чека взимания штрафа.
 *
 * @author Aleksandr Brazhkin
 */
public class ShtrihPrintRepeatFineCheckOperation extends PrintRepeatFineCheckOperation {

    private static final String TAG = Logger.makeLogTag(ShtrihPrintRepeatFineCheckOperation.class);

    public ShtrihPrintRepeatFineCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public Completable call() {
        return wrap(() -> {
            new ShtrihFiscalHeaderSetter(printer, textFormatter).setHeader(params.headerParams);

            printer.printDuplicateReceipt();

            return (Void) null;
        }).flatMap(result -> wrap(() -> {
            new FineCheckTpl(tplPrinter, tplTextFormatter, params.fineCheckTplParams).printToDriver();
            //добавим пустых строк на отрыв
            printer.printTextInNormalMode("");
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

    private final FineCheckTpl.Printer tplPrinter = new FineCheckTpl.Printer() {
        @Override
        public void printText(String text, TextStyle textStyle) throws PrinterException {
            printer.printTextInNormalMode(text, textStyle);
        }
    };

    private final FineCheckTpl.TextFormatter tplTextFormatter = new FineCheckTpl.TextFormatter() {
        @Override
        public String asStr06d(int number) {
            return textFormatter.asStr06d(number);
        }

        @Override
        public String alignCenter(String text, TextStyle textStyle) {
            return textFormatter.alignCenterText(text, textStyle);
        }

        @Override
        public String asDate_dd_MM_yyyy_HH_mm(Date dateTime) {
            return textFormatter.asDate_dd_MM_yyyy_HH_mm(dateTime);
        }
    };
}
