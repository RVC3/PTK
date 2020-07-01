package ru.ppr.cppk.printer.rx.operation.printerPrintSalesForEttLog;

import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Created by Александр on 11.08.2016.
 * <p>
 * Данные по Журналу оформления по ЭТТ (п.1.6.1)
 */
public class PrinterTplSalesForEttLogInfo extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    PrinterTplSalesForEttLogInfo(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(textFormatter.alignCenterText("ЖУРНАЛ ОФОРМЛЕНИЯ"));
        printer.printTextInNormalMode(textFormatter.alignCenterText("ПО ЭТТ"));
        printer.printTextInNormalMode(textFormatter.alignCenterText("СМЕНА № " + params.shiftNum));
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.date));
        printer.printTextInNormalMode(textFormatter.bigDelimiter());

    }

    public static class Params {

        /**
         * Время с принтера
         */
        public Date date;
        /**
         * Порядковый номер смены
         */
        public Integer shiftNum;

    }

}
