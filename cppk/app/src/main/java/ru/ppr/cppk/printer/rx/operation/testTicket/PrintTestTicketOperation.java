package ru.ppr.cppk.printer.rx.operation.testTicket;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.cppk.printer.rx.operation.base.InOpenedShiftOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Single;

/**
 * Операция печати пробного ПД.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class PrintTestTicketOperation extends InOpenedShiftOperation {

    final Params params;
    final TextFormatter textFormatter;

    PrintTestTicketOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public abstract Single<Result> call();

    public static class Params {
        public Integer pdNumber;
        public String cashierName;
        public Integer dayCode;
        public Long ptkNumber;
        /**
         * Параметры для заголовка
         */
        public FiscalHeaderBuilder.Params headerParams;
        /**
         * Параметры для шаблона билета
         */
        public TestTicketTpl.Params testTicketTplParams;

        public Params setPdNumber(Integer pdNumber) {
            this.pdNumber = pdNumber;
            return this;
        }

        public Params setCashierName(String cashierName) {
            this.cashierName = cashierName;
            return this;
        }

        public Params setDayCode(Integer dayCode) {
            this.dayCode = dayCode;
            return this;
        }

        public Params setPtkNumber(Long ptkNumber) {
            this.ptkNumber = ptkNumber;
            return this;
        }

        public Params setHeaderParams(FiscalHeaderBuilder.Params headerParams) {
            this.headerParams = headerParams;
            return this;
        }

        public Params setTestTicketTplParams(TestTicketTpl.Params testTicketTplParams) {
            this.testTicketTplParams = testTicketTplParams;
            return this;
        }
    }

    public static class Result {
        /**
         * Сквозной номер документа (СПНД) назначается самим ФР.
         */
        private int spnd;
        /**
         * Время проведения операции по фискальнику
         */
        private Date operationTime;

        public Result(Date date, int spnd) {
            this.spnd = spnd;
            operationTime = date;
        }

        public int getSpnd() {
            return spnd;
        }

        public Date getOperationTime() {
            return operationTime;
        }
    }
}
