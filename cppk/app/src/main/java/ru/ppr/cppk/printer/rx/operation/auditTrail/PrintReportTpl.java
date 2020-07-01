package ru.ppr.cppk.printer.rx.operation.auditTrail;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Печать отчетных форм (п.1.5.2)
 * Сумма в ФР (п.1.5.3)
 *
 * @author Aleksandr Brazhkin
 */
public class PrintReportTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    PrintReportTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.printDateTime));
        printer.printTextInNormalMode("СУММА В ФР =" + textFormatter.asMoney(params.cashInFR));
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.printDateTime));
        printer.printTextInNormalMode(params.reportName);

        printer.printTextInNormalMode(textFormatter.smallDelimiter());

    }

    public static class Params {

        /**
         * Наименование отчета, выведенного на печать
         */
        public String reportName;
        /**
         * Дата и время вывода отчета на печать
         */
        public Date printDateTime;
        /**
         * Сумма в ФР
         */
        public BigDecimal cashInFR;

    }

}
