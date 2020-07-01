package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.math.BigDecimal;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Статистика по маршрутам (группировка по категории поезда) (п.1.1.15)
 *
 * @author Aleksandr Brazhkin
 */
public class RouteCarrierTrainCategoryStatisticsTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    RouteCarrierTrainCategoryStatisticsTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        // Описание логики формирования данных для полей
        // http://agile.srvdev.ru/browse/CPPKPP-42948
        // В блоке «статистика по маршрутам» сумму в выручке указывать – тарифную (т.е. стоимость билетов), без добавления суммы сборов.
        // В полях «полных», «льготных», «безденежных» указывать сумму за вычетом аннулирований .
        printer.printTextInNormalMode(params.tariffName);
        printer.printTextInNormalMode("ПЕРЕВОЗЧИК \"" + params.carrierId + "\"");
        printer.printTextInNormalMode("ПРОЕЗД \"" + params.tripType + "\"");
        if (params.countExceptAnnulled() > 0) {
            printer.printTextInNormalMode("ВЫРУЧКА        =" + textFormatter.asMoney(params.totalSumExceptFee()));
        }
        printer.printTextInNormalMode("КОЛИЧ.         =" + params.count);
        if (params.countExceptAnnulled() > 0) {
            printer.printTextInNormalMode(" --ПОЛНЫХ--");
            if (params.fullCountExceptAnnulled() > 0) {
                printer.printTextInNormalMode("  СУММА        =" + textFormatter.asMoney(params.fullSumAnnulled()));
            }
            printer.printTextInNormalMode("  КОЛИЧ.       =" + params.fullCountExceptAnnulled());
            printer.printTextInNormalMode(" --ЛЬГОТНЫХ--");
            if (params.discountCountExceptAnnulled() > 0) {
                printer.printTextInNormalMode("  СУММА        =" + textFormatter.asMoney(params.discountSumExceptAnnulled()));
            }
            printer.printTextInNormalMode("  КОЛИЧ.       =" + params.discountCountExceptAnnulled());
            printer.printTextInNormalMode(" --БЕЗДЕНЕЖНЫХ--");
            printer.printTextInNormalMode("  КОЛИЧ.       =" + params.noMoneyCountExceptAnnulled());
            if (params.noMoneyCountExceptAnnulled() > 0) {
                printer.printTextInNormalMode("  ВЫП. ДОХОД.  =" + textFormatter.asMoney(params.noMoneyLossSumExceptAnnulled()));
            }
        }
        printer.printTextInNormalMode("АННУЛИРОВАНО");
        printer.printTextInNormalMode(" СУММА         =" + textFormatter.asMoney(params.sumAnnulled));
        printer.printTextInNormalMode(" АННУЛ.        =" + params.countAnnulled);

        printer.printTextInNormalMode("ПО БАНКОВСКИМ КАРТАМ");
        printer.printTextInNormalMode(" СУММА         =" + textFormatter.asMoney(params.byBankCardSum));
        printer.printTextInNormalMode(" КОЛИЧ.        =" + params.byBankCardCount);
        printer.printTextInNormalMode(" СУММ АННУЛ.   =" + textFormatter.asMoney(params.byBankCardSumAnnulled));
        printer.printTextInNormalMode(" АННУЛ.        =" + params.byBankCardCountAnnulled);

    }

    public static class Params {

        /**
         * идентификатор перевозчика
         */
        protected String carrierId;
        /**
         * наименование тарифного плана
         */
        public String tariffName;
        /**
         * тип проезда
         */
        public String tripType;
        /**
         * сумма выручки
         */
        public BigDecimal sum = BigDecimal.ZERO;
        /**
         * сумма выручки по сборам
         */
        public BigDecimal feeSum = BigDecimal.ZERO;
        /**
         * сумма аннулированных ПД
         */
        public BigDecimal sumAnnulled = BigDecimal.ZERO;

        /**
         * Общая сумма проданных ПД (Исключая аннулированные)
         */
        public BigDecimal sumExceptAnnulled() {
            return sum.subtract(sumAnnulled);
        }

        /**
         * количество проданных ПД
         */
        public Integer count = 0;
        /**
         * количество аннулированных ПД
         */
        public Integer countAnnulled = 0;

        /**
         * количество проданных ПД (Исключая аннулированные)
         */
        public Integer countExceptAnnulled() {
            return count - countAnnulled;
        }

        /**
         * Сумма, уплаченная пассажирами в кассу при покупке ПД по полному
         * тарифу и по детскому тарифу
         */
        public BigDecimal fullSum = BigDecimal.ZERO;
        /**
         * Сумма аннулированных ПД по полному тарифу и по детскому тарифу
         */
        public BigDecimal fullSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, уплаченная пассажирами в кассу при покупке ПД по полному
         * тарифу и по детскому тарифу (Исключая аннулированные)
         */
        public BigDecimal fullSumAnnulled() {
            return fullSum.subtract(fullSumAnnulled);
        }

        /**
         * Общее количество проданных полных ПД и детских ПД
         */
        public Integer fullCount = 0;
        /**
         * Общее количество аннулированных полных ПД и детских ПД
         */
        public Integer fullCountAnnulled = 0;

        /**
         * Общее количество проданных полных ПД и детских ПД (Исключая
         * аннулированные)
         */
        public Integer fullCountExceptAnnulled() {
            return fullCount - fullCountAnnulled;
        }

        /**
         * Сумма, уплаченная пассажирами в кассу при покупке льготных ПД
         */
        public BigDecimal discountSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным льготным ПД
         */
        public BigDecimal discountSumAnnulled = BigDecimal.ZERO;

        /**
         * Cумма, уплаченная пассажирами в кассу при покупке льготных ПД
         * (Исключая аннулированные)
         */
        public BigDecimal discountSumExceptAnnulled() {
            return discountSum.subtract(discountSumAnnulled);
        }

        /**
         * Количество проданных льготных
         */
        public Integer discountCount = 0;
        /**
         * Количество аннулированных льготных ПД
         */
        public Integer discountCountAnnulled = 0;

        /**
         * Количество проданных льготных (Исключая аннулированные)
         */
        public Integer discountCountExceptAnnulled() {
            return discountCount - discountCountAnnulled;
        }

        /**
         * Количество проданных безденежных
         */
        public Integer noMoneyCount = 0;
        /**
         * Количество аннулированных безденежных
         */
        public Integer noMoneyCountAnnulled = 0;

        /**
         * Количество проданных безденежных (Исключая аннулированные)
         */
        public Integer noMoneyCountExceptAnnulled() {
            return noMoneyCount - noMoneyCountAnnulled;
        }

        /**
         * Выпадающий доход по проданным безденежным
         */
        public BigDecimal noMoneyLossSum = BigDecimal.ZERO;
        /**
         * Выпадающий доход по аннулированным безденежным
         */
        public BigDecimal noMoneyLossSumAnnulled = BigDecimal.ZERO;

        /**
         * Выпадающий доход по проданным безденежным (Исключая аннулированные)
         */
        public BigDecimal noMoneyLossSumExceptAnnulled() {
            return noMoneyLossSum.subtract(noMoneyLossSumAnnulled);
        }

        /**
         * сумма ПД, оплаченных по банковским картам
         */
        public BigDecimal byBankCardSum = BigDecimal.ZERO;
        /**
         * сумма аннулированных ПД, оплаченных по банковским картам
         */
        public BigDecimal byBankCardSumAnnulled = BigDecimal.ZERO;

        /**
         * сумма ПД, оплаченных по банковским картам (Исключая аннулированные)
         */
        public BigDecimal byBankCardSumExceptAnnulled() {
            return byBankCardSum.subtract(byBankCardSumAnnulled);
        }

        /**
         * количество ПД, оплаченных по банковской карте
         */
        public Integer byBankCardCount = 0;
        /**
         * количество аннулированных ПД, оплаченных по банковской карте
         */
        public Integer byBankCardCountAnnulled = 0;

        /**
         * количество ПД, оплаченных по банковской карте (Исключая
         * аннулированные)
         */
        public Integer byBankCardCountExceptAnnulled() {
            return byBankCardCount - byBankCardCountAnnulled;
        }

        /**
         * Выручка за вычетом сборов
         */
        public BigDecimal totalSumExceptFee() {
            return sum.subtract(feeSum);
        }

    }

}
