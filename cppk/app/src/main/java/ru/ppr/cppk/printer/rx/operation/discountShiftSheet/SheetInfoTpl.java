package ru.ppr.cppk.printer.rx.operation.discountShiftSheet;

import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * @author Brazhkin A.V.
 *         <p>
 *         Данные по Льготной сменной ведомости (п.1.4.1)
 */
public class SheetInfoTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    SheetInfoTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(textFormatter.alignCenterText("ЛЬГОТНАЯ СМЕННАЯ"));
        printer.printTextInNormalMode(textFormatter.alignCenterText("ВЕДОМОСТЬ"));
        printer.printTextInNormalMode(textFormatter.alignCenterText("ЗА ВЫЧЕТОМ АННУЛИРОВАННЫХ"));
        printer.printTextInNormalMode(textFormatter.alignCenterText("СМЕНА № " + params.shiftNum));
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.date));
        printer.printTextInNormalMode(textFormatter.bigDelimiter());
    }

    public static class Params {
        /**
         * Время с принтера
         */
        protected Date date;
        /**
         * Порядковый номер смены
         */
        public int shiftNum;
    }
}
