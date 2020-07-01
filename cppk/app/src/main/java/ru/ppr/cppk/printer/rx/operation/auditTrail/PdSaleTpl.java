package ru.ppr.cppk.printer.rx.operation.auditTrail;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Оформление ПД (п.1.5.4)
 * Оформление ПД на БСК (п.1.5.4)
 * Оформление ПД при переполнении БСК (п.1.5.7)
 *
 * @author Aleksandr Brazhkin
 */
public class PdSaleTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    PdSaleTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {
        String paymentMethodWithRoute = "";

        if (!params.isTestPD) {
            if (params.isNonCash) {
                paymentMethodWithRoute = "БАНК.КАРТА ";
            }
            paymentMethodWithRoute += "М " + params.trainCategory;
        }
        printer.printTextInNormalMode(textFormatter.alignWidthText(textFormatter.asStr06d(params.PDNumber), paymentMethodWithRoute));
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.sellDateTime));
        if (params.isTestPD) {
            printer.printTextInNormalMode("РАЗОВЫЙ ПРОБНЫЙ");
        } else {
            if (params.isCanceled) {
                String canceled = "АННУЛ.";
                canceled = textFormatter.alignWidthText(params.PDType, canceled);
                printer.printTextInNormalMode(canceled);
            } else {
                printer.printTextInNormalMode(params.PDType);
            }
        }
        if (!params.isTestPD) {
            if (Decimals.moreThanZero(params.incomeLoss)) {
                printer.printTextInNormalMode("СУММА         =" + textFormatter.asMoney(params.fullCostSum));
                printer.printTextInNormalMode("ВЫП. ДОХОД    =" + textFormatter.asMoney(params.incomeLoss));
                printer.printTextInNormalMode("СБОР          =" + textFormatter.asMoney(params.feeSum));
            } else {
                printer.printTextInNormalMode(textFormatter.asMoney(params.fullCostSum) + " СБОР=" + textFormatter.asMoney(params.feeSum));
            }
        } else {
            printer.printTextInNormalMode(textFormatter.asMoney(params.fullCostSum));
        }
        if (!params.isTestPD) {
            if (params.isThereBack) {
                printer.printTextInNormalMode("" + params.departureStationName + "<=>" + params.destinationStationName);
            } else {
                printer.printTextInNormalMode("" + params.departureStationName + "==>" + params.destinationStationName);
            }
        }
        if (params.writeToSmartCardError != null) {
            if (params.exemptionBSCType != null) {
                // Льгота считана с карты
                printer.printTextInNormalMode("ЛЬГ." + params.exemptionNum + " " + params.exemptionBSCType + ":");
            }
            printer.printTextInNormalMode("ОШИБКА ЗАПИСИ БСК - " + textFormatter.asStr02d(params.writeToSmartCardError));
            if (params.BSKOuterNumber != null) {
                printer.printTextInNormalMode(params.BSCType + ": " + params.BSKOuterNumber);
            }
            if (params.isTicketWritten && params.trackNum != null) {
                printer.printTextInNormalMode("ЗАПИСЬ");
                printer.printTextInNormalMode(params.BSCType + ": " + params.BSKOuterNumber + " -" + params.trackNum);
            }
        } else if (params.exemptionBSCType == null) {
            // Нет льготы считанной с карты
            if (params.exemptionNum != 0) {
                // Но, ввели льготу руками
                printer.printTextInNormalMode("ЛЬГ." + params.exemptionNum);
                if (params.preferentialDocumentNum == null) {
                    printer.printTextInNormalMode("№ ЛЬГ.ДОК: не задано");
                } else {
                    printer.printTextInNormalMode("№ ЛЬГ.ДОК: " + params.preferentialDocumentNum);
                }
                if (params.preferentialDocumentFIO == null) {
                    printer.printTextInNormalMode("ФИО: не задано");
                } else {
                    printer.printTextInNormalMode("ФИО: " + params.preferentialDocumentFIO);
                }
            }
            if (params.BSKOuterNumber != null) {
                // Записали на БСК
                if (params.trackNum == null) {
                    printer.printTextInNormalMode(params.BSCType + ": " + params.BSKOuterNumber);
                } else {
                    printer.printTextInNormalMode(params.BSCType + ": " + params.BSKOuterNumber + " -" + params.trackNum);
                }
            }
        } else {
            // Льгота считана с карты
            printer.printTextInNormalMode("ЛЬГ." + params.exemptionNum + " " + params.exemptionBSCType + ":");
            if (params.overflowBCSInfo != null) {
                // На карте не было места, билет распечатали
                printer.printTextInNormalMode(params.exemptionBSKOuterNumber);
                printer.printTextInNormalMode("ПЕЧАТЬ. " + params.overflowBCSInfo.BCSType + " ЗАПОЛНЕНА");
                if (params.overflowBCSInfo.PDNumber1 != null) {
                    printer.printTextInNormalMode(
                            textFormatter.asStr06d(params.overflowBCSInfo.PDNumber1) +
                                    " ID ККТ " + String.valueOf(params.overflowBCSInfo.PTKNumber1)
                    );
                    printer.printTextInNormalMode("   " + textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.overflowBCSInfo.sellDateTime1));
                }
                if (params.overflowBCSInfo.PDNumber2 != null) {
                    printer.printTextInNormalMode(
                            textFormatter.asStr06d(params.overflowBCSInfo.PDNumber2) +
                                    " ID ККТ " + String.valueOf(params.overflowBCSInfo.PTKNumber2)
                    );
                    printer.printTextInNormalMode("   " + textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.overflowBCSInfo.sellDateTime2));
                }
            } else {
                printer.printTextInNormalMode(params.exemptionBSKOuterNumber + " -" + params.trackNum);
            }
        }

        if (!params.isTestPD) {
            if (params.isNonCash) {
                printer.printTextInNormalMode("ОПЛАТА: " + textFormatter.asMaskedBankCardNumber(params.bankCardPan));
            }
        }

        printer.printTextInNormalMode(textFormatter.smallDelimiter());
    }

    public static class Params {

        /**
         * является пробным билетом
         */
        public Boolean isTestPD = false;
        /**
         * порядковый номер печатаемого ПД
         */
        public Integer PDNumber = 0;
        /**
         * Признак оплаты ПД безналичным способом
         */
        public Boolean isNonCash = false;
        /**
         * Маскированный номер банковской карты
         */
        public String bankCardPan = "";
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
         * Признак последующего аннулирования ПД
         */
        public Boolean isCanceled = false;
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
        /**
         * Информация о двух действующих ПД на карте
         */
        public BCSOverflowInfo overflowBCSInfo;
        /**
         * Код ошибки записи ПД на БСК
         */
        public Integer writeToSmartCardError;

    }

    public static class BCSOverflowInfo {
        /**
         * Тип переполненной БСК
         */
        public String BCSType;
        /**
         * Номер ПД, который записан на переполненной БСК
         */
        public Integer PDNumber1;
        public Integer PDNumber2;
        /**
         * Номер конечного оборудования, на котором были проданы ПД, записанные
         * на переполненной БСК
         */
        public Long PTKNumber1;
        public Long PTKNumber2;
        /**
         * Дата и время продажи ПД, которые записаны на переполненной БСК
         */
        public Date sellDateTime1;
        public Date sellDateTime2;

    }

}
