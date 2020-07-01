package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.base.Preconditions;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReSign;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * Created by nevolin on 06.07.2016.
 */
public class CPPKTicketReSignDao extends BaseEntityDao<CPPKTicketReSign, Long> {

    private static final String TAG = Logger.makeLogTag(CPPKTicketReSignDao.class);

    public static final String TABLE_NAME = "CPPKTicketReSign";

    public static class Properties {
        public static final String EventId = "EventId";
        public static final String TicketNumber = "TicketNumber";
        public static final String SaleDateTime = "SaleDateTime";
        public static final String TicketDeviceId = "TicketDeviceId";
        public static final String EDSKeyNumber = "EDSKeyNumber";
        public static final String ReSignDateTime = "ReSignDateTime";
    }

    public CPPKTicketReSignDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public CPPKTicketReSign fromCursor(Cursor cursor) {
        CPPKTicketReSign cppkTicketReSign = new CPPKTicketReSign();
        cppkTicketReSign.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        cppkTicketReSign.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        cppkTicketReSign.setTicketNumber(cursor.getInt(cursor.getColumnIndex(Properties.TicketNumber)));
        cppkTicketReSign.setSaleDateTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.SaleDateTime))));
        cppkTicketReSign.setTicketDeviceId(cursor.getString(cursor.getColumnIndex(Properties.TicketDeviceId)));
        cppkTicketReSign.setEdsKeyNumber(cursor.getLong(cursor.getColumnIndex(Properties.EDSKeyNumber)));
        cppkTicketReSign.setReSignDateTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.ReSignDateTime))));
        return cppkTicketReSign;
    }

    @Override
    public ContentValues toContentValues(CPPKTicketReSign entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.TicketNumber, entity.getTicketNumber());
        contentValues.put(Properties.SaleDateTime, entity.getSaleDateTime().getTime());
        contentValues.put(Properties.TicketDeviceId, entity.getTicketDeviceId());
        contentValues.put(Properties.EDSKeyNumber, entity.getEdsKeyNumber());
        contentValues.put(Properties.ReSignDateTime, entity.getReSignDateTime().getTime());
        return contentValues;
    }

    @Override
    public Long getKey(CPPKTicketReSign entity) {
        return entity.getId();
    }

    /**
     * Возвращает timestamp создания последнего события переподписи ПД
     *
     * @return
     */
    public long getLastTicketReSignCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * формируем запрос
         *
         * SELECT max(CreationTimestamp) FROM CPPKTicketReSign
         * JOIN Event ON CPPKTicketReSign.EventId = Event._id
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(EventDao.Properties.CreationTimestamp).append(") FROM ").append(getTableName());
        sql.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ON ").append(Properties.EventId).append(" = ").append(getLocalDaoSession().getEventDao().getIdWithTableName());

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(sql.toString(), null);
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    lastCreationTimeStamp = cursor.getLong(0);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return lastCreationTimeStamp;
    }

    @Override
    public long insertOrThrow(@NonNull CPPKTicketReSign entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

}
