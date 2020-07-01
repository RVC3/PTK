package ru.ppr.cppk.printer.rx.operation.auditTrail;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Оформление доплаты (п.1.5.6)
 *
 * @author Aleksandr Brazhkin
 */
public class ExtraChargeTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    ExtraChargeTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(textFormatter.alignWidthText(textFormatter.asStr06d(params.PDNumber), "М " + params.trainCategory));
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.sellDateTime));
        printer.printTextInNormalMode("ДОПЛАТА к " + textFormatter.asStr06d(params.basePDNumber));
        printer.printTextInNormalMode("   ID ККТ " + String.valueOf(params.basePDPTKNumber));
        printer.printTextInNormalMode("   " + textFormatter.asDate_dd_MM_yyyy_HH_mm(params.basePDsellDateTime));
        printer.printTextInNormalMode(textFormatter.asMoney(params.fullCostSum) + " СБОР=" + textFormatter.asMoney(params.feeSum));
        printer.printTextInNormalMode("" + params.departureStationName + "==>" + params.destinationStationName);

        printer.printTextInNormalMode(textFormatter.smallDelimiter());

    }

    public static class Params {

        /**
         * порядковый номер печатаемого ПД
         */
        public int PDNumber;
        /**
         * Категория поезда проезда
         */
        public int trainCategory;
        /**
         * дата и время оформления ПД
         */
        public Date sellDateTime;
        /**
         * Номер ПД, к которому оформляется доплата
         */
        public int basePDNumber;
        /**
         * Номер конечного оборудования (СК, БПА), на котором был оформлен
         * первый ПД, к которому производится доплата
         */
        public long basePDPTKNumber;
        /**
         * Дата и время продажи ПД, к которому оформляется доплата
         */
        public Date basePDsellDateTime;
        /**
         * Сумма денежных средств, уплаченных пассажиром за ПД
         */
        public BigDecimal fullCostSum = BigDecimal.ZERO;
        /**
         * Сумма денежных средств, уплаченных пассажиром за сбор
         */
        public BigDecimal feeSum = BigDecimal.ZERO;
        /**
         * Сумма выпадающего дохода
         */
        public BigDecimal incomeLoss = BigDecimal.ZERO;
        /**
         * Наименование станции отправления
         */
        public String departureStationName = null;
        /**
         * Наименование станции назначения
         */
        public String destinationStationName = null;

    }

}
