package ru.ppr.cppk.localdb.model;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Событие совершения банковской транзакции.
 *
 * @author Grigoriy Kashka
 */
public class BankTransactionEvent implements LocalModelWithId<Long> {
    /**
     * Id
     */
    private Long id;
    /**
     * Id транзакции с точки зрения терминала
     */
    private int transactionId;
    /**
     * Id связанного события смены
     */
    private long cashRegisterWorkingShiftId;
    /**
     * Номер терминала
     */
    private String terminalNumber;
    /**
     * Номер точки продажи, присвоенный банком
     * Netman: Нет такого в данных транзакции
     */
    private String pointOfSaleNumber;
    /**
     * Идентификатор организации
     * «Merchant No» устанавливается в настройках POS-терминала
     */
    private String merchantId;
    /**
     * Код банка
     */
    private int bankCode = -1;
    /**
     * Тип операции (подажа/анулирование/возврат)
     */
    private BankOperationType operationType;
    /**
     * Результат операции
     */
    private BankOperationResult operationResult;
    /**
     * Retrieval reference orderNumber
     */
    private String rrn;
    /**
     * Код авторизации
     */
    private String authorizationCode;
    /**
     * Название приложения для чип-карты
     */
    private String smartCardApplicationName;
    /**
     * Маскированный номер карты
     */
    private String cardPan;
    /**
     * Имя эмитента карты
     */
    private String cardEmitentName;
    /**
     * Номер банковского чека
     */
    private long bankCheckNumber;
    /**
     * Момент формирования транзакции в терминале
     */
    private Date transactionDateTime;
    /**
     * Сумма в рублях, при том что сам терминал работает с копейками
     */
    private BigDecimal total = BigDecimal.ZERO;
    /**
     * Код валюты
     * Код, определенный терминалом с банковским хостом
     */
    private String currencyCode;
    /**
     * Id связанного события
     */
    private long eventId;
    /**
     * Id банковского дня
     */
    private long terminalDayId;
    /**
     * Статус транзакции
     */
    private Status status;
    /**
     * Id связанного события месяца
     */
    private long monthId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public long getCashRegisterWorkingShiftId() {
        return cashRegisterWorkingShiftId;
    }

    public void setCashRegisterWorkingShiftId(long cashRegisterWorkingShiftId) {
        this.cashRegisterWorkingShiftId = cashRegisterWorkingShiftId;
    }

    public String getTerminalNumber() {
        return terminalNumber;
    }

    public void setTerminalNumber(String terminalNumber) {
        this.terminalNumber = terminalNumber;
    }

    public String getPointOfSaleNumber() {
        return pointOfSaleNumber;
    }

    public void setPointOfSaleNumber(String pointOfSaleNumber) {
        this.pointOfSaleNumber = pointOfSaleNumber;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public int getBankCode() {
        return bankCode;
    }

    public void setBankCode(int bankCode) {
        this.bankCode = bankCode;
    }

    public BankOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(BankOperationType operationType) {
        this.operationType = operationType;
    }

    public BankOperationResult getOperationResult() {
        return operationResult;
    }

    public void setOperationResult(BankOperationResult operationResult) {
        this.operationResult = operationResult;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getSmartCardApplicationName() {
        return smartCardApplicationName;
    }

    public void setSmartCardApplicationName(String smartCardApplicationName) {
        this.smartCardApplicationName = smartCardApplicationName;
    }

    public String getCardPan() {
        return cardPan;
    }

    public void setCardPan(String cardPan) {
        this.cardPan = cardPan;
    }

    public String getCardEmitentName() {
        return cardEmitentName;
    }

    public void setCardEmitentName(String cardEmitentName) {
        this.cardEmitentName = cardEmitentName;
    }

    public long getBankCheckNumber() {
        return bankCheckNumber;
    }

    public void setBankCheckNumber(long bankCheckNumber) {
        this.bankCheckNumber = bankCheckNumber;
    }

    public Date getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(Date transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }

    @NonNull
    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(@NonNull BigDecimal total) {
        this.total = total;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getTerminalDayId() {
        return terminalDayId;
    }

    public void setTerminalDayId(long terminalDayId) {
        this.terminalDayId = terminalDayId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getMonthId() {
        return monthId;
    }

    public void setMonthId(long monthId) {
        this.monthId = monthId;
    }

    public enum Status {
        /**
         * Транзакция еще не проведена на терминале
         */
        STARTED(0),
        /**
         * Транзакция прошла, а по ФР ошибка
         */
        COMPLETED_BUT_NOT_ASSOCIATED_WITH_FISCAL_OPERATION(1),
        /**
         * Транзакция успешно проведена
         */
        COMPLETED_FULLY(2),
        /**
         * Транзакция жестко проведена без POS-терминала
         */
        COMPLETED_WITHOUT_POS(3);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status valueOf(int code) {
            for (Status item : values()) {
                if (item.code == code) {
                    return item;
                }
            }
            return STARTED;
        }
    }

}
