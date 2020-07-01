package ru.ppr.cppk.printer.rx.operation.auditTrail;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Оформление услуги (п.1.5.8)
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceSaleTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    ServiceSaleTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(textFormatter.asStr06d(params.pdNumber));
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.saleDateTime));
        printer.printTextInNormalMode(params.serviceName);
        printer.printTextInNormalMode(textFormatter.asMoney(params.cost));

        printer.printTextInNormalMode(textFormatter.smallDelimiter());
    }

    public static class Params {

        /**
         * Порядковый номер печатаемого ПД
         */
        public Integer pdNumber = 0;
        /**
         * Дата и время оформления ПД
         */
        public Date saleDateTime;
        /**
         * Наименование услуги
         */
        public String serviceName;
        /**
         * Стоимость
         */
        public BigDecimal cost = BigDecimal.ZERO;

    }

}
