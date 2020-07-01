package ru.ppr.cppk.sync.loader.model.local;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.sync.kpp.ServiceSale;
import ru.ppr.cppk.sync.kpp.baseEntities.CashRegisterEvent;
import ru.ppr.cppk.sync.kpp.model.WorkingShift;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.utils.cache.Cache;
import ru.ppr.utils.cache.LruCache;

/**
 * @author Aleksandr Brazhkin
 */
public class WorkingShiftEventLoader extends BaseLoader {

    private final Cache<Long, WorkingShift> cache = new LruCache<>(20);
    private int putToCacheCount = 0;
    private int getFromCacheCount = 0;
    private final String loadWorkingShiftQuery;


    public WorkingShiftEventLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadWorkingShiftQuery = buildLoadWorkingShiftQuery();
    }

    public static class Columns {
        static final Column SHIFT_START_DATE_TIME = new Column(0, ShiftEventDao.Properties.ShiftStartDateTime);
        static final Column NUMBER = new Column(1, ShiftEventDao.Properties.Number);

        public static Column[] all = new Column[]{
                SHIFT_START_DATE_TIME,
                NUMBER
        };
    }

    public static class ServiceSaleColumns {
        static final Column NUMBER = new Column(0, ShiftEventDao.Properties.Number);

        public static Column[] all = new Column[]{
                NUMBER
        };
    }

    /**
     * Заполнить поля в сущности {@link ServiceSale}
     *
     * @param serviceSale
     * @param cursor
     * @param offset
     */
    public void fillServiceSaleFields(ServiceSale serviceSale, Cursor cursor, Offset offset) {
        serviceSale.workingShiftNumber = cursor.getInt(offset.value + ServiceSaleColumns.NUMBER.index);
        offset.value += ServiceSaleColumns.all.length;
    }

    public WorkingShift load(Cursor cursor, Offset offset) {
        WorkingShift workingShift = new WorkingShift();
        long startDateTimeLong = cursor.getLong(offset.value + Columns.SHIFT_START_DATE_TIME.index);
        workingShift.StartDateTime = new Date(startDateTimeLong);
        workingShift.Number = cursor.getInt(offset.value + Columns.NUMBER.index);
        offset.value += Columns.all.length;
        return workingShift;
    }

    private void fillFromOther(CashRegisterEvent cashRegisterEvent, WorkingShift workingShift) {
        cashRegisterEvent.WorkingShift = workingShift;
    }

    public void fill(CashRegisterEvent cashRegisterEvent, long workingShiftId) {

        if (workingShiftId <= 0) {
            fillFromOther(cashRegisterEvent, null);
        }

        WorkingShift workingShift = cache.get(workingShiftId);
        if (workingShift != null) {
            fillFromOther(cashRegisterEvent, workingShift);
            getFromCacheCount++;
            return;
        }

        String[] selectionArgs = new String[]{String.valueOf(workingShiftId)};

        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadWorkingShiftQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                WorkingShift loadedWorkingShift = load(cursor, new Offset());
                cache.put(workingShiftId, loadedWorkingShift);
                putToCacheCount++;
                fillFromOther(cashRegisterEvent, loadedWorkingShift);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public WorkingShift load(long workingShiftId) {

        WorkingShift loadedWorkingShift = cache.get(workingShiftId);
        if (loadedWorkingShift != null) {
            getFromCacheCount++;
            return loadedWorkingShift;
        }

        String[] selectionArgs = new String[]{String.valueOf(workingShiftId)};

        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadWorkingShiftQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                loadedWorkingShift = load(cursor, new Offset());
                cache.put(workingShiftId, loadedWorkingShift);
                putToCacheCount++;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return loadedWorkingShift;
    }

    public int getGetFromCacheCount() {
        return getFromCacheCount;
    }

    public int getPutToCacheCount() {
        return putToCacheCount;
    }

    private String buildLoadWorkingShiftQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(createColumnsForSelect(ShiftEventDao.TABLE_NAME, Columns.all));
        sb.append(" FROM ");
        sb.append(ShiftEventDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}
