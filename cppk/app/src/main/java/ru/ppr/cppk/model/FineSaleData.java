package ru.ppr.cppk.model;

import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.nsi.entity.Fine;

/**
 * Хранилищие данных для процесса взыскания штрафа.
 *
 * @author Aleksandr Brazhkin
 */
public class FineSaleData {

    /**
     * Выбранный штраф
     */
    private Fine fine;
    /**
     * Способ оплаты
     */
    private PaymentType paymentType;
    /**
     * Данные для отправки электронного билета
     */
    private ETicketDataParams eTicketDataParams;
    /**
     * Данные о бансковской транзакции
     */
    private Long bankTransactionEventId;
    /**
     * Статус события продажи штрафа
     */
    private FineSaleEvent.Status status = FineSaleEvent.Status.CREATED;

    public Fine getFine() {
        return fine;
    }

    public void setFine(Fine fine) {
        this.fine = fine;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public ETicketDataParams getETicketDataParams() {
        return eTicketDataParams;
    }

    public void setETicketDataParams(ETicketDataParams eTicketDataParams) {
        this.eTicketDataParams = eTicketDataParams;
    }

    public Long getBankTransactionEventId() {
        return bankTransactionEventId;
    }

    public void setBankTransactionEventId(Long bankTransactionEventId) {
        this.bankTransactionEventId = bankTransactionEventId;
    }

    public FineSaleEvent.Status getStatus() {
        return status;
    }

    public void setStatus(FineSaleEvent.Status status) {
        this.status = status;
    }

}
