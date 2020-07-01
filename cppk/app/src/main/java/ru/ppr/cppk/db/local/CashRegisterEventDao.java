package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * Класс для работы с CashRegisterEvent
 * Created by Александр on 09.02.2016.
 */
public class CashRegisterEventDao extends BaseEntityDao<CashRegisterEvent, Long> {

    private static final String TAG = Logger.makeLogTag(CashRegisterEventDao.class);

    public static final String TABLE_NAME = "CashRegisterEvent";

    public static class Properties {
        public static final String CashierId = "CashierId";
        public static final String CashRegisterId = "CashRegisterId";
    }

    public CashRegisterEventDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.CashierId, CashierDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.CashRegisterId, CashRegisterDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public CashRegisterEvent fromCursor(Cursor cursor) {
        CashRegisterEvent out = new CashRegisterEvent();
        out.setId((cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id))));
        out.setCashRegisterId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterId)));
        out.setCashierId(cursor.getLong(cursor.getColumnIndex(Properties.CashierId)));
        return out;
    }

    @Override
    public ContentValues toContentValues(CashRegisterEvent entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.CashierId, entity.getCashierId());
        contentValues.put(Properties.CashRegisterId, entity.getCashRegisterId());
        return contentValues;
    }

    @Override
    public Long getKey(CashRegisterEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull CashRegisterEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    public CashRegisterEvent getLastCashRegisterEvent() {

        CashRegisterEvent cashRegisterEvent = null;

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(CashRegisterEventDao.TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(CashRegisterEventDao.TABLE_NAME);
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY ").append(CashRegisterEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id).append(" DESC ");
        builder.append(" LIMIT 1");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        Cursor cursor = db().rawQuery(builder.toString(), selectionArgs);
        try {
            if (cursor.moveToFirst()) {
                cashRegisterEvent = fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }

        return cashRegisterEvent;
    }

}
