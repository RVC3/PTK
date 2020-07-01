package ru.ppr.cppk.printer.rx.operation.tapeEndReport;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.ikkm.IPrinter;

public class TapeEndReportTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    TapeEndReportTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        new ReportClicheTpl(params.clicheParams, textFormatter, true).printToDriver(printer);

        printer.printTextInNormalMode(textFormatter.alignCenterText("ОКОНЧАНИЕ БИЛЕТНОЙ ЛЕНТЫ"));
        if (params.shiftNum == null) {
            printer.printTextInNormalMode(textFormatter.alignCenterText("ВНЕ СМЕНЫ"));
        } else {
            printer.printTextInNormalMode(textFormatter.alignCenterText("СМЕНА  № " + params.shiftNum));
        }
        printer.printTextInNormalMode("НАЧ.: " + textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.startDate));
        printer.printTextInNormalMode("ОКОНЧ.: " + textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.date));
        printer.printTextInNormalMode(params.tapeSeries + " " + params.tapeNumber);
        printer.printTextInNormalMode("КОЛИЧ. ЧЕКОВ        =" + params.receiptsCount);
        if (params.firstDocNumber != null && params.lastDocNumber != null) {
            printer.printTextInNormalMode("№ " + textFormatter.asStr06d(params.firstDocNumber) + " - " + textFormatter.asStr06d(params.lastDocNumber) + " вкл.");
        }
        int reportCount = params.testShiftCount + params.shiftCount + params.preferentialShiftCount + params.auditLogCount
                + params.preferentialMonthlyCount + params.monthlyCount + params.testMonthlySheetsCount + params.salesForEttLogCount;
        printer.printTextInNormalMode("КОЛИЧ. ОТЧЕТОВ      =" + reportCount);
        if (params.testShiftCount != 0) {
            printer.printTextInNormalMode(" ПРОБНАЯ СМЕННАЯ    =" + params.testShiftCount);
        }
        if (params.shiftCount != 0) {
            printer.printTextInNormalMode(" СМЕННАЯ            =" + params.shiftCount);
        }
        if (params.preferentialShiftCount != 0) {
            printer.printTextInNormalMode(" ЛЬГОТНАЯ СМЕННАЯ   =" + params.preferentialShiftCount);
        }
        if (params.auditLogCount != 0) {
            printer.printTextInNormalMode(" КОНТРОЛЬНЫЙ ЖУРНАЛ =" + params.auditLogCount);
        }
        if (params.salesForEttLogCount != 0) {
            printer.printTextInNormalMode(" ЖУРНАЛ ПО ЭТТ      =" + params.salesForEttLogCount);
        }
        if (params.testMonthlySheetsCount != 0) {
            printer.printTextInNormalMode(" ПРОБНАЯ МЕСЯЧНАЯ   =" + params.testMonthlySheetsCount);
        }
        if (params.preferentialMonthlyCount != 0) {
            printer.printTextInNormalMode(" ЛЬГОТНАЯ МЕСЯЧНАЯ  =" + params.preferentialMonthlyCount);
        }
        if (params.monthlyCount != 0) {
            printer.printTextInNormalMode(" МЕСЯЧНАЯ           =" + params.monthlyCount);
        }
        printer.printTextInNormalMode("РАСХОД Л.(м)   =" + textFormatter.asMoney(params.consumptionOfTape));
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode("ПОДПИСЬ");
        printer.printTextInNormalMode(textFormatter.bigDelimiter());
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.waitPendingOperations();
    }

    public static class Params {
        /**
         * Параметры для клише
         */
        public ReportClicheTpl.Params clicheParams;
        /**
         * Время с принтера
         */
        public Date date;
        /**
         * Номер первого документа, распечатанного на установленной билетной
         * ленте
         */
        public Integer firstDocNumber;
        /**
         * Номер последнего документа, распечатанного на установленной билетной
         * ленте
         */
        public Integer lastDocNumber;
        /**
         * Количество ПД, распечатанных на билетной ленте
         */
        public Integer receiptsCount = 0;
        /**
         * Количество отчетов "Пробная сменная ведомость"
         */
        public int testShiftCount;
        /**
         * Номер смены
         */
        public Integer shiftNum;
        /**
         * Количество отчетов "Cменная ведомость"
         */
        public int shiftCount;
        /**
         * Количество отчетов "Льготная сменная ведомость"
         */
        public int preferentialShiftCount;
        /**
         * Количество отчетов "Контрольный журнал"
         */
        public int auditLogCount;
        /**
         * Количество отчетов "Льготная месячная ведомость"
         */
        public int preferentialMonthlyCount;
        /**
         * Количество отчетов "Месячная ведомость"
         */
        public int monthlyCount;
        /**
         * Количество отчетов "Пробная месячная ведомость"
         */
        public int testMonthlySheetsCount = 0;
        /**
         * Количество отчетов "урнал оформления по ЭТТ"
         */
        public int salesForEttLogCount = 0;
        /**
         * Дата и время установки бобины билетной ленты
         */
        public Date startDate;
        /**
         * Серия и номер установленной бобины
         */
        public String tapeSeries;
        /**
         * Номер установленной бобины
         */
        public int tapeNumber;
        /**
         * Расход билетной ленты
         */
        public BigDecimal consumptionOfTape;
    }
}
