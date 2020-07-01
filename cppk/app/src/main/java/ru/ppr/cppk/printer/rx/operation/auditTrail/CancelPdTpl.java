package ru.ppr.cppk.printer.rx.operation.auditTrail;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Аннулирование ПД (п.1.5.5)
 *
 * @author Aleksandr Brazhkin
 */
public class CancelPdTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    CancelPdTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(textFormatter.asStr06d(params.PDNumber));
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.cancelDateTime));
        printer.printTextInNormalMode("АННУЛ. " + textFormatter.asStr06d(params.canceledPDNumber));
        printer.printTextInNormalMode(params.PDType);
        if (Decimals.moreThanZero(params.incomeLoss)) {
            printer.printTextInNormalMode("СУММА         =" + textFormatter.asMoney(params.fullCostSum));
            printer.printTextInNormalMode("ВЫП. ДОХОД    =" + textFormatter.asMoney(params.incomeLoss));
            printer.printTextInNormalMode("СБОР          =" + textFormatter.asMoney(params.feeSum));
        } else {
            printer.printTextInNormalMode(textFormatter.asMoney(params.fullCostSum) + " СБОР=" + textFormatter.asMoney(params.feeSum));
        }
        if (params.isThereBack) {
            printer.printTextInNormalMode("" + params.departureStationName + "<=>" + params.destinationStationName);
        } else {
            printer.printTextInNormalMode("" + params.departureStationName + "==>" + params.destinationStationName);
        }
        if (params.exemptionBSCType == null) {
            // Нет льготы считанной с карты
            if (params.exemptionNum != 0) {
                // Но, ввели льготу руками
                printer.printTextInNormalMode("ЛЬГ." + params.exemptionNum);
                printer.printTextInNormalMode("№ ЛЬГ.ДОК " + params.preferentialDocumentNum);
                printer.printTextInNormalMode("ФИО: " + params.preferentialDocumentFIO);
            }
            if (params.BSKOuterNumber != null) {
                // Записали на БСК
                printer.printTextInNormalMode(params.BSCType + ": " + params.BSKOuterNumber + " -" + params.trackNum);
            }
        } else {
            // Льгота считана с карты
            printer.printTextInNormalMode("ЛЬГ." + params.exemptionNum + " " + params.exemptionBSCType + ":");
            if (params.isTicketWritten && params.trackNum != null) {
                printer.printTextInNormalMode(params.exemptionBSCOuterNumber + " -" + params.trackNum);
            } else {
                printer.printTextInNormalMode(params.exemptionBSCOuterNumber);
            }
        }

        printer.printTextInNormalMode(textFormatter.smallDelimiter());

    }

    public static class Params {

        /**
         * порядковый номер печатаемого ПД
         */
        public Integer PDNumber;
        /**
         * дата и время печати документа
         */
        public Date cancelDateTime;
        /**
         * порядковый номер аннулируемого документа
         */
        public Integer canceledPDNumber;
        /**
         * вид аннулируемого ПД
         */
        public String PDType;
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
        /**
         * признак проезда туда-обратно
         */
        public Boolean isThereBack = false;
        /**
         * Номер льготы, по которой был оформлен ПД
         */
        public int exemptionNum;
        /**
         * Тип БСК, с которой была считана льгота
         */
        public String exemptionBSCType;
        /**
         * внешний номер БСК, с которой была считана льгота
         */
        public String exemptionBSCOuterNumber;
        /**
         * Номер дорожки БСК, на которую был записан ПД
         */
        public Integer trackNum;
        /**
         * Флаг, говорящий о том, что ПД записан/распечатан
         */
        public boolean isTicketWritten;
        /**
         * Тип БСК, на которую записан ПД
         */
        public String BSCType;
        /**
         * внешний номер БСК, на которую записан ПД
         */
        public String BSKOuterNumber;
        /**
         * Номер льготного документа, подтверждающего льготный проезд
         */
        public String preferentialDocumentNum;
        /**
         * Фамилия и инициалы владельца льготы
         */
        public String preferentialDocumentFIO;

    }

}
