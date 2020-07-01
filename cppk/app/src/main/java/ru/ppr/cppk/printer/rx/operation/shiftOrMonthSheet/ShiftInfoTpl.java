package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Данные по отчету (п.1.1.2)
 * Данные по отчету «Сменная ведомость» (п.1.2.1)
 *
 * @author Aleksandr Brazhkin
 */
public class ShiftInfoTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    ShiftInfoTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        if (params.isTest) {
            printer.printTextInNormalMode(textFormatter.alignCenterText("ПРОБНАЯ СМЕННАЯ"));
            printer.printTextInNormalMode(textFormatter.alignCenterText("ВЕДОМОСТЬ № " + params.sheetNum));
        } else {
            printer.printTextInNormalMode(textFormatter.alignCenterText("СМЕННАЯ ВЕДОМОСТЬ"));
        }
        printer.printTextInNormalMode(textFormatter.alignCenterText("СМЕНА № " + params.shiftNum));
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.date));
        if (!params.isTest) {
            printer.printTextInNormalMode("ПО v." + params.SWversion);
            printer.printTextInNormalMode(" ");
            printer.printTextInNormalMode("ПРОБНЫХ ВЕДОМОСТЕЙ " + params.testSheetsCount);
            printer.printTextInNormalMode("КОНТР. ЖУРН.       " + params.auditTrailsCount);
        }
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode("СУММА ПО ФИСКАЛЬНОМУ");
        printer.printTextInNormalMode("РЕГИСТРАТОРУ");
        printer.printTextInNormalMode("=" + textFormatter.asMoney(params.cashFiscalRegister));
        printer.printTextInNormalMode(textFormatter.bigDelimiter());

    }

    public static class Params {

        /**
         * Время с принтера
         */
        public Date date;
        /**
         * Является тестовой
         */
        protected Boolean isTest = true;
        /**
         * Порядковый номер снятой пробной ведомости в смене
         */
        public Integer sheetNum = 0;
        /**
         * Версия ПО
         */
        public String SWversion = new String();
        /**
         * Количество пробных ведомостей, распечатанных за текущую смену
         */
        public Integer testSheetsCount = 0;
        /**
         * Количество распечатанных контрольных журналов
         */
        public Integer auditTrailsCount = 0;
        /**
         * Номер смены
         */
        public Integer shiftNum = 0;
        /**
         * Сумма по фискальному регистратору
         */
        public BigDecimal cashFiscalRegister = BigDecimal.ZERO;

    }

}
