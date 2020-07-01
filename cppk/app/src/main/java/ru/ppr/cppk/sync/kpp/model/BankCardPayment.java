package ru.ppr.cppk.sync.kpp.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Grigoriy Kashka
 */
public class BankCardPayment {
    /**
     * Согласно справочнику НСИ «Банки-эквайеры» ТСОППД
     */
    public int BankCode;

    /**
     * Уникальный для указанного банка номер операции
     */
    public String Rrn;

    /**
     * Код авторизации
     */
    public String AuthCode;

    /**
     * Номер терминала
     */
    public String TerminalId;

    /**
     * Маскированный номер карты
     */
    public String CardNumber;

    /**
     * Имя эмитента карты
     */
    public String CardType;

    /**
     * Момент формирования транзакции в терминале
     */
    public Date PaymentDateTime;

    /**
     * Сумма в запросе, руб
     */
    public BigDecimal Sum;

    /**
     * Идентификатор организации
     */
    public String OrganizationId;

    /**
     * Код валюты
     */
    public String CurrencyCode;

    /**
     * Номер точки продажи
     */
    public String SellerNumber;

    /**
     * Название приложения для чип-карты
     */
    public String ApplicationName;

    /**
     * Номер банковского чека
     */
    public String CheckNumber;

    /**
     * Сумма в ответе, руб
     */
    public BigDecimal ResponseSum;
}
