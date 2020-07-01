package ru.ppr.cppk.logic.creator;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;

/**
 * Класс, выполняющий сборку {@link TicketTapeEvent} и запись его в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTapeEventCreator {

    private final EventCreator eventCreator;
    private final LocalDaoSession localDaoSession;
    private final LocalDbTransaction localDbTransaction;

    private String ticketTapeId;
    private String series;
    private int number;
    private int expectedFirstDocNumber;
    private long paperConsumption;
    private boolean paperCounterRestarted;
    private Date startTime;
    private Date endTime;

    @Inject
    TicketTapeEventCreator(EventCreator eventCreator,
                           LocalDaoSession localDaoSession,
                           LocalDbTransaction localDbTransaction) {
        this.eventCreator = eventCreator;
        this.localDaoSession = localDaoSession;
        this.localDbTransaction = localDbTransaction;
    }

    public TicketTapeEventCreator setTicketTapeId(String ticketTapeId) {
        this.ticketTapeId = ticketTapeId;
        return this;
    }

    public TicketTapeEventCreator setSeries(String series) {
        this.series = series;
        return this;
    }

    public TicketTapeEventCreator setNumber(int number) {
        this.number = number;
        return this;
    }

    public TicketTapeEventCreator setExpectedFirstDocNumber(int expectedFirstDocNumber) {
        this.expectedFirstDocNumber = expectedFirstDocNumber;
        return this;
    }

    public TicketTapeEventCreator setPaperConsumption(long paperConsumption) {
        this.paperConsumption = paperConsumption;
        return this;
    }

    public TicketTapeEventCreator setPaperCounterRestarted(boolean paperCounterRestarted) {
        this.paperCounterRestarted = paperCounterRestarted;
        return this;
    }

    public TicketTapeEventCreator setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public TicketTapeEventCreator setEndTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Выполнят сборку {@link TicketTapeEvent} и запись его в БД.
     *
     * @return Сформированный {@link TicketTapeEvent}
     */
    @NonNull
    public TicketTapeEvent create() {
        return localDbTransaction.runInTx(this::createInternal);
    }

    @NonNull
    private TicketTapeEvent createInternal() {
        Preconditions.checkNotNull(ticketTapeId);
        Preconditions.checkNotNull(series);
        Preconditions.checkNotNull(number);
        Preconditions.checkNotNull(startTime);

        MonthEvent monthEvent = localDaoSession.getMonthEventDao().getLastMonthEvent();
        if (monthEvent == null || monthEvent.getStatus() != MonthEvent.Status.OPENED) {
            throw new IllegalStateException("Month is not opened");
        }

        Long shiftEventId = null;
        ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        if (shiftEvent != null) {
            if (shiftEvent.getStatus() == ShiftEvent.Status.STARTED || shiftEvent.getStatus() == ShiftEvent.Status.TRANSFERRED) {
                // Устанавливаем Id события смены только если смена открыта
                shiftEventId = shiftEvent.getId();
            }
        }

        CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().getLastCashRegisterEvent();
        Preconditions.checkNotNull(cashRegisterEvent);

        // Пишем в БД Event
        Event event = eventCreator.create();

        TicketTapeEvent ticketTapeEvent = new TicketTapeEvent();
        ticketTapeEvent.setTicketTapeId(ticketTapeId);
        ticketTapeEvent.setSeries(series);
        ticketTapeEvent.setNumber(number);
        ticketTapeEvent.setExpectedFirstDocNumber(expectedFirstDocNumber);
        ticketTapeEvent.setPaperConsumption(paperConsumption);
        ticketTapeEvent.setPaperCounterRestarted(paperCounterRestarted);
        ticketTapeEvent.setStartTime(startTime);
        ticketTapeEvent.setEndTime(endTime);
        ticketTapeEvent.setMonthEventId(monthEvent.getId());
        ticketTapeEvent.setShiftEventId(shiftEventId);
        ticketTapeEvent.setEventId(event.getId());
        ticketTapeEvent.setCashRegisterEventId(cashRegisterEvent.getId());

        // Пишем в БД TicketTapeEvent
        localDaoSession.getTicketTapeEventDao().insertOrThrow(ticketTapeEvent);
        return ticketTapeEvent;
    }
}
