package ru.ppr.cppk.sync.loader.baseEntities;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CashRegisterDao;
import ru.ppr.cppk.db.local.CashRegisterEventDao;
import ru.ppr.cppk.db.local.CashierDao;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.sync.kpp.baseEntities.CashRegisterEvent;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.model.CashRegisterLoader;
import ru.ppr.cppk.sync.loader.model.CashierLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.utils.cache.Cache;
import ru.ppr.utils.cache.LruCache;

/**
 * Лоадер для сущностей
 * {@link ru.ppr.cppk.sync.kpp.model.Cashier}
 * {@link ru.ppr.cppk.sync.kpp.model.CashRegister}
 * {@link ru.ppr.cppk.sync.kpp.model.WorkingShift}
 * с кешированием по CashRegisterWorkingShiftEventId
 *
 * @author Aleksandr Brazhkin
 */
public class CashRegisterWorkingShiftEventLoader extends BaseLoader {

    // Таблицы, вошедшие в JOIN
    private static final String CASH_REGISTER_WORKING_SHIFT = "CashRegisterWorkingShiftTable";
    private static final String CASH_REGISTER_EVENT = "CashRegisterEventTable";
    private static final String CASH_REGISTER = "CashRegisterTable";
    private static final String CASHIER = "CashierTable";

    private final Cache<Long, CashRegisterEvent> cache = new LruCache<>(20);
    private int putToCacheCount = 0;
    private int getFromCacheCount = 0;

    private final WorkingShiftEventLoader workingShiftEventLoader;
    private final CashierLoader cashierLoader;
    private final CashRegisterLoader cashRegisterLoader;
    private final String loadCashRegisterEventQuery;

    public CashRegisterWorkingShiftEventLoader(LocalDaoSession localDaoSession,
                                               NsiDaoSession nsiDaoSession,
                                               WorkingShiftEventLoader workingShiftEventLoader,
                                               CashierLoader cashierLoader,
                                               CashRegisterLoader cashRegisterLoader) {
        super(localDaoSession, nsiDaoSession);
        this.workingShiftEventLoader = workingShiftEventLoader;
        this.cashierLoader = cashierLoader;
        this.cashRegisterLoader = cashRegisterLoader;
        loadCashRegisterEventQuery = buildLoadCashRegisterWorkingShiftEventQuery();
    }

    private CashRegisterEvent load(Cursor cursor, Offset offset) {
        CashRegisterEvent cashRegisterEvent = new CashRegisterEvent();
        cashRegisterEvent.WorkingShift = workingShiftEventLoader.load(cursor, offset);
        cashRegisterEvent.CashRegister = cashRegisterLoader.load(cursor, offset);
        cashRegisterEvent.Cashier = cashierLoader.load(cursor, offset);
        return cashRegisterEvent;
    }

    private void fillFromOther(CashRegisterEvent to, CashRegisterEvent from) {
        to.WorkingShift = from.WorkingShift;
        to.Cashier = from.Cashier;
        to.CashRegister = from.CashRegister;
    }

    public void fill(CashRegisterEvent cashRegisterEvent, long workingShiftId) {
        CashRegisterEvent cashRegisterEventInCache = cache.get(workingShiftId);
        if (cashRegisterEventInCache != null) {
            fillFromOther(cashRegisterEvent, cashRegisterEventInCache);
            getFromCacheCount++;
            return;
        }

        String[] selectionArgs = new String[]{String.valueOf(workingShiftId)};

        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadCashRegisterEventQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                CashRegisterEvent loadedCashRegisterEvent = load(cursor, new Offset());
                cache.put(workingShiftId, loadedCashRegisterEvent);
                putToCacheCount++;
                fillFromOther(cashRegisterEvent, loadedCashRegisterEvent);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getGetFromCacheCount() {
        return getFromCacheCount;
    }

    public int getPutToCacheCount() {
        return putToCacheCount;
    }

    private String buildLoadCashRegisterWorkingShiftEventQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(createColumnsForSelect(CASH_REGISTER_WORKING_SHIFT, WorkingShiftEventLoader.Columns.all)).append(", ");
        sb.append(createColumnsForSelect(CASH_REGISTER, CashRegisterLoader.Columns.all)).append(", ");
        sb.append(createColumnsForSelect(CASHIER, CashierLoader.Columns.all));
        sb.append(" FROM ");
        sb.append(ShiftEventDao.TABLE_NAME).append(" ").append(CASH_REGISTER_WORKING_SHIFT);
        sb.append(" JOIN ").append(CashRegisterEventDao.TABLE_NAME).append(" ").append(CASH_REGISTER_EVENT);
        sb.append(" ON ");
        sb.append(CASH_REGISTER_WORKING_SHIFT).append(".").append(ShiftEventDao.Properties.CashRegisterEventId);
        sb.append(" = ");
        sb.append(CASH_REGISTER_EVENT).append(".").append(BaseEntityDao.Properties.Id);
        sb.append(" JOIN ").append(CashRegisterDao.TABLE_NAME).append(" ").append(CASH_REGISTER);
        sb.append(" ON ");
        sb.append(CASH_REGISTER_EVENT).append(".").append(CashRegisterEventDao.Properties.CashRegisterId);
        sb.append(" = ");
        sb.append(CASH_REGISTER).append(".").append(BaseEntityDao.Properties.Id);
        sb.append(" JOIN ").append(CashierDao.TABLE_NAME).append(" ").append(CASHIER);
        sb.append(" ON ");
        sb.append(CASH_REGISTER_EVENT).append(".").append(CashRegisterEventDao.Properties.CashierId);
        sb.append(" = ");
        sb.append(CASHIER).append(".").append(BaseEntityDao.Properties.Id);
        sb.append(" WHERE ");
        sb.append(CASH_REGISTER_WORKING_SHIFT).append(".").append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}
