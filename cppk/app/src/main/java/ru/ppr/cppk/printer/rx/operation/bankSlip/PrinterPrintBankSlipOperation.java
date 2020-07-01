package ru.ppr.cppk.printer.rx.operation.bankSlip;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Операция печати банковского слипа.
 */
public abstract class PrinterPrintBankSlipOperation extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterPrintBankSlipOperation.class);

    final Params params;
    final TextFormatter textFormatter;

    public PrinterPrintBankSlipOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public abstract Observable<Object> call();

    public static class Params {
        /**
         * Параметры шаблона
         */
        public BankSlipTpl.Params tplParams = new BankSlipTpl.Params();
    }

    final BankSlipTpl.Printer tplPrinter = new BankSlipTpl.Printer() {
        @Override
        public void printText(String text) throws PrinterException {
            printer.printTextInNormalMode(text);
        }
    };

}
