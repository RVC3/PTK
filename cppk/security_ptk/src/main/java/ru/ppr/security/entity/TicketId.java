package ru.ppr.security.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Класс id билета, присутствует в белых списках и стоп-листах билетов в
 * securityDb
 */
public class TicketId {
    private Date creationAt = new Date(0);
    private long deviceId = 0;
    private int ticketNumber = 0;

    public TicketId(int ticketNumber, long deviceId, Date creationAt) {
        this.ticketNumber = ticketNumber;
        this.deviceId = deviceId;
        this.creationAt = creationAt;
    }

    /**
     * Вернет TicketId в виде пригодном для записи в БД
     */
    public String getString() {
        SimpleDateFormat yyyyMMddHHmm = new SimpleDateFormat("yyyyMMddHHmm");
        yyyyMMddHHmm.setTimeZone(TimeZone.getTimeZone("UTC"));
        return ticketNumber + ":" + deviceId + ":" + yyyyMMddHHmm.format(creationAt);
    }
}
