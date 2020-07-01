package ru.ppr.cppk.sync.kpp.model;

import java.util.Date;

/**
 * Информация о талоне предварительного ПД.
 *
 * @author Grigoriy Kashka
 */
public class PreTicket {
    /**
     * Номер талона 16 цифр
     */
    public long preTicketNumber;

    /**
     * Дата и время печати (utc)
     */
    public Date printDateTime;

    /**
     * Id КО
     */
    public String deviceId;

    /**
     * Станция КО
     */
    public Station station;
}
