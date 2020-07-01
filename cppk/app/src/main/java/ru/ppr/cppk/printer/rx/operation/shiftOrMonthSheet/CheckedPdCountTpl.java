package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Проверено документов (п.1.1.14)
 *
 * @author Aleksandr Brazhkin
 */
public class CheckedPdCountTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    CheckedPdCountTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(textFormatter.alignCenterText("ПРОВЕРЕНО ДОКУМЕНТОВ"));
        printer.printTextInNormalMode("БСК АБОНЕМЕНТЫ =" + params.checkedOnBSKSeasonTicketsCount);
        printer.printTextInNormalMode("БСК РАЗОВЫЕ    =" + params.checkedOnBSKOnceOnlyTicketsCount);
        printer.printTextInNormalMode("РАЗОВЫЕ БИЛЕТЫ =" + params.checkedOnceOnlyTicketsCount);
        printer.printTextInNormalMode(textFormatter.bigDelimiter());

    }

    public static class Params {

        /**
         * Количество проверенных абонементов на БСК
         */
        public Integer checkedOnBSKSeasonTicketsCount = 0;
        /**
         * Количество проверенных разовых ПД на БСК
         */
        public Integer checkedOnBSKOnceOnlyTicketsCount = 0;
        /**
         * Количество проверенных разовых ПД на бумажном носителе
         */
        public Integer checkedOnceOnlyTicketsCount = 0;

    }

}
