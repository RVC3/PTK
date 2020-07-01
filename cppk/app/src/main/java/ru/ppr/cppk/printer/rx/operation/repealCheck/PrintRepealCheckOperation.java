package ru.ppr.cppk.printer.rx.operation.repealCheck;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.cppk.printer.rx.operation.base.InOpenedShiftOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Single;

/**
 * Операция печати чека аннулирования.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class PrintRepealCheckOperation extends InOpenedShiftOperation {

    final Params params;
    final TextFormatter textFormatter;

    PrintRepealCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public abstract Single<Result> call();

    /**
     * Класс контейнер для результата печати чека отмены
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
     * Класс-контейнер входных параметров
     */
    public static class Params {
        /**
         * Номер документа
         */
        public Integer pdNumber;
        /**
         * Полная стоимость
         */
        public BigDecimal ticketCostValueWithoutDiscount = BigDecimal.ZERO;
        /**
         * Стоимость со скидкой
         */
        public BigDecimal ticketCostValueWithDiscount = BigDecimal.ZERO;
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
         * Параметры для заголовка
         */
        public FiscalHeaderBuilder.Params headerParams;
        /**
         * Параметры для шаблона билета
         */
        public RepealCheckTpl.Params repealCheckTplParams;
    }
}
