package ru.ppr.cppk.entity.event.base34;

import java.math.BigDecimal;

import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.entity.event.model34.WritePdToBscError;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * Created by Кашка Григорий on 13.12.2015.
 */
public class CPPKTicketSales {

    /**
     * локальный id текущего события
     */
    private long id = -1;

    /**
     * Первичный ключ для таблицы TicketSaleReturnEventBase
     */
    private long ticketSaleReturnEventBaseId = -1;

    /// Количество поездок
    /// Необязательное, указывается для абонементов «на количество поездок»
    private Integer tripsCount;

    /// Код типа носителя ПД
    /// Соответствует коду типа носителя в справочнике НСИ «Типы носителей ПД».
    private TicketStorageType storageTypeCode;

    /// номер ключа ЭЦП
    /// Параметр необходим для формирования Белого списка при отзыве ключа
    private long EDSKeyNumber = 0;

    // если произошла ошибка при записи на бск после печати фискального чека,
    // то этот параметр должен быть не null
    private WritePdToBscError errors;

    private long eventId;

    private long ticketTapeEventId;

    private ProgressStatus progressStatus;

    /**
     * Сколько стоит билет без учета льготы
     */
    private BigDecimal fullTicketPrice = BigDecimal.ZERO;

    /**
     * Признак связи с другим ПД - доплата, транзит, 2-й сегмент или трансфер
     */
    private ConnectionType connectionType = null;

    /**
     * Первичный ключ для таблицы {@link ru.ppr.cppk.entity.event.model.CouponReadEvent}
     */
    private long couponReadEventId = -1;

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getTicketTapeEventId() {
        return ticketTapeEventId;
    }

    public void setTicketTapeEventId(long ticketTapeEventId) {
        this.ticketTapeEventId = ticketTapeEventId;
    }

    public WritePdToBscError getErrors() {
        return errors;
    }

    public void setErrors(WritePdToBscError errors) {
        this.errors = errors;
    }

    public long getEDSKeyNumber() {
        return EDSKeyNumber;
    }

    public void setEDSKeyNumber(long EDSKeyNumber) {
        this.EDSKeyNumber = EDSKeyNumber;
    }

    public TicketStorageType getStorageTypeCode() {
        return storageTypeCode;
    }

    public void setStorageTypeCode(TicketStorageType storageTypeCode) {
        this.storageTypeCode = storageTypeCode;
    }

    public Integer getTripsCount() {
        return tripsCount;
    }

    public void setTripsCount(int tripsCount) {
        this.tripsCount = tripsCount;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getTicketSaleReturnEventBaseId() {
        return ticketSaleReturnEventBaseId;
    }

    public void setTicketSaleReturnEventBaseId(long ticketSaleReturnEventBaseId) {
        this.ticketSaleReturnEventBaseId = ticketSaleReturnEventBaseId;
    }

    public ProgressStatus getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(ProgressStatus progressStatus) {
        this.progressStatus = progressStatus;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public BigDecimal getFullTicketPrice() {
        return fullTicketPrice;
    }

    public void setFullTicketPrice(BigDecimal fullTicketPrice) {
        this.fullTicketPrice = fullTicketPrice;
    }

    public long getCouponReadEventId() {
        return couponReadEventId;
    }

    public void setCouponReadEventId(long couponReadEventId) {
        this.couponReadEventId = couponReadEventId;
    }
}
