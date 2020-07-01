package ru.ppr.cppk.printer.rx.operation.clearingSheet;

import java.math.BigDecimal;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * @author Brazhkin A.V.
 *         <p/>
 *         Данные по ведомости «Гашение сменных итогов» (п.1.3.1)
 *         <p/>
 *         ДДанные по отчету «Ведомость гашения месяца» (п.1.9.1)
 */
public class SheetInfoTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    SheetInfoTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        if (params.monthNum != null) {
            printer.printTextInNormalMode(textFormatter.alignCenterText("ЗАКРЫТИЕ МЕСЯЦА"));
            printer.printTextInNormalMode(textFormatter.alignCenterText("МЕСЯЦ № " + params.monthNum));
        } else {
            printer.printTextInNormalMode(textFormatter.alignCenterText("ГАШЕНИЕ СМЕННЫХ ИТОГОВ"));
            printer.printTextInNormalMode(textFormatter.alignCenterText("СМЕНА № " + params.shiftNum));
        }
        printer.printTextInNormalMode("МЕС.           =" + textFormatter.asMoney(params.sum));
        if (params.shiftNum != null) {
            printer.printTextInNormalMode("СУММА В ФР     =" + textFormatter.asMoney(params.cashInFR));
            printer.printTextInNormalMode("СМЕНА          =" + textFormatter.asMoney(params.clearingSum));
        }
        printer.printTextInNormalMode("БИЛЕТОВ        =" + params.clearingCount);
        if (params.shiftNum != null) {
            printer.printTextInNormalMode("РАСХОД Л.(М.)  =" + textFormatter.asMoney(params.consumptionOfTape));
            printer.printTextInNormalMode("ЗАП. ФП        =" + params.availableRecordsInFRCount);
        }
        printer.printTextInNormalMode(textFormatter.bigDelimiter());

    }

    public static class Params {

        /**
         * Порядковый номер смены
         */
        public Integer shiftNum;
        /**
         * порядковый номер месяца
         */
        public Integer monthNum;
        /**
         * Сумма выручки за месяц
         */
        public BigDecimal sum = BigDecimal.ZERO;
        /**
         * Сумма в ФР до обнуления сменных итогов при гашении смены
         */
        public BigDecimal cashInFR = BigDecimal.ZERO;
        /**
         * Сумма погашенной выручки за смену
         */
        public BigDecimal clearingSum = BigDecimal.ZERO;
        /**
         * Обнуленное количество билетов
         */
        public int clearingCount = 0;
        /**
         * Расход ленты за смену в метрах
         */
        public BigDecimal consumptionOfTape = BigDecimal.ZERO;
        /**
         * Количество оставшихся свободных записей в фискальной памяти
         */
        public long availableRecordsInFRCount = 0;

    }

}
