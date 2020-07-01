package ru.ppr.cppk.sync.kpp.model;

import java.util.Date;

/**
 * Заполняется при продаже разового ПД без места на 2-й сегмент поездки
 * инфо о билете на первый сегмент
 *
 * @author Grigoriy Kashka
 */
public class ParentTicketInfo {
    /**
     * Дата продажи
     */
    public Date SaleDateTime;

    /**
     * Номер билета
     */
    public int TicketNumber;

    /**
     * Номер кассы
     */
    public String CashRegisterNumber;

    /**
     * Направление действия ПД
     * TicketWayType
     * 0 - туда
     * 1 - туда обратно
     */
    public Integer WayType;
}
