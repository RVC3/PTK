package ru.ppr.cppk.printer.rx.operation.discountMonthSheet;

import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Данные по отчету «Месячная льготная ведомость» (п.1.7.1)
 *
 * @author Brazhkin A.V.
 */
public class MonthInfoTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    MonthInfoTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(textFormatter.alignCenterText("ЛЬГОТНАЯ ВЕДОМОСТЬ"));
        printer.printTextInNormalMode(textFormatter.alignCenterText("ЗА МЕСЯЦ"));
        printer.printTextInNormalMode(textFormatter.alignCenterText("ЗА ВЫЧЕТОМ АННУЛИРОВАННЫХ"));
        printer.printTextInNormalMode(textFormatter.alignCenterText("МЕСЯЦ № " + params.monthNum));
        printer.printTextInNormalMode(textFormatter.alignCenterText(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.date)));
        printer.printTextInNormalMode(textFormatter.bigDelimiter());
    }

    public static class Params {
        /**
         * Время с принтера
         */
        public Date date;

        /**
         * порядковый номер месяца
         */
        public int monthNum;
    }
}
