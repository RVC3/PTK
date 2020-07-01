package ru.ppr.cppk.sync.kpp;

import java.util.Date;

import ru.ppr.cppk.sync.kpp.baseEntities.Event;

/**
 * @author Grigoriy Kashka
 */
public class CPPKTicketReSign extends Event {
    /**
     * Порядковый номер билета
     */
    public int ticketNumber;

    /**
     * Дата время оформления билета
     */
    public Date saleDateTime;

    /**
     * КО, где был оформлен билет
     */
    public String ticketDeviceId;

    /**
     * номер ключа ЭЦП, которым переподписан билет
     */
    public long edsKeyNumber;

    /**
     * Дата время совершения операции переподписи
     */
    public Date reSignDateTime;
}
