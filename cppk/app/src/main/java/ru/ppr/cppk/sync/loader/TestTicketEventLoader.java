package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.TestTicketDao;
import ru.ppr.cppk.sync.kpp.TestTicketEvent;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterWorkingShiftEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.CheckLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class TestTicketEventLoader extends BaseLoader {

    private final CheckLoader checkLoader;
    private final EventLoader eventLoader;
    private final CashRegisterWorkingShiftEventLoader cashRegisterWorkingShiftEventLoader;

    public TestTicketEventLoader(LocalDaoSession localDaoSession,
                                 NsiDaoSession nsiDaoSession,
                                 CheckLoader checkLoader,
                                 EventLoader eventLoader,
                                 CashRegisterWorkingShiftEventLoader cashRegisterWorkingShiftEventLoader) {
        super(localDaoSession, nsiDaoSession);
        this.checkLoader = checkLoader;
        this.eventLoader = eventLoader;
        this.cashRegisterWorkingShiftEventLoader = cashRegisterWorkingShiftEventLoader;
    }

    public static class Columns {
        static final Column CASH_REGISTER_WORKING_SHIFT_ID = new Column(0, TestTicketDao.Properties.CashRegisterWorkingShiftId);

        public static Column[] all = new Column[]{
                CASH_REGISTER_WORKING_SHIFT_ID
        };
    }

    public TestTicketEvent load(Cursor cursor, Offset offset) {

        TestTicketEvent testTicketEvent = new TestTicketEvent();

        long cashRegisterWorkingShiftId = cursor.getLong(offset.value + Columns.CASH_REGISTER_WORKING_SHIFT_ID.index);

        offset.value += Columns.all.length;

        checkLoader.fillTestTicketEventFields(testTicketEvent, cursor, offset);

        eventLoader.fill(testTicketEvent, cursor, offset);
        cashRegisterWorkingShiftEventLoader.fill(testTicketEvent, cashRegisterWorkingShiftId);

        return testTicketEvent;
    }

}