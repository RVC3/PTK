package ru.ppr.cppk.printer.rx.operation.discountMonthSheet;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * 1.6 Реквизиты месячной льготной ведомости
 *
 * @author Brazhkin A.V.
 */
public class MonthSheetTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    MonthSheetTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        new ReportClicheTpl(params.clicheParams, textFormatter, true).printToDriver(printer);

        params.sheetInfo.date = params.date;
        new MonthInfoTpl(params.sheetInfo, textFormatter).printToDriver(printer);
        new MonthStateTpl(params.monthStates, textFormatter).printToDriver(printer);

        if (!params.exemptions.isEmpty()) {
            for (Entry<Integer, ExemptionInfoTpl.Params> exemption : params.exemptions.entrySet()) {
                new ExemptionInfoTpl(exemption.getValue(), textFormatter).printToDriver(printer);
            }
            printer.printTextInNormalMode(textFormatter.bigDelimiter());
        }
        printer.printTextInNormalMode("ПОЛНЫЙ ВЫП. ДОХОД");
        printer.printTextInNormalMode("ЗА МЕСЯЦ       =" + textFormatter.asMoney(params.lossSum));
        printer.printTextInNormalMode("КОЛИЧ. АННУЛ.  =" + params.totalRepealCount);
        printer.printTextInNormalMode("ВЫП. ДОХОД ЗА ВЫЧЕТОМ");
        printer.printTextInNormalMode("АННУЛ.         =" + textFormatter.asMoney(params.lossSumExceptAnnulled()));

        printer.printTextInNormalMode(textFormatter.bigDelimiter());
        printer.printTextInNormalMode(textFormatter.alignCenterText("ПЕЧАТЬ ЗАКОНЧЕНА"));
        printer.printTextInNormalMode(textFormatter.bigDelimiter());
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.waitPendingOperations();
    }

    public static class Params {
        /**
         * Время с принтера
         */
        public Date date;
        /**
         * Параметры для клише
         */
        public ReportClicheTpl.Params clicheParams;
        /**
         * Данные по Льготной месячной ведомости
         */
        public MonthInfoTpl.Params sheetInfo;
        /**
         * Состояние месяца
         */
        public MonthStateTpl.Params monthStates;
        /**
         * Информация о льготах
         */
        public Exemptions exemptions;
        /**
         * Выпадающий доход по всем видам льгот
         */
        public BigDecimal lossSum = BigDecimal.ZERO;
        /**
         * Выпадающий доход по всем видам льгот по аннулированным ПД
         */
        public BigDecimal lossSumAnnulled = BigDecimal.ZERO;
        /**
         * количество аннулированных ПД
         */
        public int totalRepealCount = 0;
        /**
         * Выпадающий доход по всем видам льгот (Исключая аннулированные)
         */
        public BigDecimal lossSumExceptAnnulled() {
            return lossSum.subtract(lossSumAnnulled);
        }

    }

    public static class Exemptions extends HashMap<Integer, ExemptionInfoTpl.Params> {

    }
}
