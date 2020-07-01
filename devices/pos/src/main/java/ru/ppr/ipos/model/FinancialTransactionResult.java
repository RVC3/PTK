package ru.ppr.ipos.model;

/**
 * Created by Dmitry Nevolin on 20.11.2015.
 */
public class FinancialTransactionResult extends TransactionResult {

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

    public FinancialTransactionResult() {

    }

    public FinancialTransactionResult(TransactionResult transactionResult) {
        super(transactionResult);
    }

    public FinancialTransactionResult(FinancialTransactionResult financialTransactionResult) {
        super(financialTransactionResult);

        amount = financialTransactionResult.amount;
        cardPAN = financialTransactionResult.cardPAN;
        RRN = financialTransactionResult.RRN;
        merchantId = financialTransactionResult.merchantId;
        authorizationId = financialTransactionResult.authorizationId;
        issuerName = financialTransactionResult.issuerName;
        currencyCode = financialTransactionResult.currencyCode;
        applicationName = financialTransactionResult.applicationName;
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

}
