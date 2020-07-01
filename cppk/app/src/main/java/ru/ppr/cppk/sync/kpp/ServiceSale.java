package ru.ppr.cppk.sync.kpp;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.sync.kpp.baseEntities.Event;
import ru.ppr.cppk.sync.kpp.model.Cashier;
import ru.ppr.cppk.sync.kpp.model.Check;

/**
 * Продажа услуг
 *
 * @author Grigoriy Kashka
 */
public class ServiceSale extends Event {

    /**
     * Код услуги
     */
    public String serviceCode;

    /**
     * Имя услуги
     */
    public String serviceName;

    public Cashier cashier;

    /**
     * Информация о сумме
     * В PaymentInfo - Информация о расчете. PaymentInfo.PaymentType - способ расчета
     */
    public BigDecimal price;

    public BigDecimal priceNds;

    /**
     * Чек
     */
    public Check check;

    /**
     * Дата и время продажи
     */
    public Date saleDateTime;

    public String paymentType;

    /**
     * Номер чека(операции)
     * В ЦОД приходит обязательно заполненным
     */
    public int ticketNumber;

    public int workingShiftNumber;

}
