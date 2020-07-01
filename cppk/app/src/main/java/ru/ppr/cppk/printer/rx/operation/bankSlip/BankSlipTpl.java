package ru.ppr.cppk.printer.rx.operation.bankSlip;

import android.text.TextUtils;

import java.util.LinkedList;
import java.util.List;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.ikkm.exception.PrinterException;

/**
 * Шаблон печати строк банковского слипа
 *
 * @author Grigoriy Kashka
 */
public class BankSlipTpl {

    private final Params params;
    private final TextFormatter textFormatter;
    private final Printer printer;

    public BankSlipTpl(Printer printer, Params params, TextFormatter textFormatter) {
        this.printer = printer;
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public void printToDriver() throws Exception {

        // https://aj.srvdev.ru/browse/CPPKPP-28504
        // ПТК: уменьшить отступ после журнала операций pos-терминала
        // Так, потому что Ingenico добавляет сама в конце 8 строк пустых
        final LinkedList<String> slipLines = new LinkedList<>(params.slipLines == null ? new LinkedList<>() : params.slipLines);

        while (!slipLines.isEmpty()) {
            if (TextUtils.getTrimmedLength(slipLines.getLast()) > 0) {
                break;
            } else {
                slipLines.removeLast();
            }
        }

        for (String line : slipLines)
            printer.printText(line);
    }

    public static class Params {
        /**
         * Банковский слип построчно
         */
        public List<String> slipLines;
    }

    interface Printer {
        void printText(String text) throws PrinterException;
    }

}
