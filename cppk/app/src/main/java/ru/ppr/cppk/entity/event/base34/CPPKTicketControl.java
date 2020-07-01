package ru.ppr.cppk.entity.event.base34;

import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;

/**
 * Created by Кашка Григорий on 13.12.2015.
 */
public class CPPKTicketControl {

    /**
     * локальный id текущего события контроля
     */
    private long id = -1;

    private long ticketEventBaseId = -1;

    /**
     * timestamp события контроля билета, в милисекундах. Это текущее время ПТК в момент контроля.
     * Никакого отновения к меткам прохода не имеет.
     */
    private Date controlDateTime;

    /**
     * номер ключа ЭЦП
     */
    private long edsKeyNumber;
    /**
     * Количество списанных поездок при контроле, по умолчанию - 0
     */
    private int tripsSpend = 0;

    /**
     * признак отзыва ЭЦП
     */
    private boolean isRevokedEds;

    /**
     * код отказа по стоп-листу : 0 — нет отказа; >0 — код стоп-листа (SmartCardStopListItem.ReasonCode)
     */
    private int stopListId = 0;

    /**
     * 4-значный код льготы
     */
    private int exemptionCode = -1;

    /**
     * Id билета, на основании которого была оформлена доплата, если таковая была.
     */
    private long parentTicketInfoId = -1;

    private PassageResult validationResult = null;

    private long eventId = -1;

    /**
     * Кол-во поездок на 7000-х поездах по абонементу на кол-во поездок, которое списалось при контроле
     */
    @Nullable
    private Integer trips7000Spend;

    /**
     * Количество оставшихся поездок по абонементу на кол-во поездок
     */
    @Nullable
    private Integer tripsCount;

    /**
     * Количество оставшихся поездок на 7000-х поездах по абонементу на кол-во поездок
     */
    @Nullable
    private Integer trips7000Count;

    /**
     * Ид устройства продажи
     */
    private long sellTicketDeviceId;

    /**
     * Номер проверенного билета
     */
    private int ticketNumber;

    /**
     * Восстановленный билет
     */
    private boolean isRestoredTicket;

    /**
     * Пункт отправления автобуса (для трансфера).
     * Соответствует значению «Код Экспресс» в справочнике НСИ «Станции»
     */
    private Long transferDeparturePoint;

    /**
     * Пункт назначения автобуса (для трансфера).
     * Соответствует значению «Код Экспресс» в справочнике НСИ «Станции»
     */
    private Long transferDestinationPoint;

    /**
     * Дата и время отправления автобуса (для трансфера), в UTC
     */
    @Nullable
    private Date transferDepartureDateTime;

    public long getSellTicketDeviceId() {
        return sellTicketDeviceId;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(int ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getParentTicketInfoId() {
        return parentTicketInfoId;
    }

    public void setParentTicketInfoId(long parentTicketInfoId) {
        this.parentTicketInfoId = parentTicketInfoId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getTicketEventBaseId() {
        return ticketEventBaseId;
    }

    public void setTicketEventBaseId(long ticketEventBaseId) {
        this.ticketEventBaseId = ticketEventBaseId;
    }

    public long getEdsKeyNumber() {
        return edsKeyNumber;
    }

    public void setEdsKeyNumber(long edsKeyNumber) {
        this.edsKeyNumber = edsKeyNumber;
    }

    public void setSellTicketDeviceId(long sellTicketDeviceId) {
        this.sellTicketDeviceId = sellTicketDeviceId;
    }

    public int getTripsSpend() {
        return tripsSpend;
    }

    public void setTripsSpend(int tripsSpend) {
        this.tripsSpend = tripsSpend;
    }

    public boolean isRevokedEds() {
        return isRevokedEds;
    }

    public void setRevokedEds(boolean revokedEds) {
        isRevokedEds = revokedEds;
    }

    public int getStopListId() {
        return stopListId;
    }

    public void setStopListId(int stopListId) {
        this.stopListId = stopListId;
    }

    public int getExemptionCode() {
        return exemptionCode;
    }

    public void setExemptionCode(int exemptionCode) {
        this.exemptionCode = exemptionCode;
    }

    public void setPassageResult(PassageResult passageResult) {
        this.validationResult = passageResult;
    }

    public PassageResult getPassgeResult() {
        return validationResult;
    }

    public void setControlTimestamp(Date timestamp) {
        controlDateTime = timestamp;
    }

    public Date getControlTime() {
        return controlDateTime;
    }

    public boolean isRestoredTicket() {
        return isRestoredTicket;
    }

    public void setRestoredTicket(boolean restoredTicket) {
        isRestoredTicket = restoredTicket;
    }

    public Long getTransferDeparturePoint() {
        return transferDeparturePoint;
    }

    public void setTransferDeparturePoint(Long transferDeparturePoint) {
        this.transferDeparturePoint = transferDeparturePoint;
    }

    public Long getTransferDestinationPoint() {
        return transferDestinationPoint;
    }

    public void setTransferDestinationPoint(Long transferDestinationPoint) {
        this.transferDestinationPoint = transferDestinationPoint;
    }

    public Integer getTrips7000Spend() {
        return trips7000Spend;
    }

    public void setTrips7000Spend(Integer trips7000Spend) {
        this.trips7000Spend = trips7000Spend;
    }

    public Integer getTripsCount() {
        return tripsCount;
    }

    public void setTripsCount(Integer tripsCount) {
        this.tripsCount = tripsCount;
    }

    public Integer getTrips7000Count() {
        return trips7000Count;
    }

    public void setTrips7000Count(Integer trips7000Count) {
        this.trips7000Count = trips7000Count;
    }

    public Date getTransferDepartureDateTime() {
        return transferDepartureDateTime;
    }

    public void setTransferDepartureDateTime(Date transferDepartureDateTime) {
        this.transferDepartureDateTime = transferDepartureDateTime;
    }
}
