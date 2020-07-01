package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Разовые полные в направлении «Туда» (п.1.1.7)
 * Разовые полные в направлении «Туда-обратно» (п.1.1.8)
 * Разовые детские в направлении «Туда» (п.0)
 * Разовые детские в направлении «Туда-обратно» (п.1.1.11)
 * Багаж (п.1.1.12)
 *
 * @author Aleksandr Brazhkin
 */
public class TicketDirectionTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    TicketDirectionTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        if (params.isBaggage) {
            if (params.countExceptAnnulled() > 0) {
                printer.printTextInNormalMode("СУММА          =" + textFormatter.asMoney(params.sumExceptAnnulled()));
            }
            printer.printTextInNormalMode("КОЛИЧ.         =" + params.countExceptAnnulled());
            if (params.countExceptAnnulled() > 0) {
                for (TicketTypeInDirectionTpl.Params ticketTypeParams : params.ticketTypeParamsList) {
                    new TicketTypeInDirectionTpl(ticketTypeParams, textFormatter).printToDriver(printer);
                }
            }
        } else {
            if (params.count > 0) {
                printer.printTextInNormalMode("СУММА          =" + textFormatter.asMoney(params.sum));
            }
            printer.printTextInNormalMode("КОЛИЧ.         =" + params.count);
            if (params.count > 0) {
                if (params.countAnnulled > 0) {
                    printer.printTextInNormalMode("СУММ АННУЛ.    =" + textFormatter.asMoney(params.sumAnnulled));
                    printer.printTextInNormalMode("АННУЛ.         =" + params.countAnnulled);
                    printer.printTextInNormalMode("ЗА ВЫЧЕТОМ");
                    printer.printTextInNormalMode("АННУЛ.         =" + textFormatter.asMoney(params.sumExceptAnnulled()));
                }
                printer.printTextInNormalMode(" --ПОЛНЫХ--");
                if (params.fullCountExceptAnnulled() > 0) {
                    printer.printTextInNormalMode("  СУММА        =" + textFormatter.asMoney(params.fullSumExceptAnnulled()));
                }
                printer.printTextInNormalMode("  КОЛИЧ.       =" + params.fullCountExceptAnnulled());
                if (!params.isChildTicket) {
                    printer.printTextInNormalMode(" --ЛЬГОТНЫХ--");
                    if (params.discountCountExceptAnnulled() > 0) {
                        printer.printTextInNormalMode("  СУММА        =" + textFormatter.asMoney(params.discountSumExceptAnnulled()));
                        printer.printTextInNormalMode("   ВКЛ.ТАРИФ   =" + textFormatter.asMoney(params.discountTariffExceptAnnulled()));
                        printer.printTextInNormalMode("   ВКЛ.CБОР    =" + textFormatter.asMoney(params.discountFeeExceptAnnulled()));
                    }
                    printer.printTextInNormalMode("  КОЛИЧ.       =" + params.discountCountExceptAnnulled());
                    if (params.discountCountExceptAnnulled() > 0) {
                        printer.printTextInNormalMode("  ВЫП. ДОХОД.  =" + textFormatter.asMoney(params.discountLossSumExceptAnnulled()));
                        printer.printTextInNormalMode("  ПО БСК (СОЦ+ЭТТ)");
                        printer.printTextInNormalMode("   КОЛИЧ.      =" + params.discountByBSKCountExceptAnnulled());
                        printer.printTextInNormalMode("  ПО ЭТТ");
                        printer.printTextInNormalMode("   КОЛИЧ.      =" + params.discountByETTCountExceptAnnulled());
                    }
                }
                printer.printTextInNormalMode(" --БЕЗДЕНЕЖНЫХ--");
                Integer noMoneyCountExceptAnnulled = params.noMoneyCountExceptAnnulled();
                if (noMoneyCountExceptAnnulled > 0) {
                    printer.printTextInNormalMode("   ВКЛ.CБОР    =" + textFormatter.asMoney(params.noMoneyFeeExceptAnnulled()));
                }
                printer.printTextInNormalMode("  КОЛИЧ.       =" + noMoneyCountExceptAnnulled);
                if (noMoneyCountExceptAnnulled > 0) {
                    printer.printTextInNormalMode("  ВЫП. ДОХОД.  =" + textFormatter.asMoney(params.noMoneyLossSumExceptAnnulled()));
                    printer.printTextInNormalMode("  ПО БСК (СОЦ+ЭТТ)");
                    printer.printTextInNormalMode("   КОЛИЧ.      =" + params.noMoneyByBSKCountExceptAnnulled());
                    printer.printTextInNormalMode("  ПО ЭТТ");
                    printer.printTextInNormalMode("   КОЛИЧ.      =" + params.noMoneyByETTCountExceptAnnulled());
                }
            }
        }
    }

    public static class Params {
        /**
         * Это информация о багаже
         */
        protected Boolean isBaggage = false;
        /**
         * Это информация о детском ПД
         */
        protected Boolean isChildTicket = false;
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
         * Общее количество проданных полных ПД
         */
        public int fullCount = 0;
        /**
         * Общее количество аннулированных полных ПД
         */
        public int fullCountAnnulled = 0;
        /**
         * Общее количество проданных полных ПД (Исключая аннулированные)
         */
        public int fullCountExceptAnnulled() {
            return fullCount - fullCountAnnulled;
        }
        /**
         * Сумма, уплаченная пассажирами при покупке ПД по полному тарифу
         */
        public BigDecimal fullSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным ПД по полному тарифу
         */
        public BigDecimal fullSumAnnulled = BigDecimal.ZERO;
        /**
         * Сумма, уплаченная пассажирами при покупке ПД по полному тарифу
         * (Исключая аннулированные)
         */
        public BigDecimal fullSumExceptAnnulled() {
            return fullSum.subtract(fullSumAnnulled);
        }
        /**
         * Количество проданных разовых ПД по льготному тарифу
         */
        public int discountCount = 0;
        /**
         * Общее количество аннулированных разовых ПД по льготному тарифу
         */
        public int discountCountAnnulled = 0;
        /**
         * Количество проданных разовых ПД по льготному тарифу (Исключая
         * аннулированные)
         */
        public int discountCountExceptAnnulled() {
            return discountCount - discountCountAnnulled;
        }
        /**
         * Льготный тариф
         */
        public BigDecimal discountTariff = BigDecimal.ZERO;
        /**
         * Льготный тариф было аннулировано
         */
        public BigDecimal discountTariffRepeal = BigDecimal.ZERO;
        /**
         * Льготный тариф (Исключая аннулированные)
         */
        public BigDecimal discountTariffExceptAnnulled() {
            return discountTariff.subtract(discountTariffRepeal);
        }
        /**
         * Сбор при льготном тарифе
         */
        public BigDecimal discountFee = BigDecimal.ZERO;
        /**
         * Сбор при льготном тарифе было аннулировано
         */
        public BigDecimal discountFeeRepeal = BigDecimal.ZERO;
        /**
         * Сбор при льготном тарифе (Исключая аннулированные)
         */
        public BigDecimal discountFeeExceptAnnulled() {
            return discountFee.subtract(discountFeeRepeal);
        }
        /**
         * Сумма по проданным разовым ПД по льготному тарифу
         */
        public BigDecimal discountSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным ПД по льготному тарифу
         */
        public BigDecimal discountSumAnnulled = BigDecimal.ZERO;
        /**
         * Сумма по проданным разовым ПД по льготному тарифу (Исключая
         * аннулированные)
         */
        public BigDecimal discountSumExceptAnnulled() {
            return discountSum.subtract(discountSumAnnulled);
        }
        /**
         * Сумма доплаты по проданным льготным ПД
         */
        public BigDecimal discountLossSum = BigDecimal.ZERO;
        /**
         * Сумма доплаты по аннулированным льготным ПД
         */
        public BigDecimal discountLossSumAnnulled = BigDecimal.ZERO;
        /**
         * Сумма доплаты по проданным льготным ПД (Исключая аннулированные)
         */
        public BigDecimal discountLossSumExceptAnnulled() {
            return discountLossSum.subtract(discountLossSumAnnulled);
        }
        /**
         * Количество льготных ПД, проданных по социальным картам
         */
        public int discountByBSKCount = 0;
        /**
         * Общее количество аннулированных льготных ПД, проданных по социальным
         * картам
         */
        public int discountByBSKCountAnnulled = 0;
        /**
         * Количество льготных ПД, проданных по социальным картам (Исключая
         * аннулированные)
         */
        public int discountByBSKCountExceptAnnulled() {
            return discountByBSKCount - discountByBSKCountAnnulled;
        }
        /**
         * Количество льготных ПД, проданных по ЭТТ
         */
        public int discountByETTCount = 0;
        /**
         * Общее количество аннулированных льготных ПД, проданных по ЭТТ
         */
        public int discountByETTCountAnnulled = 0;
        /**
         * Количество льготных ПД, проданных по ЭТТ (Исключая
         * аннулированные)
         */
        public int discountByETTCountExceptAnnulled() {
            return discountByETTCount - discountByETTCountAnnulled;
        }
        /**
         * Количество проданных разовых безденежных ПД
         */
        public int noMoneyCount = 0;
        /**
         * Общее количество аннулированных разовых безденежных ПД картам
         */
        public int noMoneyCountAnnulled = 0;
        /**
         * Количество проданных разовых безденежных ПД (Исключая аннулированные)
         */
        public int noMoneyCountExceptAnnulled() {
            return noMoneyCount - noMoneyCountAnnulled;
        }
        /**
         * Сбор при безденежном тарифе
         */
        public BigDecimal noMoneyFee = BigDecimal.ZERO;
        /**
         * Сбор при безденежном тарифе было аннулировано
         */
        public BigDecimal noMoneyFeeRepeal = BigDecimal.ZERO;
        /**
         * Сбор при безденежном тарифе (Исключая аннулированные)
         */
        public BigDecimal noMoneyFeeExceptAnnulled() {
            return noMoneyFee.subtract(noMoneyFeeRepeal);
        }
        /**
         * Сумма доплаты по проданным безденежным ПД
         */
        public BigDecimal noMoneyLossSum = BigDecimal.ZERO;
        /**
         * Сумма доплаты по аннулированным проданным безденежным ПД
         */
        public BigDecimal noMoneyLossSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма доплаты по проданным безденежным ПД (Исключая аннулированные)
         */
        public BigDecimal noMoneyLossSumExceptAnnulled() {
            return noMoneyLossSum.subtract(noMoneyLossSumAnnulled);
        }
        /**
         * Количество безденежных ПД, проданных по социальным картам
         */
        public int noMoneyByBSKCount = 0;
        /**
         * Общее количество аннулированных ПД, проданных по социальным картам
         */
        public int noMoneyByBSKCountAnnulled = 0;
        /**
         * Количество безденежных ПД, проданных по социальным картам (Исключая
         * аннулированные)
         */
        public int noMoneyByBSKCountExceptAnnulled() {
            return noMoneyByBSKCount - noMoneyByBSKCountAnnulled;
        }
        /**
         * Количество безденежных ПД, проданных по ЭТТ
         */
        public int noMoneyByETTCount = 0;
        /**
         * Общее количество аннулированных ПД, проданных по ЭТТ
         */
        public int noMoneyByETTCountAnnulled = 0;
        /**
         * Количество безденежных ПД, проданных по ЭТТ (Исключая
         * аннулированные)
         */
        public int noMoneyByETTCountExceptAnnulled() {
            return noMoneyByETTCount - noMoneyByETTCountAnnulled;
        }
        /**
         * Параметры для шаблонов по типу билета
         */
        public List<TicketTypeInDirectionTpl.Params> ticketTypeParamsList = Collections.emptyList();
    }
}
