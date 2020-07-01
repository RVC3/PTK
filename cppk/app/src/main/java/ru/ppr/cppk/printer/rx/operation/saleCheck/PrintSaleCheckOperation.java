package ru.ppr.cppk.printer.rx.operation.saleCheck;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.cppk.printer.rx.operation.base.InOpenedShiftOperation;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * Операция печати чека продажи.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class PrintSaleCheckOperation extends InOpenedShiftOperation {

    final Params params;
    final TextFormatter textFormatter;

    PrintSaleCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public abstract Observable<Result> call();

    /**
     * Результат печати чека
     */
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
        public SaleCheckTpl.Params saleCheckTplPrams;
        /**
         * Номер документа
         */
        public Integer pdNumber;
        /**
         * Вид ПД
         */
        public String ticketTypeName;
        /**
         * Полная стоимость
         */
        public BigDecimal ticketCostCostValueWithoutDiscount = BigDecimal.ZERO;
        /**
         * Стоимость со скидкой
         */
        public BigDecimal ticketCostCostValueWithDiscount = BigDecimal.ZERO;
        /**
         * Налоговая ставка на полную стоимость (от 0.1% до это 99.9%)
         */
        public BigDecimal ticketCostVatRate = BigDecimal.ZERO;
        /**
         * Налог на полную стоимость
         */
        public BigDecimal ticketCostVatValue = BigDecimal.ZERO;
        /**
         * Сумма сбора
         */
        public BigDecimal feeValue = BigDecimal.ZERO;
        /**
         * Налоговая ставка на сбор (от 0.1% до 99.9%)
         */
        public BigDecimal feeVatRate = BigDecimal.ZERO;
        /**
         * Налог на сбор
         */
        public BigDecimal feeVatValue = BigDecimal.ZERO;
        /**
         * Сумма платежа
         */
        public BigDecimal payment = BigDecimal.ZERO;
        /**
         * Тип платежа
         */
        public PaymentType paymentType;
        /**
         * Нужно ли добавить отступ после чека
         */
        public boolean addSpaceAfterCheck;
        /**
         * Номер телефона покупателя
         */
        public String customerPhoneNumber;
        /**
         * E-mail покупателя
         */
        public String customerEmail;

        @Override
        public String toString() {
            return "Params{" +
                    "headerParams=" + headerParams +
                    ", saleCheckTplPrams=" + saleCheckTplPrams +
                    ", pdNumber=" + pdNumber +
                    ", ticketTypeName='" + ticketTypeName + '\'' +
                    ", ticketCostCostValueWithoutDiscount=" + ticketCostCostValueWithoutDiscount +
                    ", ticketCostCostValueWithDiscount=" + ticketCostCostValueWithDiscount +
                    ", ticketCostVatRate=" + ticketCostVatRate +
                    ", ticketCostVatValue=" + ticketCostVatValue +
                    ", feeValue=" + feeValue +
                    ", feeVatRate=" + feeVatRate +
                    ", feeVatValue=" + feeVatValue +
                    ", payment=" + payment +
                    ", paymentType=" + paymentType +
                    ", addSpaceAfterCheck=" + addSpaceAfterCheck +
                    ", customerPhoneNumber='" + customerPhoneNumber + '\'' +
                    ", customerEmail='" + customerEmail + '\'' +
                    '}';
        }
    }


}
