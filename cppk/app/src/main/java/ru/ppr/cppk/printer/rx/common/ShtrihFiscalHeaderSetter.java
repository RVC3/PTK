package ru.ppr.cppk.printer.rx.common;

import java.util.List;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.exception.PrinterException;

/**
 * Установщик заголовка фискального документа для Штриха.
 *
 * @author Aleksandr Brazhkin
 */
public class ShtrihFiscalHeaderSetter {

    private final IPrinter printer;
    private final TextFormatter textFormatter;

    public ShtrihFiscalHeaderSetter(IPrinter printer, TextFormatter textFormatter) {
        this.printer = printer;
        this.textFormatter = textFormatter;
    }

    public void setHeader(FiscalHeaderBuilder.Params params) throws PrinterException {
        FiscalHeaderBuilder fiscalHeaderBuilder = new FiscalHeaderBuilder();
        List<String> headerLines = fiscalHeaderBuilder.buildHeaderLines(params, textFormatter);
        printer.setHeaderLines(headerLines);
    }
}
