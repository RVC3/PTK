package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.MonthEventDao;
import ru.ppr.cppk.sync.kpp.MonthClosure;
import ru.ppr.cppk.sync.kpp.model.WorkingShift;
import ru.ppr.cppk.sync.loader.CashRegisterEventData.MonthClosureStatisticBuilder;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class MonthClosureLoader extends BaseLoader {

    private final EventLoader eventLoader;
    private final CashRegisterEventLoader cashRegisterEventLoader;
    private final WorkingShiftEventLoader workingShiftEventLoader;
    private final MonthClosureStatisticBuilder monthClosureStatisticBuilder;

    public MonthClosureLoader(LocalDaoSession localDaoSession,
                              NsiDaoSession nsiDaoSession,
                              EventLoader eventLoader,
                              CashRegisterEventLoader cashRegisterEventLoader,
                              WorkingShiftEventLoader workingShiftEventLoader,
                              MonthClosureStatisticBuilder monthClosureStatisticBuilder) {
        super(localDaoSession, nsiDaoSession);
        this.eventLoader = eventLoader;
        this.cashRegisterEventLoader = cashRegisterEventLoader;
        this.workingShiftEventLoader = workingShiftEventLoader;
        this.monthClosureStatisticBuilder = monthClosureStatisticBuilder;
    }

    public static class Columns {
        static final Column MONTH_END_DATE_TIME = new Column(0, MonthEventDao.Properties.EndTimestamp);
        static final Column NUMBER = new Column(1, MonthEventDao.Properties.Number);
        static final Column MONTH_ID = new Column(2, MonthEventDao.Properties.MonthId);
        static final Column CASH_REGISTER_EVENT_ID = new Column(3, MonthEventDao.Properties.CashRegisterEventId);

        public static Column[] all = new Column[]{
                MONTH_END_DATE_TIME,
                NUMBER,
                MONTH_ID,
                CASH_REGISTER_EVENT_ID
        };
    }

    private static class WorkingShiftColumns {
        static final Column FIRST_SHIFT_EVENT_ID = new Column(0, BaseEntityDao.Properties.Id);
        static final Column LAST_SHIFT_EVENT_ID = new Column(1, BaseEntityDao.Properties.Id);

        public static Column[] all = new Column[]{
                FIRST_SHIFT_EVENT_ID,
                LAST_SHIFT_EVENT_ID
        };
    }

    public MonthClosure load(Cursor cursor, Offset offset) {

        MonthClosure monthClosure = new MonthClosure();

        monthClosure.closureDateTime = new Date(cursor.getLong(offset.value + Columns.MONTH_END_DATE_TIME.index));
        monthClosure.monthNumber = cursor.getInt(offset.value + Columns.NUMBER.index);
        String monthId = cursor.getString(offset.value + Columns.MONTH_ID.index);
        long cashRegisterEventId = cursor.getLong(offset.value + Columns.CASH_REGISTER_EVENT_ID.index);

        offset.value += Columns.all.length;

        monthClosure.WorkingShift = null; //закрытие месяца происходит вне смены

        long firstShiftEventId = cursor.getLong(offset.value + WorkingShiftColumns.FIRST_SHIFT_EVENT_ID.index);
        long lastShiftEventId = cursor.getLong(offset.value + WorkingShiftColumns.LAST_SHIFT_EVENT_ID.index);

        offset.value += WorkingShiftColumns.all.length;

        eventLoader.fill(monthClosure, cursor, offset);

        //заполним мелкие модельки отдельными запросами
        cashRegisterEventLoader.fill(monthClosure, cashRegisterEventId);

        // В будущем заполнить
        WorkingShift firstWorkingShift = workingShiftEventLoader.load(firstShiftEventId);
        WorkingShift lastWorkingShift = workingShiftEventLoader.load(lastShiftEventId);

        monthClosure.statistics = monthClosureStatisticBuilder.build(monthId, firstWorkingShift, lastWorkingShift);

        return monthClosure;
    }


}
