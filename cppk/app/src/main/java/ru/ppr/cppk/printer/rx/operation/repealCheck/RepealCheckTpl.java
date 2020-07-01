package ru.ppr.cppk.printer.rx.operation.repealCheck;

import java.util.Date;

import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.ikkm.TextStyle;
import ru.ppr.ikkm.exception.PrinterException;

/**
 * @author Aleksandr Brazhkin
 */
public class RepealCheckTpl {

    private final Printer printer;
    private final TextFormatter textFormatter;
    private final Params params;

    RepealCheckTpl(Printer printer, TextFormatter textFormatter, Params params) {
        this.printer = printer;
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public void printToDriver() throws Exception {
        printer.printText("Документ № " + textFormatter.asStr06d(params.pdNumber));

        if (params.printCheckDateTime != null) {
            printer.printText(textFormatter.asDate_dd_MM_yyyy_HH_mm(params.printCheckDateTime));
        }

        printer.printText(" ");

        printer.printText(textFormatter.alignCenter("АННУЛИРОВАНИЕ", TextStyle.FISCAL_LARGE), TextStyle.FISCAL_LARGE);
        printer.printText("Док. №" + textFormatter.asStr06d(params.repealPdNumber)
                + "  " + textFormatter.asDate_HH_mm_ss(params.repealPdSaleTime));

        if (params.smartCard != null) {
            String smartCardTypeAndNumber = params.smartCard.getType().getAbbreviation() + " № " + params.smartCard.getOuterNumber();
            if (smartCardTypeAndNumber.length() > textFormatter.getWidth()) {
                printer.printText(params.smartCard.getType().getAbbreviation() + " №");
                printer.printText(params.smartCard.getOuterNumber());
            } else
                printer.printText(smartCardTypeAndNumber);
            printer.printText("ЗАПИСЬ " + String.valueOf(params.smartCard.getTrack() + 1));
        }

        printer.printText("ПРИЧИНА");
        printer.printText(params.repealReason);
        // Было предложено убрать вообще,
        // см. скрин 1 http://agile.srvdev.ru/browse/CPPKPP-35362
//        printer.printText(" ");
//        printer.printText("ЧЕК ВОЗВРАТА ПРОДАЖИ");
//        printer.printText("СУММА ЗА ДОКУМЕНТ");
        printer.printText("ПОЛНАЯ СТОИМОСТЬ");
    }

    /**
     * Класс-контейнер входных параметров
     */
    public static class Params {
        /**
         * Время печати чека
         */
        public Date printCheckDateTime;
        /**
         * Смарт-карта, на которую записан билет
         */
        public SmartCard smartCard;
        /**
         * Номер документа
         */
        public Integer pdNumber;
        /**
         * Номер аннлируемого ПД
         */
        public Integer repealPdNumber;
        /**
         * Дата продажи аннлируемого ПД
         */
        public Date repealPdSaleTime;
        /**
         * Причина аннлирования
         */
        public String repealReason;
        /**
         * Вид ПД
         */
        public String repealPdTicketTypeName;
    }

    interface TextFormatter {
        String asStr06d(int number);

        String alignCenter(String text);

        String alignCenter(String text, TextStyle textStyle);

        int getWidth();

        String asDate_dd_MM_yyyy_HH_mm(Date dateTime);

        String asDate_HH_mm_ss(Date date);
    }

    interface Printer {
        void printText(String text) throws PrinterException;

        void printText(String text, TextStyle textStyle) throws PrinterException;
    }
}
