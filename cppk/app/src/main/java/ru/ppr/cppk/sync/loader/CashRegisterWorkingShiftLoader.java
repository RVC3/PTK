package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.sync.kpp.CashRegisterWorkingShift;
import ru.ppr.cppk.sync.loader.CashRegisterEventData.ShiftClosureStatisticsBuilder;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.EventsStatisticLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class CashRegisterWorkingShiftLoader extends BaseLoader {

    private final EventLoader eventLoader;
    private final CashRegisterEventLoader cashRegisterEventLoader;
    private final WorkingShiftEventLoader workingShiftEventLoader;
    private final EventsStatisticLoader eventsStatisticLoader;
    private final ShiftClosureStatisticsBuilder shiftClosureStatisticsBuilder;

    public CashRegisterWorkingShiftLoader(LocalDaoSession localDaoSession,
                                          NsiDaoSession nsiDaoSession,
                                          EventLoader eventLoader,
                                          CashRegisterEventLoader cashRegisterEventLoader,
                                          WorkingShiftEventLoader workingShiftEventLoader,
                                          EventsStatisticLoader eventsStatisticLoader,
                                          ShiftClosureStatisticsBuilder shiftClosureStatisticsBuilder) {
        super(localDaoSession, nsiDaoSession);
        this.eventLoader = eventLoader;
        this.cashRegisterEventLoader = cashRegisterEventLoader;
        this.workingShiftEventLoader = workingShiftEventLoader;
        this.eventsStatisticLoader = eventsStatisticLoader;
        this.shiftClosureStatisticsBuilder = shiftClosureStatisticsBuilder;
    }

    public static class Columns {
        static final Column SHIFT_END_DATE_TIME = new Column(0, ShiftEventDao.Properties.ShiftEndDate);
        static final Column OPERATION_DATE_TIME = new Column(1, ShiftEventDao.Properties.ShiftOperationDate);
        static final Column STATUS = new Column(2, ShiftEventDao.Properties.ShiftStatus);
        static final Column PAPER_COUNTER_HAS_RESTARTED = new Column(3, ShiftEventDao.Properties.IsPaperCounterRestarted);
        static final Column SHIFT_ID = new Column(4, ShiftEventDao.Properties.ShiftId);
        static final Column CASH_REGISTER_EVENT_ID = new Column(5, ShiftEventDao.Properties.CashRegisterEventId);

        public static Column[] all = new Column[]{
                SHIFT_END_DATE_TIME,
                OPERATION_DATE_TIME,
                STATUS,
                PAPER_COUNTER_HAS_RESTARTED,
                SHIFT_ID,
                CASH_REGISTER_EVENT_ID
        };
    }

    public CashRegisterWorkingShift load(Cursor cursor, Offset offset) {

        CashRegisterWorkingShift cashRegisterWorkingShift = new CashRegisterWorkingShift();

        int index = offset.value + Columns.SHIFT_END_DATE_TIME.index;
        cashRegisterWorkingShift.shiftEndDateTime = cursor.isNull(index) ? null : new Date(cursor.getLong(index));
        cashRegisterWorkingShift.operationDateTime = new Date(cursor.getLong(offset.value + Columns.OPERATION_DATE_TIME.index));
        cashRegisterWorkingShift.status = cursor.getInt(offset.value + Columns.STATUS.index);
        cashRegisterWorkingShift.paperCounterHasRestarted = cursor.getInt(offset.value + Columns.PAPER_COUNTER_HAS_RESTARTED.index) == 1;
        String shiftId = cursor.getString(offset.value + Columns.SHIFT_ID.index);
        long cashRegisterEventId = cursor.getLong(offset.value + Columns.CASH_REGISTER_EVENT_ID.index);

        offset.value += Columns.all.length;

        cashRegisterWorkingShift.WorkingShift = workingShiftEventLoader.load(cursor, offset);

        eventLoader.fill(cashRegisterWorkingShift, cursor, offset);

        //заполним мелкие модельки отдельными запросами
        cashRegisterEventLoader.fill(cashRegisterWorkingShift, cashRegisterEventId);

        ShiftEvent.Status status = ShiftEvent.Status.valueOf(cashRegisterWorkingShift.status);

        cashRegisterWorkingShift.eventsStatistic = eventsStatisticLoader.load(shiftId, status == ShiftEvent.Status.ENDED);
        if (status == ShiftEvent.Status.ENDED) { //событие закрытия смены
            cashRegisterWorkingShift.shiftClosureStatistics = shiftClosureStatisticsBuilder.build(shiftId, cashRegisterWorkingShift.eventsStatistic);
        } else {
            cashRegisterWorkingShift.shiftClosureStatistics = null;
        }

        return cashRegisterWorkingShift;
    }


}
