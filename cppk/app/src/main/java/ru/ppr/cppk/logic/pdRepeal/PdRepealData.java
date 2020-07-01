package ru.ppr.cppk.logic.pdRepeal;

import java.util.List;

import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;

/**
 * Хранилищие данных для процесса аннулирования ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class PdRepealData {
    /**
     * Событие продажи ПД
     */
    private CPPKTicketSales pdSaleEvent;
    /**
     * Причина аннулирования
     */
    private String repealReason;
    /**
     * Данные о бансковской транзакции (отмене оплаты)
     */
    private long bankTransactionEventId = -1;
    /**
     * Слип банковского терминала об отмене банковской транзакции
     */
    private List<String> slipReceipt;
    /**
     * Флаг, что при начале процесса аннулирования ПД было выяснено, что в БД уже есть событие в статусе {{@link ProgressStatus#PrePrinting}}
     * И, возможно, чек аннулирования для этого события уже в ФР
     */
    private boolean previousAttemptCouldBeInFr;

    public CPPKTicketSales getPdSaleEvent() {
        return pdSaleEvent;
    }

    public void setPdSaleEvent(CPPKTicketSales pdSaleEvent) {
        this.pdSaleEvent = pdSaleEvent;
    }

    public String getRepealReason() {
        return repealReason;
    }

    public void setRepealReason(String repealReason) {
        this.repealReason = repealReason;
    }

    public long getBankTransactionEventId() {
        return bankTransactionEventId;
    }

    public void setBankTransactionEventId(long bankTransactionEventId) {
        this.bankTransactionEventId = bankTransactionEventId;
    }

    public List<String> getSlipReceipt() {
        return slipReceipt;
    }

    public void setSlipReceipt(List<String> slipReceipt) {
        this.slipReceipt = slipReceipt;
    }

    public boolean isPreviousAttemptCouldBeInFr() {
        return previousAttemptCouldBeInFr;
    }

    public void setPreviousAttemptCouldBeInFr(boolean previousAttemptCouldBeInFr) {
        this.previousAttemptCouldBeInFr = previousAttemptCouldBeInFr;
    }
}
