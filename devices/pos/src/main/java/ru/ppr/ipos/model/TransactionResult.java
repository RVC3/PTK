package ru.ppr.ipos.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Dmitry Nevolin on 20.11.2015.
 */
public class TransactionResult {

    /**
     * Идентификатор транзации.
     */
    private int id;
    /**
     * Дата и время транзакции (в UTC).
     */
    private Date timeStamp;
    /**
     * Признак одобрения.
     */
    private boolean approved;
    /**
     * Идентификатор терминала.
     */
    private String terminalId;
    /**
     * Номер чека.
     */
    private int invoiceNumber;
    /**
     * Квитанция.
     */
    private List<String> receipt;
    /**
     * Код ответа банка.
     */
    private int bankResponseCode;
    /**
     * Расшифровка кода ответа банка.
     */
    private String bankResponse;

    public TransactionResult() {

    }

    public TransactionResult(TransactionResult transactionResult) {
        id = transactionResult.id;
        timeStamp = transactionResult.timeStamp;
        approved = transactionResult.approved;
        terminalId = transactionResult.terminalId;
        invoiceNumber = transactionResult.invoiceNumber;
        bankResponseCode = transactionResult.bankResponseCode;
        bankResponse = transactionResult.bankResponse;

        if (receipt != null)
            receipt = new ArrayList<String>(receipt);
        else
            receipt = new ArrayList<String>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public int getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(int invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public List<String> getReceipt() {
        return receipt;
    }

    public void setReceipt(List<String> receipt) {
        this.receipt = receipt;
    }

    public int getBankResponseCode() {
        return bankResponseCode;
    }

    public void setBankResponseCode(int bankResponseCode) {
        this.bankResponseCode = bankResponseCode;
    }

    public String getBankResponse() {
        return bankResponse;
    }

    public void setBankResponse(String bankResponse) {
        this.bankResponse = bankResponse;
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "id=" + id +
                ", timeStamp=" + timeStamp +
                ", approved=" + approved +
                ", terminalId='" + terminalId + '\'' +
                ", invoiceNumber=" + invoiceNumber +
                ", receipt=" + receipt +
                ", bankResponseCode=" + bankResponseCode +
                ", bankResponse='" + bankResponse + '\'' +
                '}';
    }
}
