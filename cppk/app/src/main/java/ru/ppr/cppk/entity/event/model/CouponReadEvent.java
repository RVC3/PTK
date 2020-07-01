package ru.ppr.cppk.entity.event.model;

import java.util.Date;

import ru.ppr.security.entity.PtsKey;

/**
 * Информация о талоне предварительного ПД.
 *
 * @author Grigoriy Kashka
 */
public class CouponReadEvent {

    /**
     * локальный идентификатор
     */
    private long id;
    /**
     * Номер талона 16 цифр
     */
    private long preTicketNumber;
    /**
     * Дата и время печати (utc)
     */
    private Date printDateTime;
    /**
     * Id КО
     */
    private String deviceId;
    /**
     * Код станции
     */
    private long stationCode;
    /**
     * Статус события
     */
    private Status status;
    /**
     * Id связанной сущности из таблицы Event
     */
    private long eventId = -1;
    /**
     * Id связанной сущности из таблицы CashRegisterWorkingShift
     */
    private long shiftEventId = -1;
    /**
     * Id связанной сущности {@link PtsKey}
     */
    private String ptsKeyId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPreTicketNumber() {
        return preTicketNumber;
    }

    public void setPreTicketNumber(long preTicketNumber) {
        this.preTicketNumber = preTicketNumber;
    }

    public Date getPrintDateTime() {
        return printDateTime;
    }

    public void setPrintDateTime(Date printDateTime) {
        this.printDateTime = printDateTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    //region Station getter and setter
    public long getStationCode() {
        return stationCode;
    }

    public void setStationCode(long stationCode) {
        this.stationCode = stationCode;
    }
    //endregion

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    //region Event getters and setters
    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
    //endregion

    //region ShiftEvent getters and setters
    public long getShiftEventId() {
        return shiftEventId;
    }

    public void setShiftEventId(long shiftEventId) {
        this.shiftEventId = shiftEventId;
    }
    //endregion

    //region PtsKey getters and setters
    public String getPtsKeyId() {
        return ptsKeyId;
    }

    public void setPtsKeyId(String ptsKeyId) {
        this.ptsKeyId = ptsKeyId;
    }
    //endregion

    /**
     * Статус события
     */
    public enum Status {
        /**
         * Создано в БД.
         */
        CREATED(0);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status fromCode(int code) {
            for (Status item : values()) {
                if (item.code == code) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Unknown code = " + code);
        }
    }
}
