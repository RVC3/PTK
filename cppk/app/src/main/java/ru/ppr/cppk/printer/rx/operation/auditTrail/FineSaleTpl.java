package ru.ppr.cppk.printer.rx.operation.auditTrail;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Оформление штрафа (п.1.5.10)
 *
 * @author Aleksandr Brazhkin
 */
public class FineSaleTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    FineSaleTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {
        String paymentMethod = "";

        if (params.isNonCash) {
            paymentMethod = "БАНК.КАРТА";
        }

        printer.printTextInNormalMode(textFormatter.alignWidthText(textFormatter.asStr06d(params.pdNumber), paymentMethod));
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.saleDateTime));
        printer.printTextInNormalMode(params.fineName);
        printer.printTextInNormalMode(textFormatter.asMoney(params.cost));

        if (params.isNonCash) {
            printer.printTextInNormalMode("ОПЛАТА: " + textFormatter.asMaskedBankCardNumber(params.bankCardPan));
        }

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
         * Наименование штрафа
         */
        public String fineName;
        /**
         * Стоимость
         */
        public BigDecimal cost = BigDecimal.ZERO;
        /**
         * Признак оплаты ПД безналичным способом
         */
        public Boolean isNonCash = false;
        /**
         * Маскированный номер банковской карты
         */
        public String bankCardPan = "";

    }

}
