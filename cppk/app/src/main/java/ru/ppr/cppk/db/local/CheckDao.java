package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.database.Database;
import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;

/**
 * DAO для таблицы локальной БД <i>CheckTable</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class CheckDao extends BaseEntityDao<Check, Long> implements GCNoLinkRemovable {

    public static final String TABLE_NAME = "CheckTable";

    public static class Properties {
        public static final String SerialNumber = "SerialNumber";
        public static final String AdditionalInfo = "AdditionalInfo";
        public static final String PrintDateTime = "PrintDateTime";
        public static final String SpndNumber = "SpndNumber";
    }

    public CheckDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Check fromCursor(@NonNull final Cursor cursor) {
        Check check = new Check();
        check.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        check.setAdditionalInfo(cursor.getString(cursor.getColumnIndex(Properties.AdditionalInfo)));
        check.setOrderNumber(cursor.getInt(cursor.getColumnIndex(Properties.SerialNumber)));
        check.setPrintDateTimeInMillis(new Date((cursor.getLong(cursor.getColumnIndex(Properties.PrintDateTime)))));
        check.setSnpdNumber(cursor.getInt(cursor.getColumnIndex(Properties.SpndNumber)));
        return check;
    }

    @Override
    public ContentValues toContentValues(@NonNull final Check entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CheckDao.Properties.SerialNumber, entity.getOrderNumber());
        contentValues.put(CheckDao.Properties.AdditionalInfo, entity.getAdditionalInfo());
        contentValues.put(CheckDao.Properties.PrintDateTime, entity.getPrintDatetime().getTime());
        contentValues.put(CheckDao.Properties.SpndNumber, entity.getSnpdNumber());
        return contentValues;
    }

    @Override
    public Long getKey(Check entity) {
        return entity.getId();
    }

    @Override
    public boolean gcHandleNoLinkRemoveData(Database database) {
        // оставляем реализацию сборщика мусора по умолчанию
        return false;
    }

    @Override
    public long insertOrThrow(@NonNull Check entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    /**
     * Возвращает последний распечатанный документ на указанную дату
     *
     * @return
     */
    public Check getLastCheckForPeriod(@Nullable Date fromDateTime, @Nullable Date toDateTime) {

        String selection = "1 = 1";
        if (fromDateTime != null) {
            selection += " AND " + Properties.PrintDateTime + " > " + fromDateTime.getTime();
        }
        if (toDateTime != null) {
            selection += " AND " + Properties.PrintDateTime + " < " + toDateTime.getTime();
        }

        Cursor cursor = null;
        Check check = null;

        try {
            cursor = db().query(TABLE_NAME,
                    null,
                    selection,
                    null,
                    null,
                    null,
                    Properties.PrintDateTime + " DESC",
                    "1");

            if (cursor.moveToFirst()) {
                check = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return check;
    }

    /**
     * @return Возвращает последний чек
     */
    public Check getLastCheck() {
        QueryBuilder qb = new QueryBuilder();

        qb.selectAll().from(CheckDao.TABLE_NAME)
                .orderBy(BaseEntityDao.Properties.Id).desc().limit(1);

        Query query = qb.build();
        Cursor cursor = null;
        Check check = null;

        try {
            cursor = query.run(db());

            if (cursor.moveToFirst()) {
                check = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return check;
    }
}
