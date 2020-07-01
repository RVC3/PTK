package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.math.BigDecimal;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Выручка (п.1.1.4)
 *
 * @author Aleksandr Brazhkin
 */
public class ProfitTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    ProfitTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {
        printer.printTextInNormalMode("ВЫРУЧКА");

        if (!params.isMonthly) {
            printer.printTextInNormalMode(" МЕС.          =" + textFormatter.asMoney(params.monthlyProfit));
            printer.printTextInNormalMode(" СУММ АННУЛ.   =" + textFormatter.asMoney(params.monthlyProfitAnnulled));
            printer.printTextInNormalMode(" ЗА ВЫЧЕТОМ");
            printer.printTextInNormalMode(" АННУЛ.        =" + textFormatter.asMoney(params.monthlyProfitExceptAnnulled()));
            printer.printTextInNormalMode("ШТРАФ          =" + textFormatter.asMoney(params.monthlyFineProfit));
            printer.printTextInNormalMode(" ");
            printer.printTextInNormalMode("ВЫРУЧКА ЗА СМЕНУ");
        }

        printer.printTextInNormalMode(" ВСЕГО         =" + textFormatter.asMoney(params.totalProfitSum));
        if (!params.isTestAtShiftStart && (params.totalProfitSum.compareTo(BigDecimal.ZERO) != 0)) {
            printer.printTextInNormalMode(" СУММ АННУЛ.   =" + textFormatter.asMoney(params.totalProfitSumAnnulled));
            printer.printTextInNormalMode(" ЗА ВЫЧЕТОМ");
            printer.printTextInNormalMode(" АННУЛ.        =" + textFormatter.asMoney(params.totalProfitSumExceptAnnulled()));
            printer.printTextInNormalMode("ШТРАФ          =" + textFormatter.asMoney(params.fineProfitSum));
        }

        if (Decimals.moreThanZero(params.totalProfitSum)) {
            printer.printTextInNormalMode(" ВКЛ.ЦППК      =" + textFormatter.asMoney(params.trainTotalProfitSum()));
            printer.printTextInNormalMode("  ТАРИФ        =" + textFormatter.asMoney(params.trainTariffProfitSum()));
            printer.printTextInNormalMode("  СУММ АННУЛ.  =" + textFormatter.asMoney(params.trainTariffProfitSumAnnulled()));

            printer.printTextInNormalMode("  ТАРИФ ЗА ВЫЧЕТОМ");
            printer.printTextInNormalMode("  АННУЛ.       =" + textFormatter.asMoney(params.trainTariffProfitSumExceptAnnulled()));
            printer.printTextInNormalMode("  СБОР         =" + textFormatter.asMoney(params.trainFeeProfitSum()));
            printer.printTextInNormalMode("  СУММ АННУЛ.  =" + textFormatter.asMoney(params.trainFeeProfitSumAnnulled()));

            printer.printTextInNormalMode("  СБОР ЗА ВЫЧЕТОМ");
            printer.printTextInNormalMode("  АННУЛ.       =" + textFormatter.asMoney(params.trainFeeProfitSumExceptAnnulled()));
            printer.printTextInNormalMode("    ВКЛ. НДС   =" + textFormatter.asMoney(params.trainFeeVATSumExceptAnnulled()));

            printer.printTextInNormalMode("  ЗА ВЫЧЕТОМ");
            printer.printTextInNormalMode("  АННУЛ.       =" + textFormatter.asMoney(params.trainTotalProfitSumExceptAnnulled()));

            if (Decimals.moreThanZero(params.transferTotalProfitSum)) {
                printer.printTextInNormalMode(" ВКЛ.ТРАНСФЕР  =" + textFormatter.asMoney(params.transferTotalProfitSum));
                printer.printTextInNormalMode("  ТАРИФ        =" + textFormatter.asMoney(params.transferTariffProfitSum));
                printer.printTextInNormalMode("  СУММ АННУЛ.  =" + textFormatter.asMoney(params.transferTariffProfitSumAnnulled));

                printer.printTextInNormalMode("  ТАРИФ ЗА ВЫЧЕТОМ");
                printer.printTextInNormalMode("  АННУЛ.       =" + textFormatter.asMoney(params.transferTariffProfitSumExceptAnnulled()));
                printer.printTextInNormalMode("    ВКЛ. НДС   =" + textFormatter.asMoney(params.transferTariffVATSumExceptAnnulled()));
                printer.printTextInNormalMode("  СБОР         =" + textFormatter.asMoney(params.transferFeeProfitSum));
                printer.printTextInNormalMode("  СУММ АННУЛ.  =" + textFormatter.asMoney(params.transferFeeProfitSumAnnulled));

                printer.printTextInNormalMode("  СБОР ЗА ВЫЧЕТОМ");
                printer.printTextInNormalMode("  АННУЛ.       =" + textFormatter.asMoney(params.transferFeeProfitSumExceptAnnulled()));
                printer.printTextInNormalMode("    ВКЛ. НДС   =" + textFormatter.asMoney(params.transferFeeVATSumExceptAnnulled()));

                printer.printTextInNormalMode("  ЗА ВЫЧЕТОМ");
                printer.printTextInNormalMode("  АННУЛ.       =" + textFormatter.asMoney(params.transferTotalProfitSumExceptAnnulled()));
            }
        }

