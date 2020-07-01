package ru.ppr.cppk.printer.rx.operation.btMonthReport;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Created by Dmitry Nevolin on 17.02.2016.
 */
public class BtMonthReportTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    BtMonthReportTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {
        //клише
        new ReportClicheTpl(params.clicheParams, textFormatter, true).printToDriver(printer);
        //терминал
        printer.printTextInNormalMode(textFormatter.alignWidthText("Терминал", params.terminalNumber));
        //дата и время вывода отчёта на печать
        printer.printTextInNormalMode(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.date));
        //заголовок отчёта и номер месяца, за который снимается отчёт
        printer.printTextInNormalMode(" ");
        printer.printTextInNormalMode(textFormatter.alignCenterText("ИТОГИ ТЕРМИНАЛА"));
        printer.printTextInNormalMode(textFormatter.alignCenterText("МЕСЯЦ " + params.monthNumber));
        printer.printTextInNormalMode(" ");
        //все успешные транзакции оплаты (сумма и количество)
        printer.printTextInNormalMode("ВСЕГО ОПЛАТ ПО БАНК.КАРТАМ");
        printer.printTextInNormalMode(" СУММА         =" + textFormatter.asMoney(params.transactionsTotal));
        printer.printTextInNormalMode(" КОЛИЧ.        =" + params.transactionsQuantity);
        //все успешные транзакции отмены (сумма и количество)
        printer.printTextInNormalMode(" ВСЕГО ОТМЕНА (БАНК)");
        printer.printTextInNormalMode(" СУММА         =" + textFormatter.asMoney(params.cancelsTotal));
        printer.printTextInNormalMode(" КОЛИЧ.        =" + params.cancelsQuantity);
        //транзакции не прикреплённые к событию продажи ПД + события продажи со статусом checkPrinted (сумма и количество)
        printer.printTextInNormalMode(" В ТОМ ЧИСЛЕ ОШИБКА");
        printer.printTextInNormalMode(" ПЕЧАТИ/ЗАПИСИ");
        printer.printTextInNormalMode("  СУММА (БАНК) =" + textFormatter.asMoney(params.transactionsWithoutSaleTotal));
        printer.printTextInNormalMode("  КОЛИЧ.       =" + params.transactionsWithoutSaleQuantity);
        //транзакции не прикреплённые к событию продажи ПД + события продажи со статусом checkPrinted, но без успешно аннулированных (сумма и количество)
        printer.printTextInNormalMode("  КОРРЕКТИРОВКА НА ОТМЕНУ");
        printer.printTextInNormalMode("  (БАНК)");
        printer.printTextInNormalMode("  СУММА (БАНК) =" + textFormatter.asMoney(params.transactionsWithoutSaleAndCancellationTotal));
        printer.printTextInNormalMode("  КОЛИЧ.       =" + params.transactionsWithoutSaleAndCancellationQuantity);
        //все успешные и целиком завершенные транзакции оплаты (сумма и количество)
        printer.printTextInNormalMode("ИТОГО ПО БАНК.КАРТАМ");
        printer.printTextInNormalMode("ЗА ВЫЧЕТОМ ОТМЕН");
        printer.printTextInNormalMode(" СУММА         =" + textFormatter.asMoney(params.completedTransactionsTotal));
        printer.printTextInNormalMode(" КОЛИЧ.        =" + params.completedTransactionsQuantity);
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
        private Date date;
        /**
         * Параметры для клише
         */
        private ReportClicheTpl.Params clicheParams;
        /**
         * Номер IPos-терминала, подключенного к ПТК
         */
        private String terminalNumber;
        /**
         * Номер месяца, за который снимается отчёт
         */
        private int monthNumber;
        /**
         * Все успешные транзакции оплаты (количество)
         */
        private int transactionsQuantity;
        /**
         * Все успешные транзакции оплаты (сумма)
         */
        private BigDecimal transactionsTotal = BigDecimal.ZERO;
        /**
         * Все успешные транзакции отмены (количество)
         */
        private int cancelsQuantity;
        /**
         * Все успешные транзакции отмены (сумма)
         */
        private BigDecimal cancelsTotal = BigDecimal.ZERO;
        /**
         * Транзакции не прикреплённые к событию продажи ПД + события продажи со статусом checkPrinted (количество)
         */
        private int transactionsWithoutSaleQuantity;
        /**
         * Транзакции не прикреплённые к событию продажи ПД + события продажи со статусом checkPrinted (сумма)
         */
        private BigDecimal transactionsWithoutSaleTotal = BigDecimal.ZERO;
        /**
         * Транзакции не прикреплённые к событию продажи ПД + события продажи со статусом checkPrinted, но без успешно аннулированных (количество)
         */
        private int transactionsWithoutSaleAndCancellationQuantity;
        /**
         * Транзакции не прикреплённые к событию продажи ПД + события продажи со статусом checkPrinted, но без успешно аннулированных (сумма)
         */
        private BigDecimal transactionsWithoutSaleAndCancellationTotal = BigDecimal.ZERO;
        /**
         * Все успешные и целиком завершенные транзакции оплаты (количество)
         */
        private int completedTransactionsQuantity;
        /**
         * Все успешные и целиком завершенные транзакции оплаты (сумма)
         */
        private BigDecimal completedTransactionsTotal = BigDecimal.ZERO;

        public void setDate(Date date) {
            this.date = date;
        }

        public void setClicheParams(ReportClicheTpl.Params clicheParams) {
            this.clicheParams = clicheParams;
        }

        public void setTerminalNumber(String terminalNumber) {
            this.terminalNumber = terminalNumber;
        }

        public void setMonthNumber(int monthNumber) {
            this.monthNumber = monthNumber;
        }

        public void setTransactionsQuantity(int transactionsQuantity) {
            this.transactionsQuantity = transactionsQuantity;
        }

        public void setTransactionsTotal(BigDecimal transactionsTotal) {
            this.transactionsTotal = transactionsTotal;
        }

        public void setCancelsQuantity(int cancelsQuantity) {
            this.cancelsQuantity = cancelsQuantity;
        }

        public void setCancelsTotal(BigDecimal cancelsTotal) {
            this.cancelsTotal = cancelsTotal;
        }

        public void setTransactionsWithoutSaleQuantity(int transactionsWithoutSaleQuantity) {
            this.transactionsWithoutSaleQuantity = transactionsWithoutSaleQuantity;
        }

        public void setTransactionsWithoutSaleTotal(BigDecimal transactionsWithoutSaleTotal) {
            this.transactionsWithoutSaleTotal = transactionsWithoutSaleTotal;
        }

        public void setTransactionsWithoutSaleAndCancellationQuantity(int transactionsWithoutSaleAndCancellationQuantity) {
            this.transactionsWithoutSaleAndCancellationQuantity = transactionsWithoutSaleAndCancellationQuantity;
        }

        public void setTransactionsWithoutSaleAndCancellationTotal(BigDecimal transactionsWithoutSaleAndCancellationTotal) {
            this.transactionsWithoutSaleAndCancellationTotal = transactionsWithoutSaleAndCancellationTotal;
        }

        public void setCompletedTransactionsQuantity(int completedTransactionsQuantity) {
            this.completedTransactionsQuantity = completedTransactionsQuantity;
        }

        public void setCompletedTransactionsTotal(BigDecimal completedTransactionsTotal) {
            this.completedTransactionsTotal = completedTransactionsTotal;
        }

    }

}
