package ru.ppr.cppk.printer.rx.common;

import java.util.List;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.TextStyle;

/**
 * Установщик заголовка фискального документа для Зебры.
 *
 * @author Aleksandr Brazhkin
 */
public class ZebraFiscalHeaderSetter {

    private final IPrinter printer;
    private final TextFormatter textFormatter;

    public ZebraFiscalHeaderSetter(IPrinter printer, TextFormatter textFormatter) {
        this.printer = printer;
        this.textFormatter = textFormatter;
    }

    public void setHeader(FiscalHeaderBuilder.Params params) throws Exception {
        FiscalHeaderBuilder fiscalHeaderBuilder = new FiscalHeaderBuilder();
        List<String> headerLines = fiscalHeaderBuilder.buildHeaderLines(params, textFormatter);

        // Не нужно печатать последнюю строку
        // Последняя строка устанаваливается как заголовок фикальго чека
        // Будет печататься сама автоматически.
        for (int i = 0; i < headerLines.size() - 1; i++) {
            printer.printTextInNormalMode(headerLines.get(i), TextStyle.FISCAL_NORMAL);
        }
        printer.closePage(0);
    }
}
