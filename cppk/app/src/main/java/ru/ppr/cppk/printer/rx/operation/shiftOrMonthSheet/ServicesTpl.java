package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.TextStyle;

/**
 * Шаблон печати.
 * Услуги (п.1.1.13)
 *
 * @author Aleksandr Brazhkin
 */
public class ServicesTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    ServicesTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(textFormatter.alignCenterText("УСЛУГИ"));
        printer.printTextInNormalMode("СУММА          =" + textFormatter.asMoney(params.totalProfitSum));
        printer.printTextInNormalMode("КОЛИЧ.         =" + params.totalCount);
        printer.printTextInNormalMode("СУММ АННУЛ.    =" + textFormatter.asMoney(params.totalProfitSumAnnulled));
        printer.printTextInNormalMode("АННУЛ.         =" + params.totalAnnulledCount);
        printer.printTextInNormalMode("ЗА ВЫЧЕТОМ");
        printer.printTextInNormalMode("АННУЛ.         =" + textFormatter.asMoney(params.totalProfitSumExceptAnnulled()));
        printer.printTextInNormalMode(" НДС           =" + textFormatter.asMoney(params.vatSumExceptAnnulled));
        printer.printTextInNormalMode("ПО БАНКОВСКИМ");
        printer.printTextInNormalMode("КАРТАМ         =" + textFormatter.asMoney(params.byBankCardProfitSumExceptAnnulled));
        for (DetailParams detail : params.detailParams) {
            String[] title = textFormatter.wordWrapText("--" + detail.name.toUpperCase(), TextStyle.TEXT_NORMAL);
            for (String s : title) {
                printer.printTextInNormalMode(s);
            }
            printer.printTextInNormalMode("   СУММА.      =" + textFormatter.asMoney(detail.totalProfitSumExceptAnnulled));
            printer.printTextInNormalMode("   КОЛИЧ.      =" + detail.totalCountExceptAnnulled);
        }
        printer.printTextInNormalMode(textFormatter.bigDelimiter());

    }
/*
    http://agile.srvdev.ru/browse/CPPKPP-42948

    СУММА                   - общая сумма без вычета аннулированных, включая услуги проданные по банку
    КОЛИЧ.                  - общее количестов проданных услуг не исключая аннулированные
    СУММ АННУЛ.             - сумма по аннулированным услугам (сборам)
    АННУЛ.                  - количество аннулированных услуг (сборов)
    ЗА ВЫЧЕТОМ АННУЛ.       - общая сумма, включая услуги проданные по банку за вычетом аннулированных
    НДС                     - сумма НДС, выделенная при продаже сборов (Исключая аннулированные)
    ПО БАНКОВСКИМ КАРТАМ    - сумма оплаты сборов по банковским картам (Исключая аннулированные)

    -- детализация сбора
        СУММА       - общая сумма сборов этого типа за вычетом аннулированных
        КОЛИЧЕСТВО  - общее количество сборов этого типа за вычетом аннулированных
*/

    public static class Params {

        /**
         * Общая сумма без вычета аннулированных, но включая услуги проданные по банку
         */
        public BigDecimal totalProfitSum = BigDecimal.ZERO;
        /**
         * Общее количестов проданных услуг не исключая аннулированные
         */
        public Integer totalCount;
        /**
         * Сумма по аннулированным услугам (сборам)
         */
        public BigDecimal totalProfitSumAnnulled = BigDecimal.ZERO;
        /**
         * Общее количестов аннулированных услуг (сборов)
         */
        public Integer totalAnnulledCount;

        /**
         * Сумма выручки по услугам (сборам) за вычетом аннулированных
         */
        BigDecimal totalProfitSumExceptAnnulled() {
            return totalProfitSum.subtract(totalProfitSumAnnulled);
        }

        /**
         * Сумма НДС, выделенная при продаже сборов (Исключая аннулированные)
         */
        public BigDecimal vatSumExceptAnnulled = BigDecimal.ZERO;
        /**
         * Сумма оплаты сборов по банковским картам (Исключая аннулированные)
         */
        public BigDecimal byBankCardProfitSumExceptAnnulled = BigDecimal.ZERO;

        public List<DetailParams> detailParams = new ArrayList<>();

    }

    public static class DetailParams {

        public DetailParams(String name, BigDecimal totalProfitSumExceptAnnulled, Integer totalCountExceptAnnulled) {
            this.name = name;
            this.totalProfitSumExceptAnnulled = totalProfitSumExceptAnnulled;
            this.totalCountExceptAnnulled = totalCountExceptAnnulled;
        }

        /**
         * Название сбора
         */
        private String name;

        /**
         * общая сумма сборов этого типа за вычетом аннулированных
         */
        private BigDecimal totalProfitSumExceptAnnulled = BigDecimal.ZERO;
        /**
         * общее количество сборов этого типа за вычетом аннулированных
         */
        private Integer totalCountExceptAnnulled;
    }
}
