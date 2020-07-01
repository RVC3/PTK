package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Состояния смены (п.1.1.3)
 *
 * @author Aleksandr Brazhkin
 */
public class ShiftStatesTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    ShiftStatesTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode("НАЧАЛО СМЕНЫ");
        if (params.startDocNumber == null) {
            printer.printTextInNormalMode("Нет документов");
        } else {
            printer.printTextInNormalMode(" ПЕРВЫЙ ДОКУМЕНТ");
            printer.printTextInNormalMode(" " + textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.startDocDate));
            printer.printTextInNormalMode(" ДОК. № " + textFormatter.asStr06d(params.startDocNumber));
            printer.printTextInNormalMode(" ОПЕРАТОР № " + params.startCashierNum);

            if (!params.isTest) {
                printer.printTextInNormalMode("ОКОНЧАНИЕ СМЕНЫ");
            }
            if (!params.isTestAtShiftStart) {
                printer.printTextInNormalMode(" ПОСЛЕДНИЙ ДОКУМЕНТ");
                printer.printTextInNormalMode(" " + textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.endDocDate));
                printer.printTextInNormalMode(" ДОК. № " + textFormatter.asStr06d(params.endDocNumber));
                printer.printTextInNormalMode(" ОПЕРАТОР № " + params.endCashierNum);
            }
        }
        printer.printTextInNormalMode(textFormatter.bigDelimiter());
    }

    public static class Params {

        /**
         * Является тестовой
         */
        protected boolean isTest;
        /**
         * Является пробной на начало смены
         */
        protected boolean isTestAtShiftStart = false;
        /**
         * Номер первого пробного ПД в смене
         */
        public Integer startDocNumber;
        /**
         * Дата и время печати первого пробного ПД в смене
         */
        public Date startDocDate;
        /**
         * Порядковый номер кассира, который распечатал первый пробный ПД в
         * смене
         */
        public Integer startCashierNum;
        /**
         * Номер последнего распечатанного за смену документа
         */
        public Integer endDocNumber;
        /**
         * Дата и время формирования последнего за смену документа
         */
        public Date endDocDate;
        /**
         * Порядковый номер кассира, который распечатал последний документ в
         * смене
         */
        public Integer endCashierNum;

    }

}
