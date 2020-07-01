package ru.ppr.chit.api.entity;

/**
 * Ключ билета
 * Состоит из трех полей
 *
 * @author Dmitry Nevolin
 */
public class TicketIdEntity {

    /**
     * Порядковый номер документа
     */
    private long ticketNumber;
    /**
     * Время продажи в UTC
     */
    private String saleDateTimeUtc;
    /**
     * Id оборудования, которое продало билет
     */
    private String deviceId;

    public long getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(long ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getSaleDateTimeUtc() {
        return saleDateTimeUtc;
    }

    public void setSaleDateTimeUtc(String saleDateTimeUtc) {
        this.saleDateTimeUtc = saleDateTimeUtc;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

}
