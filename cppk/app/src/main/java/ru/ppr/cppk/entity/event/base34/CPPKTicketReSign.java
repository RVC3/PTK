package ru.ppr.cppk.entity.event.base34;

import java.util.Date;

/**
 * Событие переподписи билета
 * (возникает при записи на карту билета в случае когда на карте уже есть билет, либо при аннулировании билета)
 */
public class CPPKTicketReSign {

    private long id = -1;

    private long eventId = -1;

    /**
     * Порядковый номер билета
     */
    private Integer ticketNumber;

    /**
     * Дата время оформления билета
     */
    private Date saleDateTime;

    /**
     * КО, где был оформлен билет
     */
    private String ticketDeviceId;

    /**
     * Номер ключа ЭЦП, которым переподписан билет
     */
    private Long edsKeyNumber;

    /**
     * Дата время совершения операции переподписи
     */
    private Date reSignDateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public Integer getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(Integer ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Date getSaleDateTime() {
        return saleDateTime;
    }

    public void setSaleDateTime(Date saleDateTime) {
        this.saleDateTime = saleDateTime;
    }

    public String getTicketDeviceId() {
        return ticketDeviceId;
    }

    public void setTicketDeviceId(String ticketDeviceId) {
        this.ticketDeviceId = ticketDeviceId;
    }

    public Long getEdsKeyNumber() {
        return edsKeyNumber;
    }

    public void setEdsKeyNumber(Long edsKeyNumber) {
        this.edsKeyNumber = edsKeyNumber;
    }

    public Date getReSignDateTime() {
        return reSignDateTime;
    }

    public void setReSignDateTime(Date reSignDateTime) {
        this.reSignDateTime = reSignDateTime;
    }

}
