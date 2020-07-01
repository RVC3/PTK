package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.math.BigDecimal;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Штрафы (п.1.1.14)
 *
 * @author Aleksandr Brazhkin
 */
public class FinesTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    FinesTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {
        printer.printTextInNormalMode(textFormatter.alignCenterText("ШТРАФЫ"));
        printer.printTextInNormalMode("СУММА          =" + textFormatter.asMoney(params.sum));
        printer.printTextInNormalMode("КОЛИЧ.         =" + params.count);
        if (params.byCashCount != 0) {
            printer.printTextInNormalMode("НАЛИЧНЫМИ");
            printer.printTextInNormalMode(" СУММА         =" + textFormatter.asMoney(params.byCashProfitSum));
            printer.printTextInNormalMode(" КОЛИЧ.        =" + params.byCashCount);
        }
        if (params.byCardCount != 0) {
            printer.printTextInNormalMode("ПО БАНКОВСКИМ КАРТАМ");
            printer.printTextInNormalMode(" СУММА         =" + textFormatter.asMoney(params.byCardProfitSum));
            printer.printTextInNormalMode(" КОЛИЧ.        =" + params.byCardCount);
        }
        printer.printTextInNormalMode(textFormatter.bigDelimiter());
    }

    public static class Params {
        /**
         * Сумма штрафов
         */
        public BigDecimal sum = BigDecimal.ZERO;
        /**
         * Количество штрафов
         */
        public int count;
        /**
         * Сумма оплаты по наличными
         */
        public BigDecimal byCashProfitSum = BigDecimal.ZERO;
        /**
         * Количество штрафов, оплаченных наличными
         */
        public int byCashCount;
        /**
         * Сумма оплаты по банковским картам
         */
        public BigDecimal byCardProfitSum = BigDecimal.ZERO;
        /**
         * Количество штрафов, оплаченных банковской картой
         */
        public int byCardCount;
    }

}
