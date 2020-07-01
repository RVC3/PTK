package ru.ppr.cppk.printer.rx.operation.discountShiftSheet;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * @author Brazhkin A.V.
 * <p>
 * 1.4 Реквизиты льготной сменной ведомости
 */
public class ShiftSheetTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    ShiftSheetTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        new ReportClicheTpl(params.clicheParams, textFormatter, true).printToDriver(printer);

        params.sheetInfo.date = params.date;
        new SheetInfoTpl(params.sheetInfo, textFormatter).printToDriver(printer);

        if (!params.exemptionRoutes.isEmpty()) {
            for (Entry<Integer, ExemptionGroups> route : params.exemptionRoutes.entrySet()) {
                printer.printTextInNormalMode("МАРШРУТ " + route.getKey());
                for (Entry<String, Exemptions> group : route.getValue().entrySet()) {
                    printer.printTextInNormalMode(group.getKey());

                    Exemptions exemptionGroup = group.getValue();

                    if (exemptionGroup.size() > 1) {
                        printer.printTextInNormalMode(" ВСЕГО         =" + exemptionGroup.totalCountExceptAnnulled);
                        printer.printTextInNormalMode(" ВЫП. ДОХОД    =" + textFormatter.asMoney(exemptionGroup.lossSumExceptAnnulled));
                    }

                    for (Entry<Integer, ExemptionInfoTpl.Params> exemption : exemptionGroup.entrySet()) {
                        new ExemptionInfoTpl(exemption.getValue(), textFormatter).printToDriver(printer);
                    }
                }
            }
            printer.printTextInNormalMode(textFormatter.bigDelimiter());
        }
        printer.printTextInNormalMode("ПОЛНЫЙ ВЫП. ДОХОД");
        printer.printTextInNormalMode("ЗА СМЕНУ       =" + textFormatter.asMoney(params.lossSum));
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
         * Данные по Льготной сменной ведомости
         */
        public SheetInfoTpl.Params sheetInfo;
        /**
         * Информация о льготах, сгруппированная по категориям
         */
        public ExemptionRoutes exemptionRoutes;
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

    public static class ExemptionRoutes extends HashMap<Integer, ExemptionGroups> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

    }

    public static class ExemptionGroups extends HashMap<String, Exemptions> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

    }

    public static class Exemptions extends HashMap<Integer, ExemptionInfoTpl.Params> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public int totalCountExceptAnnulled = 0;

        public BigDecimal lossSumExceptAnnulled = BigDecimal.ZERO;
    }

}
