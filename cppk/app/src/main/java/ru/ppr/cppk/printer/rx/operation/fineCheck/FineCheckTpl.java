package ru.ppr.cppk.printer.rx.operation.fineCheck;

import java.util.Date;
import java.util.Locale;

import ru.ppr.ikkm.TextStyle;
import ru.ppr.ikkm.exception.PrinterException;

/**
 * Шаблон чека взимания штрафа.
 *
 * @author Aleksandr Brazhkin
 */
public class FineCheckTpl {

    private final Printer printer;
    private final TextFormatter textFormatter;
    private final Params params;

    FineCheckTpl(Printer printer, TextFormatter textFormatter, Params params) {
        this.printer = printer;
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public void printToDriver() throws Exception {
        printer.printText("Документ № " + textFormatter.asStr06d(params.pdNumber), TextStyle.TEXT_NORMAL);

        if (params.printCheckDateTime != null) {
            printer.printText(textFormatter.asDate_dd_MM_yyyy_HH_mm(params.printCheckDateTime), TextStyle.TEXT_NORMAL);
        }

        printer.printText(textFormatter.alignCenter(params.fineName.toUpperCase(Locale.getDefault()), TextStyle.TEXT_LARGE), TextStyle.TEXT_LARGE);
    }

    public static class Params {
        /**
         * Время печати чека
         */
        public Date printCheckDateTime;
        /**
         * Номер документа
         */
        public Integer pdNumber;
        /**
         * Наименование штрафа
         */
        public String fineName;
    }

    interface TextFormatter {
        String asStr06d(int number);

        String alignCenter(String text, TextStyle textStyle);

        String asDate_dd_MM_yyyy_HH_mm(Date dateTime);
    }

    interface Printer {
        void printText(String text, TextStyle textStyle) throws PrinterException;
    }
}