        if (Decimals.moreThanZero(params.totalProfitSum)) {
            printer.printTextInNormalMode("");

            printer.printTextInNormalMode(" НАЛИЧНЫМИ     =" + textFormatter.asMoney(params.byCashTotalProfitSum));
            printer.printTextInNormalMode("  СУММ АННУЛ. =" + textFormatter.asMoney(params.byCashTotalProfitSumAnnulled));
            printer.printTextInNormalMode(" ЗА ВЫЧЕТОМ");
            printer.printTextInNormalMode(" АННУЛ.        =" + textFormatter.asMoney(params.byCashTotalProfitSumExceptAnnulled()));
            printer.printTextInNormalMode("  ВКЛ.ЦППК     =" + textFormatter.asMoney(params.byCashTrainTotalProfitSumExceptAnnulled()));
            //https://aj.srvdev.ru/browse/CPPKPP-28355
            //сдвинуть "тариф", "сбор", "тариф(банк)", "сбор(банк)"
            //на 1 символ влево, а то "тариф(банк)" прилипает к сумме
            printer.printTextInNormalMode("   ТАРИФ       =" + textFormatter.asMoney(params.byCashTariffProfitSumExceptAnnulled()));
            //https://aj.srvdev.ru/browse/CPPKPP-28355
            //см. выше
            printer.printTextInNormalMode("   СБОР        =" + textFormatter.asMoney(params.byCashFeeProfitSumExceptAnnulled()));

            if (Decimals.moreThanZero(params.byCashTransferTotalProfitSumExceptAnnulled())) {
                printer.printTextInNormalMode("  ВКЛ.ТРАНСФЕР =" + textFormatter.asMoney(params.byCashTransferTotalProfitSumExceptAnnulled()));
                printer.printTextInNormalMode("   ТАРИФ       =" + textFormatter.asMoney(params.byCashTransferTariffProfitSumExceptAnnulled()));
                printer.printTextInNormalMode("   СБОР        =" + textFormatter.asMoney(params.byCashTransferFeeProfitSumExceptAnnulled()));
            }
        }
        // Блок выводится, если были оплаты штрафов наличными
        if (params.byCashFineProfitCount > 0) {
            printer.printTextInNormalMode(" ШТРАФ");
            printer.printTextInNormalMode("   СУММА       =" + textFormatter.asMoney(params.byCashFineProfitSum));
            printer.printTextInNormalMode("   КОЛИЧ.      =" + params.byCashFineProfitCount);
        }

