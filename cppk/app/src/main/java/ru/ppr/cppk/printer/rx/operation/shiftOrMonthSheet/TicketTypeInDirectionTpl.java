package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.math.BigDecimal;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Багаж (п.1.1.12)
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTypeInDirectionTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    TicketTypeInDirectionTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {
        if (params.countExceptAnnulled() > 0) {
            printer.printTextInNormalMode(" --" + textFormatter.alignWidthText(params.ticketTypeShortName, "=", 15) + textFormatter.asMoney(params.sumExceptAnnulled()));
            printer.printTextInNormalMode(" КОЛИЧ.        =" + params.countExceptAnnulled());
            printer.printTextInNormalMode("   ВКЛ.ТАРИФ   =" + textFormatter.asMoney(params.tariffExceptAnnulled()));
            printer.printTextInNormalMode("   ВКЛ.CБОР    =" + textFormatter.asMoney(params.feeExceptAnnulled()));
        }
    }

    public static class Params {
        /**
         * Краткое наименование типа ПД.
         */
        public String ticketTypeShortName;
        /**
         * Общее количество проданных ПД
         */
        public int count = 0;
        /**
         * Общее количество аннулированных ПД
         */
        public int countAnnulled = 0;
        /**
         * Общее количество проданных ПД (Исключая аннулированные)
         */
        public int countExceptAnnulled() {
            return count - countAnnulled;
        }
        /**
         * Тариф
         */
        public BigDecimal tariff = BigDecimal.ZERO;
        /**
         * Тариф было аннулировано
         */
        public BigDecimal tariffRepeal = BigDecimal.ZERO;
        /**
         * Тариф (Исключая аннулированные)
         */
        public BigDecimal tariffExceptAnnulled() {
            return tariff.subtract(tariffRepeal);
        }
        /**
         * Сбор
         */
        public BigDecimal fee = BigDecimal.ZERO;
        /**
         * Сбор было аннулировано
         */
        public BigDecimal feeRepeal = BigDecimal.ZERO;
        /**
         * Сбор (Исключая аннулированные)
         */
        public BigDecimal feeExceptAnnulled() {
            return fee.subtract(feeRepeal);
        }
        /**
         * Общая сумма проданных ПД
         */
        public BigDecimal sum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным ПД
         */
        public BigDecimal sumAnnulled = BigDecimal.ZERO;

        /**
         * Общая сумма проданных ПД (Исключая аннулированные)
         */
        public BigDecimal sumExceptAnnulled() {
            return sum.subtract(sumAnnulled);
        }
    }
}
