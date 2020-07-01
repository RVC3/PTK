package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.base.GCCascadeLinksRemovable;
import ru.ppr.database.garbage.base.GCOldDataRemovable;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>Event</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class EventDao extends BaseEntityDao<Event, Long> implements GCOldDataRemovable, GCCascadeLinksRemovable {

    public static final String TABLE_NAME = "Event";

    public static class Properties {
        public static final String Guid = "Guid";
        public static final String CreationTimestamp = "CreationTimestamp";
        public static final String VersionId = "VersionId";
        public static final String SoftwareUpdateEventId = "SoftwareUpdateEventId";
        public static final String StationDeviceId = "StationDeviceId";
        public static final String StationCode = "StationCode";
        public static final String DeletedMark = "DeletedMark";
    }

    public EventDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.StationDeviceId, StationDeviceDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.SoftwareUpdateEventId, UpdateEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Event fromCursor(@NonNull final Cursor cursor) {
        Event event = new Event();
        event.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        event.setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(Properties.Guid))));
        event.setCreationTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.CreationTimestamp))));
        event.setVersionId(cursor.getInt(cursor.getColumnIndex(Properties.VersionId)));
        event.setSoftwareUpdateEventId(cursor.getLong(cursor.getColumnIndex(Properties.SoftwareUpdateEventId)));
        event.setDeviceId(cursor.getLong(cursor.getColumnIndex(Properties.StationDeviceId)));
        event.setStationCode(cursor.getLong(cursor.getColumnIndex(Properties.StationCode)));
        event.setDeletedMark(cursor.getInt(cursor.getColumnIndex(Properties.DeletedMark)) > 0);
        return event;
    }

    @Override
    public ContentValues toContentValues(@NonNull final Event entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.Guid, entity.getUuid().toString());
        contentValues.put(Properties.CreationTimestamp, entity.getCreationTimestammpInMillis());
        contentValues.put(Properties.VersionId, entity.getVersionId());
        contentValues.put(Properties.SoftwareUpdateEventId, entity.getSoftwareUpdateEventId());
        contentValues.put(Properties.StationDeviceId, entity.getDeviceId());
        contentValues.put(Properties.StationCode, entity.getStationCode());
        contentValues.put(Properties.DeletedMark, entity.getDeletedMark());
        return contentValues;
    }

    @Override
    public Long getKey(Event entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull Event entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Override
    public String getDeletedMarkField() {
        return Properties.DeletedMark;
    }

    @Override
    public void gcRemoveOldData(Database database, Date dateBefore) {
        // Получаем id события последнего закрытия месяца до даты dateBefore
        long lastOldEventId =  getLocalDaoSession().getMonthEventDao().getLastCloseMonthEventId(dateBefore);
        if (lastOldEventId > 0){
            // Помечаем для удаления события, id которых меньше события закрытия месяца для dateBefore
            StringBuilder sql = new StringBuilder();
            sql.append("update ").append(getTableName()).append(" set ").append(Properties.DeletedMark).append(" = 1 ")
                    .append("where " ).append(BaseEntityDao.Properties.Id).append(" <= ").append(lastOldEventId);

            Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcRemoveOldData(): execute sql" + "\n" + sql.toString());
            database.execSQL(sql.toString());
        }
    }

    @Override
    public boolean gcHandleRemoveCascadeLink(Database database, String referenceTable, String referenceField) {
        // оставляем реализацию сборщика по умолчанию
        return false;
    }

    /**
     * Возвращает время последнего события в БД
     *
     * @return
     */
    public long getLastEventTimeStamp() {
        long out = 0;
        Cursor cursor = null;
        try {

            String sql = "select " + Properties.CreationTimestamp + " from "
                    + TABLE_NAME + " ORDER BY " + Properties.CreationTimestamp
                    + " DESC LIMIT 1";

            cursor = getLocalDaoSession().getLocalDb().rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                out = cursor.getLong(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return out;
    }
}
