package ru.ppr.cppk.printer.rx.operation.auditTrail;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * @author Dmitry Nevolin
 */
public class TransferSaleTpl extends PrinterTpl {

    private final TransferSaleTpl.Params params;
    private final TextFormatter textFormatter;

    TransferSaleTpl(TransferSaleTpl.Params params, TextFormatter textFormatter) {
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
        printer.printTextInNormalMode(params.name);

        if (params.canceled) {
            // http://agile.srvdev.ru/browse/CPPKPP-44031
            // Надо бы вывести справа от наименования, но там нет места
            printer.printTextInNormalMode(textFormatter.alignWidthText("", "АННУЛ."));
        }

        if (params.parentPdNumber != null) {
            printer.printTextInNormalMode(" к Док. №" + textFormatter.asStr06d(params.parentPdNumber) + " ID " + params.parentPdDeviceId);
            printer.printTextInNormalMode(" от " + textFormatter.asDate_dd_MM_yyyy_HH_mm(params.parentPdSaleDateTime));
        }

        printer.printTextInNormalMode(textFormatter.asMoney(params.cost));
        // В связи с тем, что в НСИ заведен тариф на трансфер только в одну сторону,
        // то маршрут трансфера в чеке выводить через дефис,
        // не используя указатели направлений <=> или =>. Т.е. выводить АВТО ДОМОДЕД - АВТО Н.ДОМОДЕД
        // см. http://agile.srvdev.ru/browse/CPPKPP-37099
        String direction = "-";
        String departureStation = params.departureStationName.toUpperCase(Locale.getDefault());
        String destinationStation = params.destinationStationName.toUpperCase(Locale.getDefault());

        int stationsStrLength = departureStation.length() + direction.length() + destinationStation.length();

        if (stationsStrLength > textFormatter.getWidth()) {
            printer.printTextInNormalMode(departureStation + direction);
            printer.printTextInNormalMode(destinationStation);
        } else {
            printer.printTextInNormalMode(departureStation + direction + destinationStation);
        }
        if (params.bscType != null) {
            // Если трансфер записан на карту
            printer.printTextInNormalMode(params.bscType + ": " + params.bscOuterNumber + " -" + params.trackNum);
        }

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
         * Дата и время оформления трансфера
         */
        public Date saleDateTime;
        /**
         * Наименование трансфера
         */
        public String name;
        /**
         * Признак последующего аннулирования ПД
         */
        public boolean canceled = false;
        /**
         * Стоимость трансфера
         */
        public BigDecimal cost = BigDecimal.ZERO;
        /**
         * Наименование станции отправления
         */
        public String departureStationName = null;
        /**
         * Наименование станции назначения
         */
        public String destinationStationName = null;
        /**
         * Номер дорожки БСК, на которую был записан ПД
         */
        public Integer trackNum;
        /**
         * Тип БСК, на которую записан ПД
         */
        public String bscType;
        /**
         * внешний номер БСК, на которую записан ПД
         */
        public String bscOuterNumber;
        /**
         * Признак оплаты ПД безналичным способом
         */
        public Boolean isNonCash = false;
        /**
         * Маскированный номер банковской карты
         */
        public String bankCardPan = "";
        /**
         * Номер родительского ПД
         */
        public Integer parentPdNumber = null;
        /**
         * Ид оборудования, продавшего исходный ПД
         */
        public Long parentPdDeviceId = null;
        /**
         * Время продажи исходного ПД
         */
        public Date parentPdSaleDateTime = null;

    }

    interface TextFormatter {

        String asStr06d(int number);

        String asDate_dd_MM_yyyy_HH_mm_ss(Date dateTime);

        String asDate_dd_MM_yyyy_HH_mm(Date dateTime);

        String asMoney(BigDecimal value);

        int getWidth();

        String smallDelimiter();

        String alignWidthText(String first, String second);

        String asMaskedBankCardNumber(String cardPan);

    }

}
