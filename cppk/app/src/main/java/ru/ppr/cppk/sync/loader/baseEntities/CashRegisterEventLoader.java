package ru.ppr.cppk.sync.loader.baseEntities;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CashRegisterDao;
import ru.ppr.cppk.db.local.CashRegisterEventDao;
import ru.ppr.cppk.db.local.CashierDao;
import ru.ppr.cppk.sync.kpp.baseEntities.CashRegisterEvent;
import ru.ppr.cppk.sync.kpp.model.WorkingShift;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.model.CashRegisterLoader;
import ru.ppr.cppk.sync.loader.model.CashierLoader;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.utils.cache.Cache;
import ru.ppr.utils.cache.LruCache;

/**
 * Лоадер для сущностей
 * {@link ru.ppr.cppk.sync.kpp.model.Cashier}
 * {@link ru.ppr.cppk.sync.kpp.model.CashRegister}
 * с кешированием по CashRegisterEventId
 *
 * @author Grigoriy Kashka
 */
public class CashRegisterEventLoader extends BaseLoader {

    // Таблицы, вошедшие в JOIN
    private static final String CASH_REGISTER_EVENT = "CashRegisterEventTable";
    private static final String CASH_REGISTER = "CashRegisterTable";
    private static final String CASHIER = "CashierTable";

    private final Cache<Long, CashRegisterEvent> cache = new LruCache<>(20);
    private int putToCacheCount = 0;
    private int getFromCacheCount = 0;

    private final CashierLoader cashierLoader;
    private final CashRegisterLoader cashRegisterLoader;
    private final String loadCashRegisterEventQuery;

    public CashRegisterEventLoader(LocalDaoSession localDaoSession,
                                   NsiDaoSession nsiDaoSession,
                                   CashierLoader cashierLoader,
                                   CashRegisterLoader cashRegisterLoader) {
        super(localDaoSession, nsiDaoSession);
        this.cashierLoader = cashierLoader;
        this.cashRegisterLoader = cashRegisterLoader;
        loadCashRegisterEventQuery = buildLoadCashRegisterEventQuery();
    }

    private CashRegisterEvent load(Cursor cursor, Offset offset) {
        CashRegisterEvent cashRegisterEvent = new CashRegisterEvent();
        cashRegisterEvent.CashRegister = cashRegisterLoader.load(cursor, offset);
        cashRegisterEvent.Cashier = cashierLoader.load(cursor, offset);
        return cashRegisterEvent;
    }

    private void fillFromOther(CashRegisterEvent to, CashRegisterEvent from) {
        to.Cashier = from.Cashier;
        to.CashRegister = from.CashRegister;
    }

    public void fill(CashRegisterEvent cashRegisterEvent, long cashRegisterEventId) {
        CashRegisterEvent cashRegisterEventInCache = cache.get(cashRegisterEventId);
        if (cashRegisterEventInCache != null) {
            fillFromOther(cashRegisterEvent, cashRegisterEventInCache);
            getFromCacheCount++;
            return;
        }

        String[] selectionArgs = new String[]{String.valueOf(cashRegisterEventId)};

        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadCashRegisterEventQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                CashRegisterEvent loadedCashRegisterEvent = load(cursor, new Offset());
                cache.put(cashRegisterEventId, loadedCashRegisterEvent);
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

    private String buildLoadCashRegisterEventQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(createColumnsForSelect(CASH_REGISTER, CashRegisterLoader.Columns.all)).append(", ");
        sb.append(createColumnsForSelect(CASHIER, CashierLoader.Columns.all));
        sb.append(" FROM ");
        sb.append(CashRegisterEventDao.TABLE_NAME).append(" ").append(CASH_REGISTER_EVENT);
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
        sb.append(CASH_REGISTER_EVENT).append(".").append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}
