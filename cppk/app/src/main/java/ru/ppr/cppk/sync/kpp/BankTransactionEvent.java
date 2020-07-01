package ru.ppr.cppk.sync.kpp;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.sync.kpp.baseEntities.CashRegisterEvent;

/**
 * Событие «Транзакция на POS-терминале»
 *
 * @author Grigoriy Kashka
 */
public class BankTransactionEvent extends CashRegisterEvent {


    /**
     * ERN - Уникальный номер транзакции в рамках дня pos-терминала
     */
    public int ern;


    /**
     * Номер терминала
     */
    public String terminalNumber;


    /**
     * Номер точки продажи, присвоенный банком
     * Netman: Нет такого в данных транзакции
     */
    public String pointOfSaleNumber;


    /**
     * Идентификатор организации
     * «Merchant No» устанавливается в настройках POS-терминала
     */
    public String merchantId;


    /**
     * Код банка
     */
    public int bankCode;


    /**
     * Тип операции (подажа/анулирование/возврат)
     */
    public int operationType;


    /**
     * Результат операции
     */
    public int operationResult;


    /**
     * Retrieval reference number
     */
    public String rrn;


    /**
     * Код авторизации
     */
    public String authorizationCode;


    /**
     * Название приложения для чип-карты
     */
    public String smartCardApplicationName;


    /**
     * Маскированный номер карты
     */
    public String cardPan;


    /**
     * Имя эмитента карты
     */
    public String cardEmitentName;


    /**
     * Номер банковского чека
     */
    public int bankCheckNumber;


    /**
     * Момент формирования транзакции в терминале
     */
    public Date transactionDateTime;


    /**
     * Сумма в рублях
     */
    public BigDecimal total;


    /**
     * Код валюты
     * /** Код, определенный терминалом с банковским хостом
     */
    public String currencyCode;

}
