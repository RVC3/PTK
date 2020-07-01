package ru.ppr.cppk.printer.rx.operation.printerPrintSalesForEttLog;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.TextStyle;

/**
 * Created by Александр on 11.08.2016.
 * <p>
 * Состав полей блока «ПД по ЭТТ» (п.1.6.2)
 */
public class PrinterTplSalesForEttPdInfo extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    PrinterTplSalesForEttPdInfo(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        String paymentMethodWithRoute = "";
        if (params.isNonCash) {
            paymentMethodWithRoute = "БЕЗНАЛ ";
        }
        paymentMethodWithRoute += "М " + params.trainCategory + " ";
        printer.printTextInNormalMode(textFormatter.alignWidthText("--" + textFormatter.asStr06d(params.PDNumber), paymentMethodWithRoute));
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.sellDateTime));

        printer.printTextInNormalMode(params.PDType);
        printer.printTextInNormalMode("СУММА         =" + textFormatter.asMoney(params.fullCostSum));
        printer.printTextInNormalMode("ВЫП. ДОХОД    =" + textFormatter.asMoney(params.incomeLoss));
        printer.printTextInNormalMode("СБОР          =" + textFormatter.asMoney(params.feeSum));

        if (params.isThereBack) {
            printer.printTextInNormalMode("" + params.departureStationName + "<=>" + params.destinationStationName);
        } else {
            printer.printTextInNormalMode("" + params.departureStationName + "==>" + params.destinationStationName);
        }
        printer.printTextInNormalMode(params.exemptionBSCType + ": " + params.exemptionBSKOuterNumber);

        String trackNum;
        if (params.isTicketWritten && params.trackNum != null) {
            trackNum = "ЗАПИСЬ " + params.trackNum;
        } else {
            trackNum = "ПЕЧАТЬ ПД";
        }
        printer.printTextInNormalMode("ЛЬГ." + params.exemptionNum + " / " + trackNum);

        String organization = params.passengerCategory + "-" + params.issueUnitCode + "-" + params.ownerOrganizationCode;
        printer.printTextInNormalMode(organization);

        String strForConcat;
        if ("И".equals(params.passengerCategory) || "Г".equals(params.passengerCategory) || "Н".equals(params.passengerCategory)) {
            if (params.passengerFio != null) {
                printer.printTextInNormalMode(params.passengerFio + "/");
            }
            strForConcat = params.guardianFio;
        } else {
            strForConcat = params.passengerFio;
        }
        if (strForConcat == null) {
            strForConcat = "";
        }
        String snils = params.snilsNumber == null ? "" : params.snilsNumber;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(strForConcat).append("/*").append(snils);
        if (stringBuilder.length() > textFormatter.getWidthForTextStyle(TextStyle.TEXT_NORMAL)) {
            printer.printTextInNormalMode(strForConcat + "/*");
            printer.printTextInNormalMode(snils);
        } else {
            printer.printTextInNormalMode(stringBuilder.toString());
        }

        printer.printTextInNormalMode(textFormatter.smallDelimiter());
    }

    public static class Params {

        /**
         * порядковый номер печатаемого ПД
         */
        public Integer PDNumber = 0;
        /**
         * Признак оплаты ПД безналичным способом
         */
        public Boolean isNonCash = false;
        /**
         * Категория поезда проезда
         */
        public Integer trainCategory = 0;
        /**
         * дата и время оформления ПД
         */
        public Date sellDateTime;
        /**
         * вид оформленного ПД
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
        public String exemptionBSKOuterNumber;
        /**
         * Номер дорожки БСК, на которую был записан ПД
         */
        public Integer trackNum;
        /**
         * Флаг, говорящий о том, что ПД записан/распечатан
         */
        public boolean isTicketWritten;
        /**
         * Код подразделения
         */
        public String issueUnitCode;
        /**
         * Код организации
         */
        public String ownerOrganizationCode;
        /**
         * Шифр категории пассажира
         */
        public String passengerCategory;
        /**
         * Снилс
         */
        public String snilsNumber;
        /**
         * ФИО пассажира
         */
        public String passengerFio;
        /**
         * ФИО работника ОАО «РЖД», на чьем иждивении находится пассажир (для категории пассажира «И», «Г», «Н»).
         */
        public String guardianFio;

    }

}
