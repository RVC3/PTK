package ru.ppr.cppk.printer.rx.operation.discountMonthSheet;

import java.math.BigDecimal;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Данные по льготе (п.1.4.3)
 *
 * @author Brazhkin A.V.
 */
public class ExemptionInfoTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    ExemptionInfoTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(" --ЛЬГОТА " + params.number + "--");
        printer.printTextInNormalMode("  ВСЕГО        =" + params.totalCountExceptAnnulled());
        if (params.childCount > 0) {
            printer.printTextInNormalMode("  ДЕТСКИХ      =" + params.childCountExceptAnnulled());
        }
        printer.printTextInNormalMode("  ВЫП. ДОХОД   =" + textFormatter.asMoney(params.lossSumExceptAnnulled()));
    }

    public static class Params {

        /**
         * Номер льготы
         */
        public Integer number = 0;
        /**
         * Количество ПД, оформленных по данной льготе
         */
        public Integer totalCount = 0;
        /**
         * Количество аннулированных ПД, оформленных по данной льготе
         */
        public Integer totalCountAnnulled = 0;

        /**
         * Количество ПД, оформленных по данной льготе (Исключая аннулированные)
         */
        public Integer totalCountExceptAnnulled() {
            return totalCount - totalCountAnnulled;
        }

        /**
         * Количество детских ПД, оформленных по данной льготе
         */
        public Integer childCount = 0;
        /**
         * Количество аннулированных детских ПД, оформленных по данной льготе
         */
        public Integer childCountAnnulled = 0;

        /**
         * оличество детских ПД, оформленных по данной льготе (Исключая аннулированные)
         */
        public Integer childCountExceptAnnulled() {
            return childCount - childCountAnnulled;
        }

        /**
         * Выпадающий доход по данной льготе
         */
        public BigDecimal lossSum = BigDecimal.ZERO;
        /**
         * Выпадающий доход по данной льготе по аннулированным ПД
         */
        public BigDecimal lossSumAnnulled = BigDecimal.ZERO;

        /**
         * Выпадающий доход по данной льготе (Исключая аннулированные)
         */
        public BigDecimal lossSumExceptAnnulled() {
            return lossSum.subtract(lossSumAnnulled);
        }

    }

}
