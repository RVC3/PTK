package ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport;

import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.ikkm.IPrinter;

public class TapeStartReportTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    TapeStartReportTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        new ReportClicheTpl(params.clicheParams, textFormatter, true).printToDriver(printer);

        printer.printTextInNormalMode(textFormatter.alignCenterText("УСТАНОВКА БИЛЕТНОЙ ЛЕНТЫ"));
        if (params.shiftNum == null) {
            printer.printTextInNormalMode(textFormatter.alignCenterText("ВНЕ СМЕНЫ"));
        } else {
            printer.printTextInNormalMode(textFormatter.alignCenterText("СМЕНА  № " + params.shiftNum));
        }
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.date));
        printer.printTextInNormalMode(params.tapeSeries + " " + params.tapeNumber);
        printer.printTextInNormalMode("ПЕРВЫЙ ДОКУМЕНТ");
        printer.printTextInNormalMode("№ " + textFormatter.asStr06d(params.firstDocNumber));
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode("ПОДПИСЬ");
        printer.printTextInNormalMode(textFormatter.bigDelimiter());
        printer.printTextInNormalMode(" ");
        printer.waitPendingOperations();
        printer.printAdjustingTable();
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
         * Номер первого документа, который будет распечатан на установленной катушке билетной ленты
         */
        public Integer firstDocNumber;
        /**
         * Номер смены
         */
        public Integer shiftNum;
        /**
         * Серия установленной бобины
         */
        public String tapeSeries;
        /**
         * Номер установленной бобины
         */
        public int tapeNumber;

    }
}