        if (Decimals.moreThanZero(params.totalProfitSum)) {
            printer.printTextInNormalMode(" ПО БАНКОВСКИМ");
            printer.printTextInNormalMode(" КАРТАМ        =" + textFormatter.asMoney(params.byBankCardTotalProfitSum));
            printer.printTextInNormalMode("  СУММ АННУЛ. =" + textFormatter.asMoney(params.byBankCardTotalProfitSumAnnulled));
            printer.printTextInNormalMode(" ЗА ВЫЧЕТОМ");
            printer.printTextInNormalMode(" АННУЛ.(БАНК)  =" + textFormatter.asMoney(params.byBankCardTotalProfitSumExceptAnnulled()));

            if (Decimals.moreThanZero(params.byBankCardTotalProfitSumExceptAnnulled())) {
                printer.printTextInNormalMode("  ВКЛ.ЦППК     =" + textFormatter.asMoney(params.byBankCardTrainTotalProfitSumExceptAnnulled()));
                //https://aj.srvdev.ru/browse/CPPKPP-28355
                //см. выше
                printer.printTextInNormalMode("   ТАРИФ(БАНК) =" + textFormatter.asMoney(params.byBankCardTariffProfitSumExceptAnnulled()));
                //https://aj.srvdev.ru/browse/CPPKPP-28355
                //см. выше
                printer.printTextInNormalMode("   СБОР(БАНК)  =" + textFormatter.asMoney(params.byBankCardFeeProfitSumExceptAnnulled()));
            }

            if (Decimals.moreThanZero(params.byBankCardTransferTotalProfitSumExceptAnnulled())) {
                printer.printTextInNormalMode("  ВКЛ.ТРАНСФЕР =" + params.byBankCardTransferTotalProfitSumExceptAnnulled());
                printer.printTextInNormalMode("   ТАРИФ(БАНК) =" + textFormatter.asMoney(params.byBankCardTransferTariffProfitSumExceptAnnulled()));
                printer.printTextInNormalMode("   СБОР(БАНК)  =" + textFormatter.asMoney(params.byBankCardTransferFeeProfitSumExceptAnnulled()));
            }
        }
        // Блок выводится, если были оплаты штрафов картой
        if (params.byBankCardFineProfitCount > 0) {
            printer.printTextInNormalMode(" ШТРАФ");
            printer.printTextInNormalMode("   СУММА       =" + textFormatter.asMoney(params.byBankCardFineProfitSum));
            printer.printTextInNormalMode("   КОЛИЧ.      =" + params.byBankCardFineProfitCount);
        }

