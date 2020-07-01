package ru.ppr.cppk.printer.rx.operation.clearingSheet;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * @author Brazhkin A.V.
 * <p>
 * 1.3 Реквизиты ведомости гашения смены
 * <p>
 * 1.8 Реквизиты ведомости гашения месяца
 */
public class ClearingSheetTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    ClearingSheetTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        new ReportClicheTpl(params.clicheParams, textFormatter, true).printToDriver(printer);

        new SheetInfoTpl(params.sheetInfo, textFormatter).printToDriver(printer);

        printer.printTextInNormalMode(textFormatter.alignCenterText("ПЕЧАТЬ ЗАКОНЧЕНА"));
        printer.printTextInNormalMode(textFormatter.bigDelimiter());
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.waitPendingOperations();

    }

    public static class Params {
        /**
         * Параметры для клише
         */
        public ReportClicheTpl.Params clicheParams;
        /**
         * Данные по ведомости гашения
         */
        public SheetInfoTpl.Params sheetInfo;

    }

}
