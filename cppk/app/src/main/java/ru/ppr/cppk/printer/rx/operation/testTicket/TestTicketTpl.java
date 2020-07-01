package ru.ppr.cppk.printer.rx.operation.testTicket;

import java.util.Date;

import ru.ppr.ikkm.TextStyle;
import ru.ppr.ikkm.exception.PrinterException;

/**
 * Шаблон пробного ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class TestTicketTpl {

    private final Printer printer;
    private final TextFormatter textFormatter;
    private final Params params;

    TestTicketTpl(Printer printer, TextFormatter textFormatter, Params params) {
        this.printer = printer;
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public void printToDriver() throws Exception {
        printer.printText("ДОКУМЕНТ №" + textFormatter.asStr06d(params.pdNumber));

        if (params.printCheckDateTime != null) {
            printer.printText(textFormatter.asDate_dd_MM_yyyy_HH_mm(params.printCheckDateTime));
        }

        printer.printText(textFormatter.alignCenter("ПРОБНЫЙ", TextStyle.TEXT_LARGE), TextStyle.TEXT_LARGE);
        String bindingStationTitle = "ст. привязки: ";
        String bindingStationValue = params.bindingStationName;
        int width = bindingStationTitle.length() + bindingStationValue.length();
        int maxWidth = textFormatter.getWidthForTextStyle(TextStyle.TEXT_NORMAL);
        if (width > maxWidth) {
            printer.printText(bindingStationTitle);
            printer.printText(bindingStationValue);
        } else {
            String bindingStation = bindingStationTitle + bindingStationValue;
            printer.printText(bindingStation);
        }
    }

    public static class Params {
        /**
         * Время печати чека
         */
        Date printCheckDateTime;

        Integer pdNumber;
        String bindingStationName;

        public Params setPdNumber(Integer pdNumber) {
            this.pdNumber = pdNumber;
            return this;
        }

        public Params setBindingStationName(String bindingStationName) {
            this.bindingStationName = bindingStationName;
            return this;
        }
    }

    interface TextFormatter {
        String asStr06d(int number);

        String alignCenter(String text);

        String alignCenter(String text, TextStyle textStyle);

        String asDate_dd_MM_yyyy_HH_mm(Date dateTime);

        int getWidthForTextStyle(TextStyle textStyle);
    }

    interface Printer {
        void printText(String text, TextStyle textStyle) throws PrinterException;

        void printText(String text) throws PrinterException;
    }
}
