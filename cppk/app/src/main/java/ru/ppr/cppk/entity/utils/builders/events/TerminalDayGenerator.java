package ru.ppr.cppk.entity.utils.builders.events;

import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.base34.TerminalDay;
import ru.ppr.cppk.localdb.model.ShiftEvent;

/**
 * Created by Dmitry Nevolin on 20.01.2016.
 */
public class TerminalDayGenerator extends AbstractGenerator implements Generator<TerminalDay> {

    private Long TerminalDayId;
    private Date StartDateTime;
    private Date EndDateTime;
    private String Report;
    private Integer CurrentSaleTransactionId;
    private String TerminalNumber;

    private Event event;
    private Long startShiftEventId;
    private Long endShiftEventId;

    public TerminalDayGenerator setTerminalDayId(Long terminalDayId) {
        TerminalDayId = terminalDayId;

        return this;
    }

    public TerminalDayGenerator setStartDateTime(Date startDateTime) {
        StartDateTime = startDateTime;

        return this;
    }

    public TerminalDayGenerator setEndDateTime(Date endDateTime) {
        EndDateTime = endDateTime;

        return this;
    }

    public TerminalDayGenerator setReport(String report) {
        Report = report;

        return this;
    }

    public TerminalDayGenerator setCurrentSaleTransactionId(int currentSaleTransactionId) {
        CurrentSaleTransactionId = currentSaleTransactionId;

        return this;
    }

    public TerminalDayGenerator setEvent(Event event) {
        this.event = event;

        return this;
    }

    public TerminalDayGenerator setStartShiftEventId(Long startShiftEventId) {
        this.startShiftEventId = startShiftEventId;

        return this;
    }

    public TerminalDayGenerator setEndShiftEventId(Long endShiftEventId) {
        this.endShiftEventId = endShiftEventId;

        return this;
    }

    public TerminalDayGenerator setTerminalNumber(String terminalNumber) {
        TerminalNumber = terminalNumber;
        return this;
    }

    @NonNull
    @Override
    public TerminalDay build() {
        checkNotNull(TerminalDayId, "TerminalDayId is null");
        checkNotNull(StartDateTime, "StartDateTime is null");
        checkNotNull(event, "event is null");
        checkNotNull(CurrentSaleTransactionId, "CurrentSaleTransactionId is null");
        checkNotNull(TerminalNumber, "TerminalNumber is null");

        TerminalDay terminalDay = new TerminalDay();

        terminalDay.setTerminalDayId(TerminalDayId);
        terminalDay.setEventId(event.getId());
        terminalDay.setStartShiftEventId(startShiftEventId == null ? 0 : startShiftEventId);
        terminalDay.setEndShiftEventId(endShiftEventId == null ? 0 : endShiftEventId);
        terminalDay.setStartDateTime(StartDateTime);
        terminalDay.setEndDateTime(EndDateTime);
        terminalDay.setReport(Report);
        terminalDay.setEventId(event.getId());
        terminalDay.setCurrentSaleTransactionId(CurrentSaleTransactionId);
        terminalDay.setTerminalNumber(TerminalNumber);

        return terminalDay;
    }

}
