package ru.ppr.cppk.localdb.model;

import java.util.Date;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Событие установки/изъятия билетной ленты.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTapeEvent implements LocalModelWithId<Long> {
    /**
     * Id
     */
    private Long id;
    /**
     * Id билетной ленты
     */
    private String ticketTapeId;
    /**
     * Id связанной сущности из таблицы CashRegisterEvent
     */
    private Long cashRegisterEventId;
    /**
     * Id связанной сущности из таблицы Event
     */
    private Long eventId;
    /**
     * Id события смены
     */
    private Long shiftEventId;
    /**
     * Id события месяца
     */
    private Long monthEventId;
    /**
     * Дата установки
     */
    private Date startTime;
    /**
     * Дата изъятия
     */
    private Date endTime;
    /**
     * Серия бобины
     */
    private String series;
    /**
     * Номер бобины
     */
    private int number;
    /**
     * Ожидаемый порядковый номер первого чека
     */
    private int expectedFirstDocNumber;
    /**
     * Расход билетной ленты на бобине
     */
    private long paperConsumption;
    /**
     * Флаг, говорящий о том, что счетчик был сброшен
     */
    private boolean paperCounterRestarted;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketTapeId() {
        return ticketTapeId;
    }

    public void setTicketTapeId(String ticketTapeId) {
        this.ticketTapeId = ticketTapeId;
    }

    public Long getCashRegisterEventId() {
        return cashRegisterEventId;
    }

    public void setCashRegisterEventId(Long cashRegisterEventId) {
        this.cashRegisterEventId = cashRegisterEventId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getShiftEventId() {
        return shiftEventId;
    }

    public void setShiftEventId(Long shiftEventId) {
        this.shiftEventId = shiftEventId;
    }

    public Long getMonthEventId() {
        return monthEventId;
    }

    public void setMonthEventId(Long monthEventId) {
        this.monthEventId = monthEventId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getExpectedFirstDocNumber() {
        return expectedFirstDocNumber;
    }

    public void setExpectedFirstDocNumber(int expectedFirstDocNumber) {
        this.expectedFirstDocNumber = expectedFirstDocNumber;
    }

    public long getPaperConsumption() {
        return paperConsumption;
    }

    public void setPaperConsumption(long paperConsumption) {
        this.paperConsumption = paperConsumption;
    }

    public boolean isPaperCounterRestarted() {
        return paperCounterRestarted;
    }

    public void setPaperCounterRestarted(boolean paperCounterRestarted) {
        this.paperCounterRestarted = paperCounterRestarted;
    }
}
