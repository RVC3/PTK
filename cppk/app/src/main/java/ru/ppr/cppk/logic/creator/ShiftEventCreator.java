package ru.ppr.cppk.logic.creator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;
import ru.ppr.logger.Logger;

/**
 * Класс, выполняющий сборку {@link ShiftEvent} и запись его в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class ShiftEventCreator {

    private static final String TAG = Logger.makeLogTag(ShiftEventCreator.class);

    private final EventCreator eventCreator;
    private final LocalDaoSession localDaoSession;
    private final LocalDbTransaction localDbTransaction;

    private ShiftEvent.Status status;
    private String shiftId;
    private int shiftNumber;
    private Date startTime;
    private Date operationTime;
    private Date closeTime;
    private long paperConsumption;
    private boolean paperCounterRestarted;
    private BigDecimal cashInFR;
    /**
     * Фискальный чек
     */
    private Check check;
    /**
     * Статус операции
     */
    private ShiftEvent.ShiftProgressStatus progressStatus;

    @Inject
    ShiftEventCreator(EventCreator eventCreator,
                      LocalDaoSession localDaoSession,
                      LocalDbTransaction localDbTransaction) {
        this.eventCreator = eventCreator;
        this.localDaoSession = localDaoSession;
        this.localDbTransaction = localDbTransaction;
    }

    public ShiftEventCreator setStatus(ShiftEvent.Status status) {
        this.status = status;
        return this;
    }

    public ShiftEventCreator setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public ShiftEventCreator setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
        return this;
    }

    public ShiftEventCreator setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public ShiftEventCreator setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
        return this;
    }

    public ShiftEventCreator setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
        return this;
    }

    public ShiftEventCreator setPaperConsumption(long paperConsumption) {
        this.paperConsumption = paperConsumption;
        return this;
    }

    public ShiftEventCreator setPaperCounterRestarted(boolean paperCounterRestarted) {
        this.paperCounterRestarted = paperCounterRestarted;
        return this;
    }

    public ShiftEventCreator setCashInFR(BigDecimal cashInFR) {
        this.cashInFR = cashInFR;
        return this;
    }

    /**
     * Задать идентификатор чека
     *
     * @param check
     * @return
     */
    public ShiftEventCreator setCheck(@Nullable Check check) {
        this.check = check;
        return this;
    }

    /**
     * Задать идентификатор чека
     *
     * @param progressStatus
     * @return
     */
    public ShiftEventCreator setProgressStatus(@NonNull ShiftEvent.ShiftProgressStatus progressStatus) {
        this.progressStatus = progressStatus;
        return this;
    }

    /**
     * Выполнят сборку {@link ShiftEvent} и запись его в БД.
     *
     * @return Сформированный {@link ShiftEvent}
     */
    @NonNull
    public ShiftEvent create() {
        return localDbTransaction.runInTx(this::createInternal);
    }

    @NonNull
    private ShiftEvent createInternal() {
        Preconditions.checkNotNull(status);
        Preconditions.checkNotNull(shiftId);
        Preconditions.checkNotNull(startTime);
        Preconditions.checkNotNull(operationTime);
        Preconditions.checkNotNull(progressStatus);

        if (status == ShiftEvent.Status.ENDED && ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES.contains(progressStatus)) {
            Preconditions.checkNotNull(closeTime);
        }

        if (status != ShiftEvent.Status.TRANSFERRED) {
            Preconditions.checkNotNull(cashInFR);
        }

        MonthEvent monthEvent = localDaoSession.getMonthEventDao().getLastMonthEvent();
        if (monthEvent == null || monthEvent.getStatus() != MonthEvent.Status.OPENED) {
            throw new IllegalStateException("Month is not opened");
        }

        if (shiftNumber <= 0) {
            Logger.trace(TAG, "ShiftEventCreator shiftNumber = " + shiftNumber);
            throw new IllegalArgumentException("ShiftNumber should be > 0");
        }

        CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().getLastCashRegisterEvent();
        Preconditions.checkNotNull(cashRegisterEvent);

        ShiftEvent shiftEvent = new ShiftEvent();

        try {
            localDaoSession.beginTransaction();

            // Пишем в БД Event
            Event event = eventCreator.create();

            shiftEvent.setShiftId(shiftId);
            shiftEvent.setShiftNumber(shiftNumber);
            shiftEvent.setStartTime(startTime);
            shiftEvent.setCloseTime(closeTime);
            shiftEvent.setOperationTime(operationTime);
            shiftEvent.setPaperConsumption(paperConsumption);
            shiftEvent.setPaperCounterRestarted(paperCounterRestarted);
            shiftEvent.setCashInFR(cashInFR);
            shiftEvent.setEventId(event.getId());
            shiftEvent.setCashRegisterEventId(cashRegisterEvent.getId());
            shiftEvent.setMonthEventId(monthEvent.getId());
            shiftEvent.setStatus(status);
            shiftEvent.setProgressStatus(progressStatus);

            // В будущем выпилить этот костыль и сделать отдельную сущность для хранения фискальных документов принтера
            if (check != null) {
                shiftEvent.setCheckId(localDaoSession.getCheckDao().insertOrThrow(check));
            } else {
                shiftEvent.setCheckId(null);
            }

            localDaoSession.getShiftEventDao().insertOrThrow(shiftEvent);

            localDaoSession.setTransactionSuccessful();
        } finally {
            localDaoSession.endTransaction();
        }

        return shiftEvent;
    }
}