        printer.printTextInNormalMode(textFormatter.bigDelimiter());
    }

    public static class Params {
        /**
         * Является месячной
         */
        Boolean isMonthly = false;
        /**
         * Является пробной на начало смены
         */
        Boolean isTestAtShiftStart = false;
        /**
         * Сумма месячной выручки
         */
        public BigDecimal monthlyProfit = BigDecimal.ZERO;
        /**
         * Сумма аннулирования за месяц
         */
        public BigDecimal monthlyProfitAnnulled = BigDecimal.ZERO;

        BigDecimal monthlyProfitExceptAnnulled() {
            return monthlyProfit.subtract(monthlyProfitAnnulled);
        }

        /**
         * Сумма выручки от продажи ПД, квитанций на багаж, услуг. + Сбор
         */
        public BigDecimal totalProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным ПД, квитанциям на багаж, услугам. + Сбор
         */
        public BigDecimal totalProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма выручки от продажи ПД, квитанций на багаж, услуг. + Сбор
         */
        BigDecimal totalProfitSumExceptAnnulled() {
            return totalProfitSum.subtract(totalProfitSumAnnulled);
        }

        /**
         * Сумма выручки от продажи ПД, квитанций на багаж, услуг. Сбор не
         * учитывается
         */
        public BigDecimal tariffProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным ПД, квитанциям на багаж, услугам. Сбор не
         * учитывается
         */
        public BigDecimal tariffProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма выручки от продажи ПД, квитанций на багаж, услуг. Сбор не
         * учитывается (Исключая аннулированные)
         */
        public BigDecimal tariffProfitSumExceptAnnulled() {
            return tariffProfitSum.subtract(tariffProfitSumAnnulled);
        }

        /**
         * Дополнительный сбор
         */
        public BigDecimal feeProfitSum = BigDecimal.ZERO;
        /**
         * Сумма сбора за оформление в поезде по аннулированным ПД
         */
        public BigDecimal feeProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Дополнительный сбор (Исключая аннулированные)
         */
        public BigDecimal feeProfitSumExceptAnnulled() {
            return feeProfitSum.subtract(feeProfitSumAnnulled);
        }

        /**
         * Сумма НДС за дополнительный сбор
         */
        public BigDecimal feeVATSum = BigDecimal.ZERO;
        /**
         * Сумма НДС по аннулированным ПД за дополнительный сбор
         */
        public BigDecimal feeVATSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма НДС за дополнительный сбор (Исключая аннулированные)
         */
        public BigDecimal feeVATSumExceptAnnulled() {
            return feeVATSum.subtract(feeVATSumAnnulled);
        }

        /**
         * Сумма, полученная наличными
         */
        public BigDecimal byCashTotalProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным ПД, полученная наличными
         */
        public BigDecimal byCashTotalProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная наличными (Исключая аннулированные)
         */
        BigDecimal byCashTotalProfitSumExceptAnnulled() {
            return byCashTotalProfitSum.subtract(byCashTotalProfitSumAnnulled);
        }

        /**
         * Сумма, полученная наличными в разрезе тарифа
         */
        public BigDecimal byCashTariffProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным ПД, полученная наличнымив разрезе тарифа
         */
        public BigDecimal byCashTariffProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная наличными (Исключая аннулированные) в разрезе тарифа
         */
        BigDecimal byCashTariffProfitSumExceptAnnulled() {
            return byCashTariffProfitSum.subtract(byCashTariffProfitSumAnnulled);
        }

        /**
         * Сумма, полученная наличными в разрезе суммы сбора
         */
        public BigDecimal byCashFeeProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным ПД, полученная наличными в разрезе суммы сбора
         */
        public BigDecimal byCashFeeProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная наличными (Исключая аннулированные) в разрезе суммы сбора
         */
        BigDecimal byCashFeeProfitSumExceptAnnulled() {
            return byCashFeeProfitSum.subtract(byCashFeeProfitSumAnnulled);
        }

        /**
         * Сумма, полученная по банковским картам
         */
        public BigDecimal byBankCardTotalProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным ПД, полученная по банковским картам
         */
        public BigDecimal byBankCardTotalProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная по банковским картам (Исключая аннулированные)
         */
        BigDecimal byBankCardTotalProfitSumExceptAnnulled() {
            return byBankCardTotalProfitSum.subtract(byBankCardTotalProfitSumAnnulled);
        }

        /**
         * Сумма, полученная по банковским картам в разрезе тарифа
         */
        public BigDecimal byBankCardTariffProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным ПД, полученная по банковским картам в разрезе тарифа
         */
        public BigDecimal byBankCardTariffProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная по банковским картам (Исключая аннулированные) в разрезе тарифа
         */
        BigDecimal byBankCardTariffProfitSumExceptAnnulled() {
            return byBankCardTariffProfitSum.subtract(byBankCardTariffProfitSumAnnulled);
        }

        /**
         * Сумма, полученная по банковским картам в разрезе суммы сбора
         */
        public BigDecimal byBankCardFeeProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным ПД, полученная по банковским картам в разрезе суммы сбора
         */
        public BigDecimal byBankCardFeeProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная по банковским картам (Исключая аннулированные) в разрезе суммы сбора
         */
        BigDecimal byBankCardFeeProfitSumExceptAnnulled() {
            return byBankCardFeeProfitSum.subtract(byBankCardFeeProfitSumAnnulled);
        }

        /**
         * Штрафы за месяц
         */
        public BigDecimal monthlyFineProfit = BigDecimal.ZERO;
        /**
         * Всего штрафов за смену
         */
        public BigDecimal fineProfitSum = BigDecimal.ZERO;
        /**
         * Штрафы, оплаченные наличными, количество
         */
        public int byCashFineProfitCount = 0;
        /**
         * Штрафы, оплаченные наличными, сумма
         */
        public BigDecimal byCashFineProfitSum = BigDecimal.ZERO;
        /**
         * Штрафы, оплаченные картой, количество
         */
        public int byBankCardFineProfitCount = 0;
        /**
         * Штрафы, оплаченные картой, сумма
         */
        public BigDecimal byBankCardFineProfitSum = BigDecimal.ZERO;

        /**
         * Сумма выручки от продажи ПД поездов + сбор
         */
        BigDecimal trainTotalProfitSum() {
            return totalProfitSum.subtract(transferTotalProfitSum);
        }

        /**
         * Сумма по аннулированным ПД поездов + сбор
         */
        BigDecimal trainTotalProfitSumAnnulled() {
            return totalProfitSumAnnulled.subtract(transferTotalProfitSumAnnulled);
        }

        /**
         * Сумма выручки от продажи ПД поездов + сбор (Исключая аннулированные)
         */
        BigDecimal trainTotalProfitSumExceptAnnulled() {
            return trainTotalProfitSum().subtract(trainTotalProfitSumAnnulled());
        }

        /**
         * Сумма выручки от продажи ПД поездов, сбор не учитывается
         */
        BigDecimal trainTariffProfitSum() {
            return tariffProfitSum.subtract(transferTariffProfitSum);
        }

        /**
         * Сумма по аннулированным ПД поездов, сбор не учитывается
         */
        BigDecimal trainTariffProfitSumAnnulled() {
            return tariffProfitSumAnnulled.subtract(transferTariffProfitSumAnnulled);
        }

        /**
         * Сумма выручки от продажи ПД поездов, сбор не учитывается (Исключая аннулированные)
         */
        BigDecimal trainTariffProfitSumExceptAnnulled() {
            return trainTariffProfitSum().subtract(trainTariffProfitSumAnnulled());
        }

        /**
         * Дополнительный сбор по ПД поездов
         */
        BigDecimal trainFeeProfitSum() {
            return feeProfitSum.subtract(transferFeeProfitSum);
        }

        /**
         * Сумма сбора по аннулированным ПД поездов
         */
        BigDecimal trainFeeProfitSumAnnulled() {
            return feeProfitSumAnnulled.subtract(transferFeeProfitSumAnnulled);
        }

        /**
         * Дополнительный сбор по ПД поездов (Исключая аннулированные)
         */
        BigDecimal trainFeeProfitSumExceptAnnulled() {
            return trainFeeProfitSum().subtract(trainFeeProfitSumAnnulled());
        }

        /**
         * Сумма НДС за дополнительный сбор за ПД поездов
         */
        BigDecimal trainFeeVATSum() {
            return feeVATSum.subtract(transferFeeVATSum);
        }

        /**
         * Сумма НДС по аннулированным ПД за дополнительный сбор за ПД поездов
         */
        BigDecimal trainFeeVATSumAnnulled() {
            return feeVATSumAnnulled.subtract(transferFeeVATSumAnnulled);
        }

        /**
         * Сумма НДС за дополнительный сбор (Исключая аннулированные)
         */
        BigDecimal trainFeeVATSumExceptAnnulled() {
            return trainFeeVATSum().subtract(trainFeeVATSumAnnulled());
        }

        /**
         * Сумма выручки от продажи трансферов + сбор
         */
        public BigDecimal transferTotalProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным трансферам + сбор
         */
        public BigDecimal transferTotalProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма выручки от продажи трансферов + сбор (Исключая аннулированные)
         */
        BigDecimal transferTotalProfitSumExceptAnnulled() {
            return transferTotalProfitSum.subtract(transferTotalProfitSumAnnulled);
        }

        /**
         * Сумма выручки от продажи трансферов, сбор не учитывается
         */
        public BigDecimal transferTariffProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным трансферам, сбор не учитывается
         */
        public BigDecimal transferTariffProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма выручки от продажи трансферов, сбор не учитывается (Исключая аннулированные)
         */
        BigDecimal transferTariffProfitSumExceptAnnulled() {
            return transferTariffProfitSum.subtract(transferTariffProfitSumAnnulled);
        }

        /**
         * Сумма НДС за выручку от продажи трансферов, сбор не учитывается
         */
        public BigDecimal transferTariffVATSum = BigDecimal.ZERO;
        /**
         * Сумма НДС по аннулированным трансферам за выручку от продажи трансферов, сбор не учитывается
         */
        public BigDecimal transferTariffVATSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма НДС за выручку от продажи (Исключая аннулированные)
         */
        BigDecimal transferTariffVATSumExceptAnnulled() {
            return transferTariffVATSum.subtract(transferTariffVATSumAnnulled);
        }

        /**
         * Дополнительный сбор по трансферам
         */
        public BigDecimal transferFeeProfitSum = BigDecimal.ZERO;
        /**
         * Сумма сбора по аннулированным трансферам
         */
        public BigDecimal transferFeeProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Дополнительный сбор по трансферам (Исключая аннулированные)
         */
        BigDecimal transferFeeProfitSumExceptAnnulled() {
            return transferFeeProfitSum.subtract(transferFeeProfitSumAnnulled);
        }

        /**
         * Сумма НДС за дополнительный сбор за трансферы
         */
        public BigDecimal transferFeeVATSum = BigDecimal.ZERO;
        /**
         * Сумма НДС по аннулированным трансферам за дополнительный сбор за трансферы
         */
        public BigDecimal transferFeeVATSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма НДС за дополнительный сбор по трансферам (Исключая аннулированные)
         */
        BigDecimal transferFeeVATSumExceptAnnulled() {
            return transferFeeVATSum.subtract(transferFeeVATSumAnnulled);
        }

        /**
         * Сумма, полученная наличными в разрезе трансферов
         */
        public BigDecimal byCashTransferTotalProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным трансферам, полученная наличными в разрезе трансферов
         */
        public BigDecimal byCashTransferTotalProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная наличными за трансферы (Исключая аннулированные) в разрезе трансферов
         */
        BigDecimal byCashTransferTotalProfitSumExceptAnnulled() {
            return byCashTransferTotalProfitSum.subtract(byCashTransferTotalProfitSumAnnulled);
        }

        /**
         * Сумма, полученная по банковским картам в разрезе трансферов
         */
        public BigDecimal byBankCardTransferTotalProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным трансферам, полученная по банковским картам в разрезе трансферов
         */
        public BigDecimal byBankCardTransferTotalProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная по банковским картам за трансферы (Исключая аннулированные) в разрезе трансферов
         */
        BigDecimal byBankCardTransferTotalProfitSumExceptAnnulled() {
            return byBankCardTransferTotalProfitSum.subtract(byBankCardTransferTotalProfitSumAnnulled);
        }

        /**
         * Сумма, полученная наличными за поезда (Исключая аннулированные) в разрезе поездов
         */
        BigDecimal byCashTrainTotalProfitSumExceptAnnulled() {
            return byCashTotalProfitSumExceptAnnulled().subtract(byCashTransferTotalProfitSumExceptAnnulled());
        }

        /**
         * Сумма, полученная по банковским картам за поезда (Исключая аннулированные) в разрезе поездов
         */
        BigDecimal byBankCardTrainTotalProfitSumExceptAnnulled() {
            return byBankCardTotalProfitSumExceptAnnulled().subtract(byBankCardTransferTotalProfitSumExceptAnnulled());
        }

        /**
         * Сумма, полученная наличными в разрезе тарифа для трансфера
         */
        public BigDecimal byCashTransferTariffProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным трансферам, полученная наличнымив разрезе тарифа для трансфера
         */
        public BigDecimal byCashTransferTariffProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная наличными (Исключая аннулированные) в разрезе тарифа для трансфера
         */
        BigDecimal byCashTransferTariffProfitSumExceptAnnulled() {
            return byCashTransferTariffProfitSum.subtract(byCashTransferTariffProfitSumAnnulled);
        }

        /**
         * Сумма, полученная наличными в разрезе суммы сбора для трансфера
         */
        public BigDecimal byCashTransferFeeProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным трансферам, полученная наличными в разрезе суммы сбора
         */
        public BigDecimal byCashTransferFeeProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная наличными (Исключая аннулированные) в разрезе суммы сбора для трансфера
         */
        BigDecimal byCashTransferFeeProfitSumExceptAnnulled() {
            return byCashTransferFeeProfitSum.subtract(byCashTransferFeeProfitSumAnnulled);
        }

        /**
         * Сумма, полученная по банковским картам в разрезе тарифа для трансфера
         */
        public BigDecimal byBankCardTransferTariffProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным трансферам, полученная по банковским картам в разрезе тарифа
         */
        public BigDecimal byBankCardTransferTariffProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная по банковским картам (Исключая аннулированные) в разрезе тарифа для трансфера
         */
        BigDecimal byBankCardTransferTariffProfitSumExceptAnnulled() {
            return byBankCardTransferTariffProfitSum.subtract(byBankCardTransferTariffProfitSumAnnulled);
        }

        /**
         * Сумма, полученная по банковским картам в разрезе суммы сбора для трансфера
         */
        public BigDecimal byBankCardTransferFeeProfitSum = BigDecimal.ZERO;
        /**
         * Сумма по аннулированным трансферам, полученная по банковским картам в разрезе суммы сбора
         */
        public BigDecimal byBankCardTransferFeeProfitSumAnnulled = BigDecimal.ZERO;

        /**
         * Сумма, полученная по банковским картам (Исключая аннулированные) в разрезе суммы сбора для трансфера
         */
        BigDecimal byBankCardTransferFeeProfitSumExceptAnnulled() {
            return byBankCardTransferFeeProfitSum.subtract(byBankCardTransferFeeProfitSumAnnulled);
        }

    }

}
