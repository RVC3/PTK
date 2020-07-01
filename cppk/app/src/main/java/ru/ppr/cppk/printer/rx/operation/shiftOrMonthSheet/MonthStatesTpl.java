package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Состояния месяца (п.1.8.2)
 *
 * @author Aleksandr Brazhkin
 */
public class MonthStatesTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    MonthStatesTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        if (params.startShiftNum == null) {
            printer.printTextInNormalMode("Смены не открывались");
        } else if (params.startCloseShiftDate == null) {
            printer.printTextInNormalMode("В месяце нет закрытых смен");
        } else {
            printer.printTextInNormalMode("НАЧАЛО");
            printer.printTextInNormalMode(" СМЕНА № " + params.startShiftNum);
            printer.printTextInNormalMode(" " + textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.startOpenShiftDate));
            if (params.startFirstDocNumber == null) {
                printer.printTextInNormalMode("Нет документов");
            } else {
                printer.printTextInNormalMode(" начало ДОК. №" + textFormatter.asStr06d(params.startFirstDocNumber));
                printer.printTextInNormalMode(" окончание ДОК. №" + textFormatter.asStr06d(params.startLastDocNumber));
            }
            printer.printTextInNormalMode("ОКОНЧАНИЕ");
            printer.printTextInNormalMode("  СМЕНА № " + params.endShiftNum);
            printer.printTextInNormalMode("  " + textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.endOpenShiftDate));
            if (params.endFirstDocNumber == null) {
                printer.printTextInNormalMode("Нет документов");
            } else {
                printer.printTextInNormalMode("  начало ДОК. №" + textFormatter.asStr06d(params.endFirstDocNumber));
                printer.printTextInNormalMode("  окончание ДОК. №" + textFormatter.asStr06d(params.endLastDocNumber));
            }
        }
        printer.printTextInNormalMode(textFormatter.bigDelimiter());
    }

    public static class Params {

        /**
         * Номер первой смены в месяце
         */
        public Integer startShiftNum;
        /**
         * Дата и время открытия первой смены в месяце
         */
        public Date startOpenShiftDate;
        /**
         * Дата и время закрытия первой смены в месяце
         */
        public Date startCloseShiftDate;
        /**
         * Номер первого оформленного документа в первой смене месяца (пробного
         * ПД)
         */
        public Integer startFirstDocNumber = 0;
        /**
         * Номер последнего оформленного документа в первой смене месяца
         */
        public Integer startLastDocNumber = 0;
        /**
         * Номер последней смены в месяце
         */
        public Integer endShiftNum;
        /**
         * Дата и время открытия последней смены в месяце
         */
        public Date endOpenShiftDate;
        /**
         * Дата и время закрытия последней смены в месяце
         */
        public Date endCloseShiftDate;
        /**
         * Номер первого оформленного документа в последней смене месяца
         */
        public Integer endFirstDocNumber = 0;
        /**
         * Номер последнего оформленного документа в последней смене месяца
         */
        public Integer endLastDocNumber = 0;
    }

}
