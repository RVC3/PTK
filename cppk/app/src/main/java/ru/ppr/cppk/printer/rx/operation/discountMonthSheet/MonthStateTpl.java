package ru.ppr.cppk.printer.rx.operation.discountMonthSheet;

import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Состояние месяца (https://aj.srvdev.ru/browse/CPPKPP-32424)
 *
 * @author Aleksandr Brazhkin
 */
public class MonthStateTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    MonthStateTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        if (params.startShiftNum == null) {
            printer.printTextInNormalMode("СМЕНЫ НЕ ОТКРЫВАЛИСЬ");
        } else if (params.startCloseShiftDate == null) {
            printer.printTextInNormalMode("В МЕСЯЦЕ НЕТ ЗАКРЫТЫХ СМЕН");
        } else {
            printer.printTextInNormalMode("СМЕНА № " + params.startShiftNum + "-" + params.endShiftNum);
            printer.printTextInNormalMode(" " + textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.startOpenShiftDate) + " -");
            printer.printTextInNormalMode(" " + textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.endCloseShiftDate));

            if (params.firstDocNumber == null) {
                printer.printTextInNormalMode("БИЛЕТЫ НЕ ОФОРМЛЯЛИСЬ");
            } else {
                printer.printTextInNormalMode(" ДОК. № " + textFormatter.asStr06d(params.firstDocNumber) + " - № " + textFormatter.asStr06d(params.lastDocNumber));
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
         * Номер первого оформленного документа в  месяце
         */
        public Integer firstDocNumber = 0;
        /**
         * Номер последнего оформленного документа в  месяце
         */
        public Integer lastDocNumber = 0;
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
    }

}
