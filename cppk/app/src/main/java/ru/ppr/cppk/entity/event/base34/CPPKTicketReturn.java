package ru.ppr.cppk.entity.event.base34;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.entity.event.model34.ReturnOperationType;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.logger.Logger;

/**
 * Событие аннулирования ПД.
 *
 * @author Grigoriy Kashka
 */
public class CPPKTicketReturn {

    /**
     * Id события
     */
    private long id = -1;
    /**
     * Id события продажи ПД, с которым связано данное событие аннулирования ПД
     */
    private long pdSaleEventId = -1;

    /// Код способа возврата
    /// Соответствует коду возврата оплаты в справочнике НСИ «Способы оплаты».
    private ReturnOperationType operation = ReturnOperationType.Annulment;

    private Date recallDateTime;
    private String recallReason;

    /// Код способа возврата
    /// Соответствует коду возврата оплаты в справочнике НСИ «Способы оплаты».
    private PaymentType returnPaymentMethod;

    /**
     * Первичный ключ для таблицы Check
     * Чек возврата, в выгрузках вроде не учавствует, добавляем чтобы сохранился номер,
     * в AdditionalInfo предлагаю писать "Аннулирование чека №5"
     */
    private long checkId;

    private long eventId;

    private long ticketTapeEventId;

    private long bankTransactionCashRegisterEventId = -1;

    private long priceId;

    public BigDecimal getSumToReturn() {
        return sumToReturn;
    }

    public void setSumToReturn(BigDecimal sumToReturn) {
        this.sumToReturn = sumToReturn;
    }

    /**
     * сколько нужно вернуть денег при аннулировании, для ПТК пока нет частичных возвратов - всегда = Price.Payed
     */
    private BigDecimal sumToReturn;

    /**
     * Id сменного события, в рамках которого случилось это событие
     * Первичный ключ для таблицы CashRegisterWorkingShift
     */
    private long cashRegisterWorkingShiftId;

    private ProgressStatus progressStatus;

    private String returnBankTerminalSlip;

    public long getPdSaleEventId() {
        return pdSaleEventId;
    }

    public void setPdSaleEventId(long pdSaleEventId) {
        this.pdSaleEventId = pdSaleEventId;
    }

    public long getPriceId() {
        return priceId;
    }

    public void setPriceId(long priceId) {
        this.priceId = priceId;
    }

    public long getCashRegisterWorkingShiftId() {
        return cashRegisterWorkingShiftId;
    }

    public void setCashRegisterWorkingShiftId(long cashRegisterWorkingShiftId) {
        this.cashRegisterWorkingShiftId = cashRegisterWorkingShiftId;
    }

    public long getBankTransactionCashRegisterEventId() {
        return bankTransactionCashRegisterEventId;
    }

    public void setBankTransactionCashRegisterEventId(long bankTransactionCashRegisterEventId) {
        this.bankTransactionCashRegisterEventId = bankTransactionCashRegisterEventId;
    }

    public long getTicketTapeEventId() {
        return ticketTapeEventId;
    }

    public void setTicketTapeEventId(long ticketTapeEventId) {
        this.ticketTapeEventId = ticketTapeEventId;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public Date getRecallDateTime() {
        return recallDateTime;
    }

    public void setRecallDateTime(Date recallDateTime) {
        this.recallDateTime = recallDateTime;
    }

    public String getRecallReason() {
        return recallReason;
    }

    public void setRecallReason(String recallReason) {
        this.recallReason = recallReason;
    }

    public long getCheckId() {
        return checkId;
    }

    public void setCheckId(long checkId) {
        this.checkId = checkId;
    }

    public ReturnOperationType getOperation() {
        return operation;
    }

    public void setOperation(ReturnOperationType operation) {
        this.operation = operation;
    }

    public PaymentType getReturnPaymentMethod() {
        return returnPaymentMethod;
    }

    public void setReturnPaymentMethod(PaymentType returnPaymentMethod) {
        this.returnPaymentMethod = returnPaymentMethod;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getReturnBankTerminalSlip() {
        return returnBankTerminalSlip;
    }

    public void setReturnBankTerminalSlip(String returnBankTerminalSlip) {
        this.returnBankTerminalSlip = returnBankTerminalSlip;
    }

    public ProgressStatus getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(ProgressStatus progressStatus) {
        this.progressStatus = progressStatus;
    }

}
