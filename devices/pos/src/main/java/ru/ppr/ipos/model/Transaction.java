package ru.ppr.ipos.model;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Aleksandr Brazhkin
 */
public class Transaction {

    public enum TransactionType {
        /**
         * Продажа
         */
        SALE(1),
        /**
         * Аннулирование
         */
        CANCELLATION(2),
        /**
         * Возврат
         */
        RETURN(3),
        /**
         * Вcе
         */
        ALL(4);

        /**
         * Код
         */
        private final int code;

        TransactionType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        @Nullable
        public static TransactionType valueOf(int code) {
            for (TransactionType type : TransactionType.values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * Идентификатор записи БД.
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
    /**
     * Сумма транзакции, в копейках.
     */
    private int amount;
    /**
     * Маскированный номер карты.
     */
    private String cardPAN;
    /**
     * Retrieval reference orderNumber.
     */
    private String RRN;
    /**
     * Идентификатор организации
     */
    private String merchantId;
    /**
     * Код авторазации.
     */
    private String authorizationId;
    /**
     * Имя эмитента карты.
     */
    private String issuerName;
    /**
     * Код валюты
     */
    private String currencyCode;
    /**
     * Имя приложения для чип карты.
     */
    private String applicationName;
    /**
     * Id дня в базе данных стаба.
     */
    private long dayId;

    /**
     * Тип банковской операции.
     */
    private TransactionType bankOperationType;
    /**
     * Id транзакции
     */
    private int transactionId;

    public Transaction() {
    }

    public Transaction(int id, int transactionId, int amount) {
        setId(id);
        setTransactionId(transactionId);
        setTimeStamp(new Date());
        setApproved(true);
        setTerminalId("StubTerminal");
        setInvoiceNumber(0);
        setAmount(amount);
        setCardPAN("0000");
        setRRN("0.0.0");
        setMerchantId("MerchantId");
        setAuthorizationId("000000 A");
        setIssuerName("IssuerName");
        setCurrencyCode("RUB");
        setApplicationName("ApplicationName");
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "id=" + transactionId +
                ", timeStamp=" + timeStamp +
                ", approved=" + approved +
                ", terminalId='" + terminalId + '\'' +
                ", invoiceNumber=" + invoiceNumber +
                ", receipt=" + receipt +
                ", bankResponseCode=" + bankResponseCode +
                ", bankResponse='" + bankResponse + '\'' +
                '}';
    }

    public List<String> getReceipt() {
        List<String> receipt = new ArrayList<>();
        receipt.add("id = " + getTransactionId());
        receipt.add("timeStamp = " + getTimeStamp());
        receipt.add("approved = " + isApproved());
        receipt.add("terminalId = " + getTerminalId());
        receipt.add("invoiceNumber = " + getInvoiceNumber());
        receipt.add("bankResponseCode = " + getBankResponseCode());
        receipt.add("bankResponse = " + getBankResponse());
        receipt.add("amount = " + getAmount());
        receipt.add("cardPAN = " + getCardPAN());
        receipt.add("RRN = " + getRRN());
        receipt.add("merchantId = " + getMerchantId());
        receipt.add("authorizationId = " + getAuthorizationId());
        receipt.add("issuerName = " + getIssuerName());
        receipt.add("currencyCode = " + getCurrencyCode());
        receipt.add("applicationName = " + getApplicationName());
        return receipt;
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCardPAN() {
        return cardPAN;
    }

    public void setCardPAN(String cardPAN) {
        this.cardPAN = cardPAN;
    }

    public String getRRN() {
        return RRN;
    }

    public void setRRN(String RRN) {
        this.RRN = RRN;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getAuthorizationId() {
        return authorizationId;
    }

    public void setAuthorizationId(String authorizationId) {
        this.authorizationId = authorizationId;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public long getDayId() {
        return dayId;
    }

    public void setDayId(long dayId) {
        this.dayId = dayId;
    }

    public TransactionType getBankOperationType() {
        return bankOperationType;
    }

    public void setBankOperationType(TransactionType bankOperationType) {
        this.bankOperationType = bankOperationType;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
}
