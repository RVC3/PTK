package ru.ppr.cppk.entity.event.base34;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.localdb.model.ShiftEvent;

/**
 * Created by Dmitry Nevolin on 20.01.2016.
 */
public class TerminalDay {

    private long id;
    private long terminalDayId;
    private long eventId;
    private long startShiftEventId;
    private long endShiftEventId;
    private Date startDateTime;
    private Date endDateTime;
    private String report;
    private int currentSaleTransactionId;

    public void setTerminalNumber(String terminalNumber) {
        this.terminalNumber = terminalNumber;
    }

    private String terminalNumber;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTerminalDayId() {
        return terminalDayId;
    }

    public void setTerminalDayId(long terminalDayId) {
        this.terminalDayId = terminalDayId;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getStartShiftEventId() {
        return startShiftEventId;
    }

    public void setStartShiftEventId(long startShiftEventId) {
        this.startShiftEventId = startShiftEventId;
    }

    public long getEndShiftEventId() {
        return endShiftEventId;
    }

    public void setEndShiftEventId(long endShiftEventId) {
        this.endShiftEventId = endShiftEventId;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    @Nullable
    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    @Nullable
    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public int getCurrentSaleTransactionId() {
        return currentSaleTransactionId;
    }

    public void setCurrentSaleTransactionId(int currentSaleTransactionId) {
        this.currentSaleTransactionId = currentSaleTransactionId;
    }

    public String getTerminalNumber() {
        return terminalNumber;
    }
}
