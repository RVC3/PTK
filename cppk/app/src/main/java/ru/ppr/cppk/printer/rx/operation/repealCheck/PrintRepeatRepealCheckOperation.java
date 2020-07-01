package ru.ppr.cppk.printer.rx.operation.repealCheck;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.cppk.printer.rx.operation.base.InOpenedShiftOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Completable;

/**
 * Операция повтора печати чека аннулирования.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class PrintRepeatRepealCheckOperation extends InOpenedShiftOperation {

    final Params params;
    final TextFormatter textFormatter;

    PrintRepeatRepealCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public abstract Completable call();

    /**
     * Класс-контейнер входных параметров
     */
    public static class Params {
        /**
         * Параметры для заголовка
         */
        public FiscalHeaderBuilder.Params headerParams;
        /**
         * Параметры для шаблона билета
         */
        public RepealCheckTpl.Params repealCheckTplParams;
    }
}
