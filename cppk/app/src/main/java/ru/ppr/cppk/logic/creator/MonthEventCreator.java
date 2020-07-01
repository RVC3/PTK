package ru.ppr.cppk.logic.creator;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;

/**
 * Класс, выполняющий сборку {@link MonthEvent} и запись его в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class MonthEventCreator {

    private final EventCreator eventCreator;
    private final LocalDaoSession localDaoSession;
    private final LocalDbTransaction localDbTransaction;

    private MonthEvent.Status status;
    private String monthId;
    private int monthNumber;
    private Date openDate;
    private Date closeDate;

    @Inject
    MonthEventCreator(EventCreator eventCreator,
                      LocalDaoSession localDaoSession,
                      LocalDbTransaction localDbTransaction) {
        this.eventCreator = eventCreator;
        this.localDaoSession = localDaoSession;
        this.localDbTransaction = localDbTransaction;
    }

    public MonthEventCreator setStatus(MonthEvent.Status status) {
        this.status = status;
        return this;
    }

    public MonthEventCreator setMonthId(String monthId) {
        this.monthId = monthId;
        return this;
    }

    public MonthEventCreator setMonthNumber(int monthNumber) {
        this.monthNumber = monthNumber;
        return this;
    }

    public MonthEventCreator setOpenDate(Date openDate) {
        this.openDate = openDate;
        return this;
    }

    public MonthEventCreator setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
        return this;
    }

    /**
     * Выполнят сборку {@link MonthEvent} и запись его в БД.
     *
     * @return Сформированный {@link MonthEvent}
     */
    @NonNull
    public MonthEvent create() {
        return localDbTransaction.runInTx(this::createInternal);
    }

    @NonNull
    private MonthEvent createInternal() {
        Preconditions.checkNotNull(status);
        Preconditions.checkNotNull(monthId);
        Preconditions.checkNotNull(openDate);

        if (status == MonthEvent.Status.CLOSED) {
            Preconditions.checkNotNull(closeDate);
        }

        if (monthNumber <= 0) {
            throw new IllegalArgumentException("MonthNumber should be > 0");
        }

        // Пишем в БД Event
        Event event = eventCreator.create();

        CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().getLastCashRegisterEvent();

        Preconditions.checkNotNull(cashRegisterEvent);

        MonthEvent monthEvent = new MonthEvent();
        monthEvent.setMonthId(monthId);
        monthEvent.setMonthNumber(monthNumber);
        monthEvent.setOpenDate(openDate);
        monthEvent.setCloseDate(closeDate);
        monthEvent.setEventId(event.getId());
        monthEvent.setCashRegisterEventId(cashRegisterEvent.getId());
        monthEvent.setStatus(status);

        // Пишем в БД MonthEvent
        localDaoSession.getMonthEventDao().insertOrThrow(monthEvent);
        return monthEvent;
    }
}
