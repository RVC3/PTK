package ru.ppr.cppk.localdb.model;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Событие "Оплата штрафа".
 *
 * @author Aleksandr Brazhkin
 */
public class FineSaleEvent implements LocalModelWithId<Long> {
    /**
     * Id
     */
    private Long id;
    /**
     * Сумма штрафа
     */
    private BigDecimal amount;
    /**
     * Дата и время операции (utc)
     * В базу это значение должно сохраняться в секундах
     */
    private Date operationDateTime;
    /**
     * Способ оплаты
     */
    private PaymentType paymentMethod;
    /**
     * Статус события
     */
    private Status status;
    /**
     * Id связанной сущности из таблицы Event
     */
    private long eventId;
    /**
     * Id связанной сущности из таблицы CashRegisterWorkingShift
     */
    private long shiftEventId;
    /**
     * Id связанной сущности из таблицы TicketTapeEvent
     */
    private Long ticketTapeEventId;
    /**
     * Id связанной сущности из таблицы Price
     */
    private Long checkId;
    /**
     * Id связанной сущности из таблицы BankTransactionCashRegisterEvent
     */
    private Long bankTransactionEventId;
    /**
     * Код связанной сущности из таблицы НСИ Fines
     */
    private long fineCode;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getOperationDateTime() {
        return operationDateTime;
    }

    public void setOperationDateTime(Date operationDateTime) {
        this.operationDateTime = operationDateTime;
    }

    public PaymentType getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentType paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getShiftEventId() {
        return shiftEventId;
    }

    public void setShiftEventId(long shiftEventId) {
        this.shiftEventId = shiftEventId;
    }

    public Long getTicketTapeEventId() {
        return ticketTapeEventId;
    }

    public void setTicketTapeEventId(Long ticketTapeEventId) {
        this.ticketTapeEventId = ticketTapeEventId;
    }

    public Long getCheckId() {
        return checkId;
    }

    public void setCheckId(Long checkId) {
        this.checkId = checkId;
    }

    public Long getBankTransactionEventId() {
        return bankTransactionEventId;
    }

    public void setBankTransactionEventId(Long bankTransactionEventId) {
        this.bankTransactionEventId = bankTransactionEventId;
    }

    public long getFineCode() {
        return fineCode;
    }

    public void setFineCode(long fineCode) {
        this.fineCode = fineCode;
    }

    /**
     * Статус события
     * Структура:
     * <pre>
     *            CREATED
     *               |
     *             PAYED
     *               |
     *          PRE_PRINTING
     *         |           |
     * CHECK_PRINTED     BROKEN
     *      |
     *  COMPLETED
     * </pre>
     */
    public enum Status {
        /**
         * Создано в БД, может быть свободно удалено.
         */
        CREATED(0),
        /**
         * Получена оплата. Нельзя удалять без возврата денег.
         */
        PAYED(1),
        /**
         * Напечатан чек, деньги дегли на фискальник.
         */
        CHECK_PRINTED(2),
        /**
         * Событие полностью сформировано.
         */
        COMPLETED(3),
        /**
         * Статус перед отправкой на ФР.
         */
        PRE_PRINTING(4),
        /**
         * Статус после синхронизации чека, когда нам известно, что данный чек не лёг на фискальник.
         */
        BROKEN(5);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status fromCode(int code) {
            for (Status item : values()) {
                if (item.code == code) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Unknown code = " + code);
        }
    }
}
