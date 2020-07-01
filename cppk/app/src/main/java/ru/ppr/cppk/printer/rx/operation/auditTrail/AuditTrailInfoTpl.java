package ru.ppr.cppk.printer.rx.operation.auditTrail;

import java.util.ArrayList;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Данные по отчету «Контрольный журнал» (п. 1.6.1)
 *
 * @author Aleksandr Brazhkin
 */
public class AuditTrailInfoTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    AuditTrailInfoTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(textFormatter.alignCenterText("КОНТРОЛЬНЫЙ ЖУРНАЛ"));
        printer.printTextInNormalMode(textFormatter.alignCenterText("СМЕНА № " + params.shiftNum));
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.date));
        printer.printTextInNormalMode("ПО v." + params.SWVersion);
        String cashier;
        int cashiersCount = params.cashiers.size();
        for (int i = 0; i < cashiersCount; i++) {
            if (i == cashiersCount - 1) {
                cashier = params.cashiers.get(i);
            } else {
                cashier = params.cashiers.get(i) + ";";
            }
            printer.printTextInNormalMode(cashier);
        }
        printer.printTextInNormalMode(textFormatter.bigDelimiter());

    }

    public static class Params {

        /**
         * Время с принтера
         */
        public Date date;
        /**
         * Порядковый номер смены
         */
        public Integer shiftNum;
        /**
         * Версия ПО
         */
        public String SWVersion;
        /**
         * Фамилия и инициалы кассиров, работавших в указанную смену
         */
        public ArrayList<String> cashiers;

    }

}
