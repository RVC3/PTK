package ru.ppr.cppk.model;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.entity.settings.ReportType;

/**
 * Created by Александр on 01.02.2016.
 */
public class PrintReportEvent {

    private long id;
    private long eventId;
    private long cashRegisterEventId;
    private ReportType reportType;
    private Date operationTime;
    private BigDecimal cashInFR = BigDecimal.ZERO;
    private long shiftEventId;
    private long monthEventId;
    private long ticketTapeEventId;

    public PrintReportEvent() {
    }

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

    public long getCashRegisterEventId() {
        return cashRegisterEventId;
    }

    public void setCashRegisterEventId(long cashRegisterEventId) {
        this.cashRegisterEventId = cashRegisterEventId;
    }

    public long getTicketTapeEventId() {
        return ticketTapeEventId;
    }

    public void setTicketTapeEventId(long ticketTapeEventId) {
        this.ticketTapeEventId = ticketTapeEventId;
    }

    public long getShiftEventId() {
        return shiftEventId;
    }

    public void setShiftEventId(long shiftEventId) {
        this.shiftEventId = shiftEventId;
    }

    public long getMonthEventId() {
        return monthEventId;
    }

    public void setMonthEventId(long monthEventId) {
        this.monthEventId = monthEventId;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public BigDecimal getCashInFR() {
        return cashInFR;
    }

    public void setCashInFR(BigDecimal cashInFR) {
        this.cashInFR = cashInFR;
    }
}
