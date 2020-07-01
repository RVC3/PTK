package ru.ppr.cppk.entity.event.base34;

import java.util.Date;

/**
 * Created by Александр on 20.07.2016.
 * <p>
 * Событие "Продажа услуги"
 * Используется для фиксации денег, при привязке принтера, с ненулевым балансом
 */
public class CPPKServiceSale {

    //region Fields
    /**
     * Primary key
     */
    private long id;
    /**
     * ID связанной сущности из таблицы Event
     */
    private long eventId;

    /**
     * ID связанной сущности из таблицы CashRegisterWorkingShift
     */
    private long shiftEventId = -1;
    /**
     * ID связанной сущности из таблицы TicketTapeEvent
     */
    private long ticketTapeEventId = -1;
    /**
     * ID связанной сущности из таблицы Price
     */
    private long checkId;
    /**
     * ID связанной сущности из таблицы Check
     */
    private long priceId;
    /**
     * ID связанной сущности из таблицы ServiceFees
     */
    private long serviceFeeCode;
    /**
     * Наименование услуги
     */
    private String serviceFeeName;
    /**
     * Время продажи услуги.
     * В базу это значение должно сохраняться в секундах
     */
    private Date saleDateTime;
    //endregion

    public CPPKServiceSale() {

    }

    //region Fields getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getSaleDateTime() {
        return saleDateTime;
    }

    public void setSaleDateTime(Date saleDateTime) {
        this.saleDateTime = saleDateTime;
    }

    public String getServiceFeeName() {
        return serviceFeeName;
    }

    public void setServiceFeeName(String serviceFeeName) {
        this.serviceFeeName = serviceFeeName;
    }

    //region Event getters and setters
    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
    //endregion

    //region TicketTapeEvent getters and setters
    public long getTicketTapeEventId() {
        return ticketTapeEventId;
    }

    public void setTicketTapeEventId(long ticketTapeEventId) {
        this.ticketTapeEventId = ticketTapeEventId;
    }
    //endregion

    //region CashRegisterWorkingShift getters and setters
    public long getShiftEventId() {
        return shiftEventId;
    }

    public void setShiftEventId(long shiftEventId) {
        this.shiftEventId = shiftEventId;
    }
    //endregion

    //region Check getters and setters
    public long getCheckId() {
        return checkId;
    }

    public void setCheckId(long checkId) {
        this.checkId = checkId;
    }
    //endregion

    //region Price getters and setters
    public long getPriceId() {
        return priceId;
    }

    public void setPriceId(long priceId) {
        this.priceId = priceId;
    }
    //endregion

    //region ServiceFee getters and setters
    public long getServiceFeeCode() {
        return serviceFeeCode;

    }

    public void setServiceFeeCode(long serviceFeeCode) {
        this.serviceFeeCode = serviceFeeCode;
    }
    //endregion
    //endregion
}
