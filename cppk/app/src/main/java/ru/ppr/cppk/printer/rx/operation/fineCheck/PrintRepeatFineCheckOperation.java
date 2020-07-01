package ru.ppr.cppk.printer.rx.operation.fineCheck;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.cppk.printer.rx.operation.base.InOpenedShiftOperation;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Completable;

/**
 * Операция повтора печати чека взимания штрафа.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class PrintRepeatFineCheckOperation extends InOpenedShiftOperation {

    final Params params;
    final TextFormatter textFormatter;

    PrintRepeatFineCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public abstract Completable call();

    /**
     * Входные параметры для печати чека продажи
     */
    public static class Params {
        /**
         * Параметры для заголовка
         */
        public FiscalHeaderBuilder.Params headerParams;
        /**
         * Параметры для шаблона билета
         */
        public FineCheckTpl.Params fineCheckTplParams;

    }
}
