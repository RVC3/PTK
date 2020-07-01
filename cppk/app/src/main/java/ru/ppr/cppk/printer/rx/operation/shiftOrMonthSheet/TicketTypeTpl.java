package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.math.BigDecimal;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Багаж (п.1.1.12)
 * Суммарные данные по разовым полным ПД (п.1.1.6)
 * Суммарные данные по разовым детским ПД (п.1.1.9)
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTypeTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    TicketTypeTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {
        printer.printTextInNormalMode("СУММА          =" + textFormatter.asMoney(params.sum));
        printer.printTextInNormalMode("КОЛИЧ.         =" + params.count);

        if (params.count > 0) {
            if (params.countAnnulled > 0) {
                printer.printTextInNormalMode("СУММ АННУЛ.    =" + textFormatter.asMoney(params.sumAnnulled));
                printer.printTextInNormalMode("АННУЛ.         =" + params.countAnnulled);
                printer.printTextInNormalMode("ЗА ВЫЧЕТОМ");
                printer.printTextInNormalMode("АННУЛ.         =" + textFormatter.asMoney(params.sumExceptAnnulled()));
            }

            if (!params.isTransferTicket) {
                printer.printTextInNormalMode(" ВКЛЮЧАЯ СБОР");
                printer.printTextInNormalMode(" ЗА ВЫЧЕТ АНН. =" + textFormatter.asMoney(params.feeSumExceptAnnulled()));
            }

            if (!params.isTransferTicket && params.extraChargeCount > 0) {
                printer.printTextInNormalMode(" ВКЛЮЧАЯ ДОПЛАТУ ДО 7000");
                printer.printTextInNormalMode("  СУММА        =" + textFormatter.asMoney(params.extraChargeSumExceptAnnulled()));
                printer.printTextInNormalMode("  КОЛИЧ.       =" + params.extraChargeCountExceptAnnulled());
            }

            /*
            https://aj.srvdev.ru/browse/CPPKPP-28378
            Если количество проданных ПД за вычетом аннулированных > 0, то НДС выводится.
             */
            if (((params.there.count - params.there.countAnnulled) > 0)
                    || (params.thereBack.count - params.thereBack.countAnnulled) > 0) {
                if (params.isTransferTicket) {
                    printer.printTextInNormalMode(" НДС           =" + textFormatter.asMoney(params.VATSumExceptAnnulled()));
                } else {
                    printer.printTextInNormalMode("НДС            =" + textFormatter.asMoney(params.VATSumExceptAnnulled()));
                }
            }

            BigDecimal byBankCardTotalSumExceptAnnulled = params.byBankCardTotalSumExceptAnnulled();
            BigDecimal byBankCardTariffSumExceptAnnulled = params.byBankCardTariffSumExceptAnnulled();
            BigDecimal byBankCardFeeSumExceptAnnulled = params.byBankCardFeeSumExceptAnnulled();
            BigDecimal byCashTotalSumExceptAnnulled = params.byCashTotalSumExceptAnnulled();
            BigDecimal byCashTariffSumExceptAnnulled = params.byCashTariffSumExceptAnnulled();
            BigDecimal byCashFeeSumExceptAnnulled = params.byCashFeeSumExceptAnnulled();

            //https://aj.srvdev.ru/browse/CPPKPP-28355
            //Если количество проданных за вычетом аннул. > 0, то поля
            //"по банковским картам" и "наличные" выводятся, хоть и нулевые.
            //Если "по банковским картам" != 0.00, то "тариф" и "сбор" выводим независимо от значения
            printer.printTextInNormalMode("ПО БАНКОВСКИМ");
            printer.printTextInNormalMode("КАРТАМ         =" + textFormatter.asMoney(byBankCardTotalSumExceptAnnulled));

            //https://aj.srvdev.ru/browse/CPPKPP-28355
            //Если поле "наличными" = 0.00 (т.е. количество проданных за наличные за вычетом
            //аннулированных = 0.00), то поля "тариф" и "сбор" под "наличными" выводить не надо.
            //+ сдвигаем на 3 символа влево
            //Аналогично для "по банковским картам"
            if (Decimals.moreThanZero(byBankCardTotalSumExceptAnnulled)) {
                printer.printTextInNormalMode(" ТАРИФ(БАНК)   =" + textFormatter.asMoney(byBankCardTariffSumExceptAnnulled));
                printer.printTextInNormalMode(" СБОР(БАНК)    =" + textFormatter.asMoney(byBankCardFeeSumExceptAnnulled));
            }

            //https://aj.srvdev.ru/browse/CPPKPP-28355
            //см. на 18 строчек выше
            //+ сдвигаем на 1 символ влево
            printer.printTextInNormalMode("НАЛИЧНЫМИ      =" + textFormatter.asMoney(byCashTotalSumExceptAnnulled));

            //https://aj.srvdev.ru/browse/CPPKPP-28355
            //см. на 17 строчек выше
            if (Decimals.moreThanZero(byCashTotalSumExceptAnnulled)) {
                printer.printTextInNormalMode(" ТАРИФ         =" + textFormatter.asMoney(byCashTariffSumExceptAnnulled));
                printer.printTextInNormalMode(" СБОР          =" + textFormatter.asMoney(byCashFeeSumExceptAnnulled));
            }

            if (!params.isTransferTicket) {
                printer.printTextInNormalMode("--> (в одну сторону)");
                params.there.isBaggage = params.isBaggage;
                params.there.isChildTicket = params.isChildTicket;
                new TicketDirectionTpl(params.there, textFormatter).printToDriver(printer);
                printer.printTextInNormalMode("<--> (туда-обратно)");
                params.thereBack.isBaggage = params.isBaggage;
                params.thereBack.isChildTicket = params.isChildTicket;
                new TicketDirectionTpl(params.thereBack, textFormatter).printToDriver(printer);
            }
        }

        printer.printTextInNormalMode(textFormatter.bigDelimiter());
    }

    public static class Params {
        /**
         * Подробно, в направлении "туда"
         */
        public TicketDirectionTpl.Params there = new TicketDirectionTpl.Params();
        /**
         * Подробно, в направлении "туда-обратно"
         */
        public TicketDirectionTpl.Params thereBack = new TicketDirectionTpl.Params();
        /**
         * Это информация о багаже
         */
        protected Boolean isBaggage = false;
        /**
         * Это информация о детском ПД
         */
        protected Boolean isChildTicket = false;
        /**
         * Это информация о трансфере
         */
        protected Boolean isTransferTicket = false;
        /**
         * Общее количество проданных ПД
         */
        public Integer count = 0;
        /**
         * Общее количество аннулированных ПД
         */
        public Integer countAnnulled = 0;

        /**
         * Общее количество проданных ПД (Исключая аннулированные)
         */
        public Integer countExceptAnnulled() {
            return count - countAnnulled;
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

        /**
         * Сумма дополнительных сборов за вычетом аннулированных ПД
         */
        public BigDecimal feeSum = BigDecimal.ZERO;
        /**
         * Сумма дополнительных сборов по аннулированным ПД
         */
        public BigDecimal feeSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма дополнительных сборов (Исключая аннулированные)
         */
        public BigDecimal feeSumExceptAnnulled() {
            return feeSum.subtract(feeSumAnnulled);
        }

        /**
         * сумма НДС, выделенная при продаже ПД
         */
        public BigDecimal VATSum = BigDecimal.ZERO;
        /**
         * сумма НДС, выделенная по аннулированным ПД
         */
        public BigDecimal VATSumAnnulled = BigDecimal.ZERO;

        /**
         * сумма НДС, выделенная при продаже ПД (Исключая аннулированные)
         */
        public BigDecimal VATSumExceptAnnulled() {
            return VATSum.subtract(VATSumAnnulled);
        }


        /**
         * сумма оплаты ПД по банковским картам
         */
        public BigDecimal byBankCardTotalSum = BigDecimal.ZERO;
        /**
         * сумма оплаты по аннулированным ПД по банковским картам
         */
        public BigDecimal byBankCardTotalSumAnnulled = BigDecimal.ZERO;

        /**
         * сумма оплаты ПД по банковским картам (Исключая аннулированные)
         */
        public BigDecimal byBankCardTotalSumExceptAnnulled() {
            return byBankCardTotalSum.subtract(byBankCardTotalSumAnnulled);
        }

        /**
         * сумма оплаты ПД по банковским картам в разрезе тарифа
         */
        public BigDecimal byBankCardTariffSum = BigDecimal.ZERO;
        /**
         * сумма оплаты по аннулированным ПД по банковским картам в разрезе тарифа
         */
        public BigDecimal byBankCardTariffSumAnnulled = BigDecimal.ZERO;

        /**
         * сумма оплаты ПД по банковским картам (Исключая аннулированные) в разрезе тарифа
         */
        public BigDecimal byBankCardTariffSumExceptAnnulled() {
            return byBankCardTariffSum.subtract(byBankCardTariffSumAnnulled);
        }

        /**
         * сумма оплаты ПД по банковским картам в разрезе суммы сбора
         */
        public BigDecimal byBankCardFeeSum = BigDecimal.ZERO;
        /**
         * сумма оплаты по аннулированным ПД по банковским картам в разрезе суммы сбора
         */
        public BigDecimal byBankCardFeeSumAnnulled = BigDecimal.ZERO;

        /**
         * сумма оплаты ПД по банковским картам (Исключая аннулированные) в разрезе суммы сбора
         */
        public BigDecimal byBankCardFeeSumExceptAnnulled() {
            return byBankCardFeeSum.subtract(byBankCardFeeSumAnnulled);
        }


        /**
         * сумма оплаты ПД наличными
         */
        public BigDecimal byCashTotalSum = BigDecimal.ZERO;
        /**
         * сумма оплаты по аннулированным ПД наличными
         */
        public BigDecimal byCashTotalSumAnnulled = BigDecimal.ZERO;

        /**
         * сумма оплаты ПД наличными (Исключая аннулированные)
         */
        public BigDecimal byCashTotalSumExceptAnnulled() {
            return byCashTotalSum.subtract(byCashTotalSumAnnulled);
        }

        /**
         * сумма оплаты ПД наличными в разрезе тарифа
         */
        public BigDecimal byCashTariffSum = BigDecimal.ZERO;
        /**
         * сумма оплаты по аннулированным ПД наличными в разрезе тарифа
         */
        public BigDecimal byCashTariffSumAnnulled = BigDecimal.ZERO;

        /**
         * сумма оплаты ПД наличными (Исключая аннулированные) в разрезе тарифа
         */
        public BigDecimal byCashTariffSumExceptAnnulled() {
            return byCashTariffSum.subtract(byCashTariffSumAnnulled);
        }

        /**
         * сумма оплаты ПД наличными в разрезе суммы сбора
         */
        public BigDecimal byCashFeeSum = BigDecimal.ZERO;
        /**
         * сумма оплаты по аннулированным ПД наличными в разрезе суммы сбора
         */
        public BigDecimal byCashFeeSumAnnulled = BigDecimal.ZERO;

        /**
         * сумма оплаты ПД наличными (Исключая аннулированные) в разрезе суммы сбора
         */
        public BigDecimal byCashFeeSumExceptAnnulled() {
            return byCashFeeSum.subtract(byCashFeeSumAnnulled);
        }


        /**
         * Сумма доплат за ПД более высокой категории
         */
        public BigDecimal extraChargeSum = BigDecimal.ZERO;
        /**
         * Сумма доплат за аннулированные ПД более высокой категории
         */
        public BigDecimal extraChargeSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма доплат за ПД более высокой категории (Исключая аннулированные)
         */
        public BigDecimal extraChargeSumExceptAnnulled() {
            return extraChargeSum.subtract(extraChargeSumAnnulled);
        }

        /**
         * Количество доплат за ПД более высокой категории
         */
        public Integer extraChargeCount = 0;

        /**
         * Количество доплат за аннулированные ПД более высокой категории
         */
        public Integer extraChargeCountAnnulled = 0;

        /**
         * Количество доплат за ПД более высокой категории (Исключая
         * аннулированные)
         */
        public Integer extraChargeCountExceptAnnulled() {
            return extraChargeCount - extraChargeCountAnnulled;
        }

    }

}
