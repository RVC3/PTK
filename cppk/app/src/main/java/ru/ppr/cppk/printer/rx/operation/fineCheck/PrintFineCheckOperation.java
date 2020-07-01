package ru.ppr.cppk.printer.rx.operation.fineCheck;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.cppk.printer.rx.operation.base.InOpenedShiftOperation;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Single;

/**
 * Операция печати чека взимания штрафа.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class PrintFineCheckOperation extends InOpenedShiftOperation {

    final Params params;
    final TextFormatter textFormatter;

    PrintFineCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public abstract Single<Result> call();

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

    /**
     * Входные параметры для печати чека продажи
     */
    public static class Params {
        /**
         * Параметры для заголовка
         */
        public FiscalHeaderBuilder.Params headerParams;
        /**
         * Параметры для шаблона билета
         */
        public FineCheckTpl.Params fineCheckTplParams;
        /**
         * Номер документа
         */
        public Integer pdNumber;
        /**
         * Стоимость
         */
        public BigDecimal amount = BigDecimal.ZERO;
        /**
         * Процент НДС
         */
        public int vatRate = 0;
        /**
         * НДС
         */
        public BigDecimal vatValue = BigDecimal.ZERO;
        /**
         * Сумма платежа
         */
        public BigDecimal payment = BigDecimal.ZERO;
        /**
         * Тип платежа
         */
        public PaymentType paymentType;
        /**
         * Номер телефона покупателя
         */
        public String customerPhoneNumber;
        /**
         * E-mail покупателя
         */
        public String customerEmail;

    }
}
