package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.logic.utils.DateUtils;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.base.GCCascadeLinksRemovable;
import ru.ppr.database.garbage.base.GCOldDataRemovable;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * Dao Класс для работы с событиями месяца {@link MonthEvent}
 * <p>
 * Created by Артем on 15.12.2015.
 */
public class MonthEventDao extends BaseEntityDao<MonthEvent, Long> implements GCCascadeLinksRemovable, GCOldDataRemovable {

    private static final String TAG = Logger.makeLogTag(MonthEventDao.class);

    public static final String TABLE_NAME = "MonthEvent";

    public static class Properties {
        public static final String StartTimestamp = "StartTimestamp";
        public static final String EndTimestamp = "EndTimestamp";
        public static final String Number = "Number";
        public static final String MonthId = "MonthId";
        public static final String EventId = "EventId";
        public static final String CashRegisterEventId = "CashRegisterEventId";
        public static final String Status = "Status";
        public static final String DeletedMark = "DeletedMark";
    }

    public MonthEventDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.CashRegisterEventId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public MonthEvent fromCursor(Cursor cursor) {
        MonthEvent monthEvent = new MonthEvent();
        monthEvent.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        monthEvent.setMonthId(cursor.getString(cursor.getColumnIndex(Properties.MonthId)));
        monthEvent.setCashRegisterEventId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterEventId)));
        monthEvent.setStatus(MonthEvent.Status.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.Status))));
        monthEvent.setOpenDate(new Date(cursor.getLong(cursor.getColumnIndex(Properties.StartTimestamp))));
        monthEvent.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        monthEvent.setMonthNumber(cursor.getInt(cursor.getColumnIndex(Properties.Number)));
        int endDateIndex = cursor.getColumnIndex(Properties.EndTimestamp);
        if (!cursor.isNull(endDateIndex)) {
            monthEvent.setCloseDate(new Date(cursor.getLong(endDateIndex)));
        }
        monthEvent.setMonthId(cursor.getString(cursor.getColumnIndex(Properties.MonthId)));
        monthEvent.setDeletedMark(cursor.getInt(cursor.getColumnIndex(Properties.DeletedMark)) > 0);
        return monthEvent;
    }

    @Override
    public ContentValues toContentValues(MonthEvent entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.StartTimestamp, entity.getOpenDate().getTime());
        contentValues.put(Properties.CashRegisterEventId, entity.getCashRegisterEventId());
        contentValues.put(Properties.Number, entity.getMonthNumber());
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.MonthId, entity.getMonthId());
        contentValues.put(Properties.Status, entity.getStatus().getCode());
        contentValues.put(Properties.EndTimestamp, entity.getCloseDate() == null ? null : entity.getCloseDate().getTime());
        contentValues.put(Properties.DeletedMark, entity.getDeletedMark());
        return contentValues;
    }

    @Override
    public Long getKey(MonthEvent entity) {
        return entity.getId();
    }

    /**
     * Возвращает текущий открытый или закрытый месяц.
     * Если вернулся null, то небыло еще ни одного открытого месяца
     *
     * @return
     */
    public MonthEvent getLastMonthEvent() {

        StringBuilder builder = new StringBuilder();
        builder.append("Select * from ").append(MonthEventDao.TABLE_NAME).append(" WHERE ")
                .append(Properties.EventId).append(" = ")
                .append(" ( ")
                .append("Select ").append(BaseEntityDao.Properties.Id).append(" from ").append(EventDao.TABLE_NAME).append(" WHERE ")
                .append(BaseEntityDao.Properties.Id).append(" in ")
                .append(" ( ")
                .append(" Select ").append(Properties.EventId).append(" from ").append(MonthEventDao.TABLE_NAME)
                .append(" ) ")
                .append(" ORDER BY ").append(EventDao.Properties.CreationTimestamp)
                .append(" DESC LIMIT 1")
                .append(" ) ");

        MonthEvent event = null;
        Cursor cursor = db().rawQuery(builder.toString(), null);
        try {
            if (cursor.moveToFirst()) {
                event = fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }

        return event;
    }

    /**
     * Возвращает месяц по id
     *
     * @param id id месяца
     * @return
     */
    public MonthEvent getMonthEventById(long id) {
        MonthEvent month = null;
        String selection = BaseEntityDao.Properties.Id + " = " + id;
        Cursor cursor = db().query(MonthEventDao.TABLE_NAME, null, selection, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                month = fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }
        return month;
    }

    /**
     * Возвращает последнеее событие для месяца с monthId
     *
     * @param monthId номер месяца
     * @return
     */
    public MonthEvent getLastMonthByMonthId(String monthId) {

        MonthEvent month = null;

        List<String> selectionArgs = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(" * ");
        stringBuilder.append(" FROM ");
        stringBuilder.append(MonthEventDao.TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(Properties.MonthId).append(" = ").append("?");
        selectionArgs.add(String.valueOf(monthId));
        stringBuilder.append(" ORDER BY ").append(Properties.EndTimestamp).append(" DESC");
        stringBuilder.append(" LIMIT 1");

        String[] selectionArgsArray = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArray);
        Cursor cursor = db().rawQuery(stringBuilder.toString(), selectionArgsArray);
        try {
            if (cursor.moveToFirst()) {
                month = fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }
        return month;
    }


    /**
     * Возвращает первое событие для месяца с monthId
     *
     * @param monthId номер месяца
     * @return
     */
    public MonthEvent getFirstMonthByMonthId(String monthId) {

        MonthEvent month = null;

        List<String> selectionArgs = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(" * ");
        stringBuilder.append(" FROM ");
        stringBuilder.append(MonthEventDao.TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(Properties.MonthId).append(" = ").append("?");
        selectionArgs.add(String.valueOf(monthId));
        stringBuilder.append(" AND ");
        stringBuilder.append(Properties.Status).append(" = ").append(MonthEvent.Status.OPENED.getCode());

        String[] selectionArgsArray = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArray);
        Cursor cursor = db().rawQuery(stringBuilder.toString(), selectionArgsArray);
        try {
            if (cursor.moveToFirst()) {
                month = fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }
        return month;
    }

    public List<MonthEvent> getAllMonthsByNumber(int monthNumber) {

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(MonthEventDao.TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(MonthEventDao.TABLE_NAME);
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(Properties.Status).append(" = ").append(MonthEvent.Status.CLOSED.getCode());
        builder.append(" AND ");
        builder.append(Properties.Number).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(monthNumber));
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY ").append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id).append(" ASC ");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        List<MonthEvent> list = new ArrayList<>();
        Cursor cursor = db().rawQuery(builder.toString(), selectionArgs);
        try {
            while (cursor.moveToNext()) {
                MonthEvent item = fromCursor(cursor);
                list.add(item);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    /**
     * Возвращает события закрытия месяца с переданного времени/
     *
     * @param date дата, с которой необходимо выгрузить события
     * @return НЕИЗМЕНЯЕМЫЙ список с событиями закрытия смены, если событий нет, то список пустой
     */
    @NonNull
    public List<MonthEvent> getMonthEventClosedByTime(Date date) {

//        SELECT MonthEvent.*
//        FROM MonthEvent
//        JOIN Event ON MonthEvent.EventId = Event._id
//        WHERE Event.CreationTimestamp > 121212121
//        AND MonthEvent.EndTimestamp IS NOT NULL

        String query = "Select " + MonthEventDao.TABLE_NAME + ".* " +
                " FROM " + MonthEventDao.TABLE_NAME +
                " JOIN " + EventDao.TABLE_NAME +
                " ON " + Properties.EventId +
                " = " + getLocalDaoSession().getEventDao().getIdWithTableName() +
                " WHERE " + EventDao.Properties.CreationTimestamp + " > " + date.getTime() +
                " AND " + Properties.EndTimestamp + " IS NOT NULL" +
                " ORDER BY " + EventDao.Properties.CreationTimestamp;

        ImmutableList.Builder<MonthEvent> listBuilder = ImmutableList.builder();
        Cursor cursor = db().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                MonthEvent monthEvent = fromCursor(cursor);
                listBuilder.add(monthEvent);
            }
        } finally {
            cursor.close();
        }
        return listBuilder.build();
    }

    /**
     * Возвращает timestamp создания последнего события {@link MonthEvent}
     *
     * @return - время закрытия месяца
     */
    public long getLastMonthEventCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * SELECT max(CreationTimestamp) FROM MonthEvent
         * JOIN Event ON MonthEvent.EventId = Event._id
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(EventDao.Properties.CreationTimestamp).append(") FROM ").append(MonthEventDao.TABLE_NAME);
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

    /**
     * Возвращает дату завершения последнего закрытого месяца от даты dateBefore
     * @param dateBefore - дата, начиная с которой ищутся события
     *
     * @return - дата закрытия месяца
     */
    public Date getLastCloseMonthEventDate(Date dateBefore) {
        /*
         * SELECT max(EndTimestamp) FROM MonthEvent where EndTimestamp < dateBefore and status = 1
         *
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(Properties.EndTimestamp).append(") FROM ").append(MonthEventDao.TABLE_NAME)
                .append(" where ").append(Properties.EndTimestamp).append(" < ").append(dateBefore.getTime())
                .append(" and ").append(Properties.Status).append(" = ").append(MonthEvent.Status.CLOSED.getCode());

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(sql.toString(), null);
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0) && cursor.getLong(0) > 0) {
                    return new Date(cursor.getLong(0));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return new Date(0);
    }

    /**
     * Возвращает EventId последнего события закрытия месяца от даты dateBefore {@link MonthEvent}
     * @param dateBefore - дата, начиная с которой ищутся события
     *
     * @return - событие закрытия месяца
     */
    public long getLastCloseMonthEventId(Date dateBefore) {
        /*
         * SELECT max(EventId) FROM MonthEvent where EndTimestamp <= dateBefore and status = 1
         *
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(Properties.EventId).append(") FROM ").append(MonthEventDao.TABLE_NAME)
                .append(" where ").append(Properties.EndTimestamp).append(" < ").append(dateBefore.getTime())
                .append(" and ").append(Properties.Status).append(" = ").append(MonthEvent.Status.CLOSED.getCode());

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(sql.toString(), null);
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    return cursor.getLong(0);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    /**
     * Возвращает список месяцев за календарный месяц
     *
     * @return
     */
    public List<MonthEvent> getMonthsAtCalendarMonth(Date date) {

        List<MonthEvent> list = new ArrayList<>();

        long startOfMonth = DateUtils.getStartOfMonth(date).getTimeInMillis();
        long endOfMonth = DateUtils.getEndOfMonth(date).getTimeInMillis();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");
        stringBuilder.append(MonthEventDao.TABLE_NAME).append(".*");
        stringBuilder.append(" FROM ");
        stringBuilder.append(MonthEventDao.TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(MonthEventDao.Properties.Status).append(" = ").append(MonthEvent.Status.OPENED.getCode());
        stringBuilder.append(" AND ");
        stringBuilder.append(MonthEventDao.Properties.StartTimestamp).append(" >= ").append(startOfMonth);
        stringBuilder.append(" AND ");
        stringBuilder.append(MonthEventDao.Properties.StartTimestamp).append(" < ").append(endOfMonth);

        stringBuilder.append(" ORDER BY ").append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);

        Logger.trace(TAG, stringBuilder.toString());

        Cursor cursor = db().rawQuery(stringBuilder.toString(), null);
        try {
            while (cursor.moveToNext()) {
                MonthEvent monthEvent = fromCursor(cursor);
                list.add(monthEvent);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    @Override
    public long insertOrThrow(@NonNull MonthEvent entity) {
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
        // Помечаем записи, дата завершения месяца которых меньше dateBefore
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(getTableName()).append(" set ").append(Properties.DeletedMark).append(" = 1 ")
                .append("where ").append(Properties.EndTimestamp).append(" <= ").append(dateBefore.getTime());

        Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcRemoveOldData(): execute sql" + "\n" + sql.toString());
        database.execSQL(sql.toString());
    }

    @Override
    public boolean gcHandleRemoveCascadeLink(Database database, String referenceTable, String referenceField) {
        return false;
    }

}
