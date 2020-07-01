package ru.ppr.cppk.printer.rx.operation.printerPrintSalesForEttLog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * 1.6 Журнал оформления по ЭТТ
 *
 * @author Aleksandr Brazhkin
 */
public class PrinterTplSalesForEttLog extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    PrinterTplSalesForEttLog(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        new ReportClicheTpl(params.clicheParams, textFormatter, true).printToDriver(printer);

        params.salesForEttLogInfo.date = params.date;

        new PrinterTplSalesForEttLogInfo(params.salesForEttLogInfo, textFormatter).printToDriver(printer);

        if (params.events.isEmpty()) {
            printer.printTextInNormalMode(textFormatter.alignCenterText("ДОКУМЕНТЫ ПО ЭТТ"));
            printer.printTextInNormalMode(textFormatter.alignCenterText("НЕ ОФОРМЛЯЛИСЬ"));
        } else {
            for (PrinterTplSalesForEttPdInfo.Params event : params.events) {
                new PrinterTplSalesForEttPdInfo(event, textFormatter).printToDriver(printer);
            }
        }

        printer.printTextInNormalMode("ДОКУМЕНТОВ:    =" + params.ettPdCount);
        if (params.ettPdCount > 0) {
            printer.printTextInNormalMode("ВЫП. ДОХОД:    =" + textFormatter.asMoney(params.lossSum));
        }

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
         * Данные по отчету «Контрольный журнал»
         */
        public PrinterTplSalesForEttLogInfo.Params salesForEttLogInfo;
        /**
         * События
         */
        public ArrayList<PrinterTplSalesForEttPdInfo.Params> events;
        /**
         * Выпадающий доход
         */
        public BigDecimal lossSum = BigDecimal.ZERO;
        /**
         * Количество билетов по ЭТТ
         */
        public int ettPdCount;


    }

}
