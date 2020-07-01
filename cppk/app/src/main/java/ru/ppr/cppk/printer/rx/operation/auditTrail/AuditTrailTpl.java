package ru.ppr.cppk.printer.rx.operation.auditTrail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.TextStyle;

/**
 * Шаблон печати.
 * 1.5 Контрольный журнал
 *
 * @author Aleksandr Brazhkin
 */
public class AuditTrailTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    AuditTrailTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        new ReportClicheTpl(params.clicheParams, textFormatter, true).printToDriver(printer);

        params.auditTrailInfo.date = params.date;

        new AuditTrailInfoTpl(params.auditTrailInfo, textFormatter).printToDriver(printer);

        for (Object event : params.events) {
            if (event instanceof PdSaleTpl.Params) {
                // Оформление ПД
                new PdSaleTpl((PdSaleTpl.Params) event, textFormatter).printToDriver(printer);
            } else if (event instanceof PrintReportTpl.Params) {
                // Печать отчета
                new PrintReportTpl((PrintReportTpl.Params) event, textFormatter).printToDriver(printer);
            } else if (event instanceof ExtraChargeTpl.Params) {
                // Оформление доплаты
                new ExtraChargeTpl((ExtraChargeTpl.Params) event, textFormatter).printToDriver(printer);
            } else if (event instanceof CancelPdTpl.Params) {
                // Аннулирование ПД
                new CancelPdTpl((CancelPdTpl.Params) event, textFormatter).printToDriver(printer);
            } else if (event instanceof ServiceSaleTpl.Params) {
                // Оформление услуги
                new ServiceSaleTpl((ServiceSaleTpl.Params) event, textFormatter).printToDriver(printer);
            } else if (event instanceof FineSaleTpl.Params) {
                // Оформление штрафа
                new FineSaleTpl((FineSaleTpl.Params) event, textFormatter).printToDriver(printer);
            } else if (event instanceof TransferSaleTpl.Params) {
                // Оформление трансфера
                new TransferSaleTpl((TransferSaleTpl.Params) event, new TransferSaleTplTextFormatter()).printToDriver(printer);
            }
        }

        printer.printTextInNormalMode(textFormatter.bigDelimiter());
        printer.printTextInNormalMode(textFormatter.alignCenterText("ПЕЧАТЬ ЗАКОНЧЕНА"));
        printer.printTextInNormalMode(textFormatter.bigDelimiter());
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(" ");
        printer.waitPendingOperations();

    }

    private class TransferSaleTplTextFormatter implements TransferSaleTpl.TextFormatter {

        @Override
        public String asStr06d(int number) {
            return textFormatter.asStr06d(number);
        }

        @Override
        public String asDate_dd_MM_yyyy_HH_mm_ss(Date dateTime) {
            return textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(dateTime);
        }

        @Override
        public String asDate_dd_MM_yyyy_HH_mm(Date dateTime) {
            return textFormatter.asDate_dd_MM_yyyy_HH_mm(dateTime);
        }

        @Override
        public String asMoney(BigDecimal value) {
            return textFormatter.asMoney(value);
        }

        @Override
        public int getWidth() {
            return textFormatter.getWidthForTextStyle(TextStyle.TEXT_NORMAL);
        }

        @Override
        public String smallDelimiter() {
            return textFormatter.smallDelimiter();
        }

        @Override
        public String alignWidthText(String first, String second) {
            return textFormatter.alignWidthText(first, second);
        }

        @Override
        public String asMaskedBankCardNumber(String cardPan) {
            return textFormatter.asMaskedBankCardNumber(cardPan);
        }

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
        public AuditTrailInfoTpl.Params auditTrailInfo;
        /**
         * События
         */
        public ArrayList<Object> events;

    }

}
