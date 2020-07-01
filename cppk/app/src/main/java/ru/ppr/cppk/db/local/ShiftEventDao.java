package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.SqlQueryBuilder;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.utils.DateUtils;
import ru.ppr.database.Database;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.garbage.base.GCCascadeLinksRemovable;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * Класс для работы с событиями смены
 * <p>
 * <p>
 * Вставка события происходит в рамках транзакции, поэтому если при добавления
 * события возникает исключение, то бд откатывается к исходному
 * состояния, которое было на моменат начала добавления события в бд
 * <p>
 * Created by Артем on 15.12.2015.
 */
public class ShiftEventDao extends BaseEntityDao<ShiftEvent, Long> implements GCCascadeLinksRemovable {

    private static final String TAG = Logger.makeLogTag(ShiftEventDao.class);

    public static final String TABLE_NAME = "CashRegisterWorkingShift";

    public static class Properties {
        public static final String EventId = "EventId";
        public static final String ShiftId = "ShiftId";
        public static final String CashRegisterEventId = "CashRegisterEventId";
        public static final String ShiftStartDateTime = "ShiftStartDateTime";
        public static final String ShiftEndDate = "ShiftEndDateTime";
        public static final String ShiftOperationDate = "OperationDateTime";
        public static final String ShiftStatus = "ShiftStatus";
        public static final String Number = "Number";
        public static final String MonthEventId = "MonthEventId";
        public static final String PaperConsumption = "PaperConsumption";
        public static final String IsPaperCounterRestarted = "IsPaperCounterRestarted";
        public static final String CashInFR = "CashInFR";
        public static final String CheckId = "CheckId";
        public static final String ProgressStatus = "ProgressStatus";
        public static final String DeletedMark = "DeletedMark";
    }

    public ShiftEventDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.CashRegisterEventId, CashRegisterEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.MonthEventId, MonthEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.CheckId, CheckDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ShiftEvent fromCursor(Cursor cursor) {
        ShiftEvent shiftEvent = new ShiftEvent();
        shiftEvent.setId((cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id))));
        shiftEvent.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        shiftEvent.setShiftId(cursor.getString(cursor.getColumnIndex(Properties.ShiftId)));
        shiftEvent.setStartTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.ShiftStartDateTime))));
        shiftEvent.setOperationTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.ShiftOperationDate))));
        if (!cursor.isNull(cursor.getColumnIndex(Properties.ShiftEndDate)))
            shiftEvent.setCloseTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.ShiftEndDate))));
        shiftEvent.setStatus(ShiftEvent.Status.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.ShiftStatus))));
        shiftEvent.setShiftNumber(cursor.getInt(cursor.getColumnIndex(Properties.Number)));
        shiftEvent.setMonthEventId(cursor.getLong(cursor.getColumnIndex(Properties.MonthEventId)));
        shiftEvent.setCashRegisterEventId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterEventId)));
        shiftEvent.setShiftId(cursor.getString(cursor.getColumnIndex(Properties.ShiftId)));
        shiftEvent.setPaperConsumption(cursor.getLong(cursor.getColumnIndex(Properties.PaperConsumption)));
        shiftEvent.setPaperCounterRestarted(cursor.getInt(cursor.getColumnIndex(Properties.IsPaperCounterRestarted)) == 1);
        int cashInFRColumnIndex = cursor.getColumnIndex(Properties.CashInFR);
        if (!cursor.isNull(cashInFRColumnIndex)) {
            shiftEvent.setCashInFR(new BigDecimal(cursor.getString(cashInFRColumnIndex)));
        }
        int checkIdIndex = cursor.getColumnIndex(Properties.CheckId);
        if (!cursor.isNull(checkIdIndex)) {
            shiftEvent.setCheckId(cursor.getLong(checkIdIndex));
        }
        shiftEvent.setProgressStatus(ShiftEvent.ShiftProgressStatus.fromCode(cursor.getInt(cursor.getColumnIndex(Properties.ProgressStatus))));
        shiftEvent.setDeletedMark(cursor.getInt(cursor.getColumnIndex(Properties.DeletedMark)) > 0);
        return shiftEvent;
    }

    @Override
    public ContentValues toContentValues(ShiftEvent entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.ShiftId, entity.getShiftId());
        contentValues.put(Properties.MonthEventId, entity.getMonthEventId());
        contentValues.put(Properties.CashRegisterEventId, entity.getCashRegisterEventId());
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.Number, entity.getShiftNumber());
        contentValues.put(Properties.ShiftStartDateTime, entity.getStartTime().getTime());
        contentValues.put(Properties.ShiftOperationDate, entity.getOperationTime().getTime());
        contentValues.put(Properties.ShiftStatus, entity.getStatus().getCode());
        contentValues.put(Properties.ShiftId, entity.getShiftId());
        contentValues.put(Properties.PaperConsumption, entity.getPaperConsumption());
        contentValues.put(Properties.IsPaperCounterRestarted, entity.isPaperCounterRestarted());
        contentValues.put(Properties.CashInFR, entity.getCashInFR() == null ? null : entity.getCashInFR().toString());
        contentValues.put(Properties.ShiftEndDate, entity.getCloseTime() == null ? null : entity.getCloseTime().getTime());
        contentValues.put(Properties.CheckId, entity.getCheckId());
        contentValues.put(Properties.ProgressStatus, entity.getProgressStatus().getCode());
        contentValues.put(Properties.DeletedMark, entity.getDeletedMark());
        return contentValues;
    }

    @Override
    public Long getKey(ShiftEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull ShiftEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    /**
     * Возвращает набор событий для смен за период с указанным статусом
     *
     * @return
     */
    public List<ShiftEvent> getShiftsForExport(Date fromTimestamp) {
        List<ShiftEvent> list = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");
        stringBuilder.append(" * ");
        stringBuilder.append(" FROM ");
        stringBuilder.append(getTableName());
        stringBuilder.append(" WHERE ")
                .append(" ( ")
                .append(Properties.ProgressStatus).append(" = ").append(ShiftEvent.ShiftProgressStatus.CHECK_PRINTED.getCode())
                .append(" OR ")
                .append(Properties.ProgressStatus).append(" = ").append(ShiftEvent.ShiftProgressStatus.COMPLETED.getCode())
                .append(" ) ");
        if (fromTimestamp != null) {
            stringBuilder.append(" AND ").append(Properties.EventId).append(" in ");
            stringBuilder.append("(");
            stringBuilder.append(" SELECT ").append(BaseEntityDao.Properties.Id).append(" FROM ").append(EventDao.TABLE_NAME);
            stringBuilder.append(" WHERE ").append(EventDao.Properties.CreationTimestamp).append(" > ").append(fromTimestamp.getTime());
            stringBuilder.append(")");
        }
        stringBuilder.append(" ORDER BY ").append(Properties.ShiftOperationDate);

        Cursor cursor = null;

        try {
            cursor = db().rawQuery(stringBuilder.toString(), null);
            while (cursor.moveToNext()) {
                ShiftEvent shiftEvent = fromCursor(cursor);
                list.add(shiftEvent);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return list;
    }

    /**
     * Возвращает список смен на дату
     *
     * @return
     */
    public List<ShiftEvent> getShiftsAtDate(Date timestamp) {

        List<ShiftEvent> list = new ArrayList<>();

        long startOfDay = DateUtils.getStartOfDay(timestamp).getTimeInMillis();
        long endOfDay = DateUtils.getEndOfDay(timestamp).getTimeInMillis();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");
        stringBuilder.append(getTableName()).append(".*");
        stringBuilder.append(" FROM ");
        stringBuilder.append(getTableName());
        stringBuilder.append(" WHERE ")
                .append(" ( ")
                .append(Properties.ProgressStatus).append(" = ").append(ShiftEvent.ShiftProgressStatus.CHECK_PRINTED.getCode())
                .append(" OR ")
                .append(Properties.ProgressStatus).append(" = ").append(ShiftEvent.ShiftProgressStatus.COMPLETED.getCode())
                .append(" ) ");
        stringBuilder.append(" AND (");
        stringBuilder.append(Properties.ShiftStatus).append(" = ").append(ShiftEvent.Status.STARTED.getCode());
        stringBuilder.append(" AND ");
        stringBuilder.append(Properties.ShiftStartDateTime).append(" >= ").append(startOfDay);
        stringBuilder.append(" AND ");
        stringBuilder.append(Properties.ShiftStartDateTime).append(" < ").append(endOfDay);
        stringBuilder.append(")");
        stringBuilder.append(" ORDER BY ").append(getIdWithTableName());

        Logger.trace(ShiftEventDao.class, stringBuilder.toString());

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), null);
            while (cursor.moveToNext()) {
                ShiftEvent shiftEvent = fromCursor(cursor);
                list.add(shiftEvent);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }


        return list;
    }

    /**
     * Возвращает событие смены по ID
     *
     * @return
     */
    @Nullable
    public ShiftEvent load(long id) {

        String selection = BaseEntityDao.Properties.Id + " = " + id;
        ShiftEvent shiftEvent = null;
        Cursor cursor = null;
        try {
            cursor = db().query(ShiftEventDao.TABLE_NAME,
                    null,
                    selection,
                    null,
                    null,
                    null,
                    null);
            if (cursor.moveToFirst()) {
                shiftEvent = fromCursor(cursor);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return shiftEvent;

    }

    /**
     * Возвращает послднее событие для смены с shiftId
     *
     * @return
     */
    @Nullable
    public ShiftEvent getLastCashRegisterWorkingShiftByShiftId(String shiftId, @Nullable EnumSet<ShiftEvent.ShiftProgressStatus> progressStatuses) {

        List<String> selectionArgs = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(" * ");
        stringBuilder.append(" FROM ");
        stringBuilder.append(ShiftEventDao.TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(Properties.ShiftId).append(" = ").append("?");
        selectionArgs.add(String.valueOf(shiftId));
        ////////////////////////////////////////////////////////////////////////////////
        if (progressStatuses != null) {
            stringBuilder.append(" AND ");
            stringBuilder.append(TABLE_NAME).append(".").append(Properties.ProgressStatus);
            stringBuilder.append(" IN ");
            stringBuilder.append(" ( ");
            stringBuilder.append(SqLiteUtils.makePlaceholders(progressStatuses.size()));
            for (ShiftEvent.ShiftProgressStatus progressStatus : progressStatuses) {
                selectionArgs.add(String.valueOf(progressStatus.getCode()));
            }
            stringBuilder.append(" ) ");
        }
        ////////////////////////////////////////////////////////////////////////////////
        stringBuilder.append(" ORDER BY ").append(TABLE_NAME + "." + BaseEntityDao.Properties.Id).append(" DESC");
        stringBuilder.append(" LIMIT 1");

        String[] selectionArgsArray = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArray);

        ShiftEvent shiftEvent = null;
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgsArray);
            if (cursor.moveToFirst()) {
                shiftEvent = fromCursor(cursor);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return shiftEvent;
    }

    /**
     * Возвращает первое событие для смены с shiftId
     *
     * @return
     */
    @Nullable
    public ShiftEvent getFirstShiftEventByShiftId(String shiftId, @Nullable EnumSet<ShiftEvent.ShiftProgressStatus> progressStatuses) {

        List<String> selectionArgs = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(" * ");
        stringBuilder.append(" FROM ");
        stringBuilder.append(TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(Properties.ShiftId).append(" = ").append("?");
        selectionArgs.add(String.valueOf(shiftId));
        stringBuilder.append(" AND ");
        stringBuilder.append(Properties.ShiftStatus).append(" = ").append(ShiftEvent.Status.STARTED.getCode());
        ////////////////////////////////////////////////////////////////////////////////
        if (progressStatuses != null) {
            stringBuilder.append(" AND ");
            stringBuilder.append(TABLE_NAME).append(".").append(Properties.ProgressStatus);
            stringBuilder.append(" IN ");
            stringBuilder.append(" ( ");
            stringBuilder.append(SqLiteUtils.makePlaceholders(progressStatuses.size()));
            for (ShiftEvent.ShiftProgressStatus progressStatus : progressStatuses) {
                selectionArgs.add(String.valueOf(progressStatus.getCode()));
            }
            stringBuilder.append(" ) ");
        }
        ////////////////////////////////////////////////////////////////////////////////
        String[] selectionArgsArray = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArray);

        ShiftEvent shiftEvent = null;
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgsArray);
            if (cursor.moveToFirst()) {
                shiftEvent = fromCursor(cursor);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return shiftEvent;
    }

    /**
     * Выполлянет поиск последнего сменного события с любым статусом
     *
     * @return Событие смены
     */
    @Nullable
    public ShiftEvent getLastShiftEvent(@Nullable EnumSet<ShiftEvent.ShiftProgressStatus> progressStatuses) {
        return getShiftByParams(null, false, null, progressStatuses);
    }

    /**
     * Выполлянет поиск первого сменного события
     *
     * @param shiftStatuses Типы запрашиваемых событий
     * @return Событие смены
     */
    @Nullable
    public ShiftEvent getFirstShiftEvent(@Nullable EnumSet<ShiftEvent.Status> shiftStatuses, @Nullable EnumSet<ShiftEvent.ShiftProgressStatus> progressStatuses) {
        return getShiftByParams(null, true, shiftStatuses, progressStatuses);
    }

    /**
     * Выполлянет поиск последнего сменного события
     *
     * @param shiftStatuses Типы запрашиваемых событий
     * @return Событие смены
     */
    @Nullable
    public ShiftEvent getLastShiftEvent(@Nullable EnumSet<ShiftEvent.Status> shiftStatuses, @Nullable EnumSet<ShiftEvent.ShiftProgressStatus> progressStatuses) {
        return getShiftByParams(null, false, shiftStatuses, progressStatuses);
    }

    /**
     * Выполлянет поиск первого сменного события в месяце
     *
     * @param shiftStatuses Типы запрашиваемых событий
     * @return Событие смены
     */
    @Nullable
    public ShiftEvent getFirstShiftEventForMonth(@NonNull String monthUid, @Nullable EnumSet<ShiftEvent.Status> shiftStatuses, @Nullable EnumSet<ShiftEvent.ShiftProgressStatus> progressStatuses) {
        return getShiftByParams(monthUid, true, shiftStatuses, progressStatuses);
    }

    /**
     * Выполлянет поиск последнего сменного события в месяце
     *
     * @param shiftStatuses Типы запрашиваемых событий
     * @return Событие смены
     */
    @Nullable
    public ShiftEvent getLastShiftEventForMonth(@NonNull String monthUid, @Nullable EnumSet<ShiftEvent.Status> shiftStatuses, @Nullable EnumSet<ShiftEvent.ShiftProgressStatus> progressStatuses) {
        return getShiftByParams(monthUid, false, shiftStatuses, progressStatuses);
    }

    /**
     * Выполлянет поиск сменного события по указанным параметрам.
     *
     * @param monthUid      UUID месяца
     * @param firstEvent    {@code true} - первое событие, {@code false} - последнее событие
     * @param shiftStatuses Типы запрашиваемых событий
     * @return Событие смены, удовлетворяющее условиям
     */
    @Nullable
    private ShiftEvent getShiftByParams(@Nullable String monthUid, boolean firstEvent, @Nullable EnumSet<ShiftEvent.Status> shiftStatuses, @Nullable EnumSet<ShiftEvent.ShiftProgressStatus> progressStatuses) {

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TABLE_NAME);
        if (monthUid != null) {
            builder.append(" JOIN ");
            builder.append(MonthEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TABLE_NAME).append(".").append(Properties.MonthEventId);
            builder.append(" = ");
            builder.append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            builder.append(" AND ");
            builder.append(MonthEventDao.Properties.MonthId).append(" = ").append("?");
            selectionArgsList.add(monthUid);
        }
        builder.append(" WHERE 1=1 ");
        ////////////////////////////////////////////////////////////////////////////////
        if (shiftStatuses != null) {
            builder.append(" AND ");
            builder.append(TABLE_NAME).append(".").append(Properties.ShiftStatus);
            builder.append(" IN ");
            builder.append(" ( ");
            builder.append(SqLiteUtils.makePlaceholders(shiftStatuses.size()));
            for (ShiftEvent.Status shiftStatus : shiftStatuses) {
                selectionArgsList.add(String.valueOf(shiftStatus.getCode()));
            }
            builder.append(" ) ");
        }
        ////////////////////////////////////////////////////////////////////////////////
        if (progressStatuses != null) {
            builder.append(" AND ");
            builder.append(TABLE_NAME).append(".").append(Properties.ProgressStatus);
            builder.append(" IN ");
            builder.append(" ( ");
            builder.append(SqLiteUtils.makePlaceholders(progressStatuses.size()));
            for (ShiftEvent.ShiftProgressStatus progressStatus : progressStatuses) {
                selectionArgsList.add(String.valueOf(progressStatus.getCode()));
            }
            builder.append(" ) ");
        }
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY ").append(TABLE_NAME + "." + BaseEntityDao.Properties.Id).append(firstEvent ? " ASC " : " DESC ");
        builder.append(" LIMIT 1");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        ShiftEvent shiftEvent = null;
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);

            if (cursor.moveToFirst()) {
                shiftEvent = fromCursor(cursor);
            }

        } finally {
            if (cursor != null)
                cursor.close();
        }
        return shiftEvent;
    }

    /**
     * Вернет все события по определенной смене
     *
     * @param shiftId - UUID смены
     * @return
     */
    public List<ShiftEvent> getAllEventsForShift(String shiftId, @Nullable EnumSet<ShiftEvent.ShiftProgressStatus> progressStatuses) {

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(ShiftEventDao.TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(ShiftEventDao.TABLE_NAME);
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
//        builder.append(Properties.ShiftStatus).append(" = ").append(ShiftStatus.STARTED.getCode());
//        builder.append(" AND ");
        builder.append(Properties.ShiftId).append(" = ").append("?");
        selectionArgsList.add(shiftId);
        ////////////////////////////////////////////////////////////////////////////////
        if (progressStatuses != null) {
            builder.append(" AND ");
            builder.append(TABLE_NAME).append(".").append(Properties.ProgressStatus);
            builder.append(" IN ");
            builder.append(" ( ");
            builder.append(SqLiteUtils.makePlaceholders(progressStatuses.size()));
            for (ShiftEvent.ShiftProgressStatus progressStatus : progressStatuses) {
                selectionArgsList.add(String.valueOf(progressStatus.getCode()));
            }
            builder.append(" ) ");
        }
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY ").append(TABLE_NAME + "." + BaseEntityDao.Properties.Id).append(" ASC ");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        List<ShiftEvent> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                ShiftEvent item = fromCursor(cursor);
                list.add(item);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return list;
    }

    /**
     * Вернет все сменные события открытия смен с опредеенным номером
     *
     * @param shiftNumber = номер смены
     * @return
     */
    public List<ShiftEvent> getAllShiftsByNumber(int shiftNumber, @Nullable EnumSet<ShiftEvent.ShiftProgressStatus> progressStatuses) {

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(ShiftEventDao.TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(ShiftEventDao.TABLE_NAME);
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(Properties.ShiftStatus).append(" = ").append(ShiftEvent.Status.STARTED.getCode());
        builder.append(" AND ");
        builder.append(Properties.Number).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(shiftNumber));
        ////////////////////////////////////////////////////////////////////////////////
        if (progressStatuses != null) {
            builder.append(" AND ");
            builder.append(TABLE_NAME).append(".").append(Properties.ProgressStatus);
            builder.append(" IN ");
            builder.append(" ( ");
            builder.append(SqLiteUtils.makePlaceholders(progressStatuses.size()));
            for (ShiftEvent.ShiftProgressStatus progressStatus : progressStatuses) {
                selectionArgsList.add(String.valueOf(progressStatus.getCode()));
            }
            builder.append(" ) ");
        }
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY ").append(TABLE_NAME + "." + BaseEntityDao.Properties.Id).append(" ASC ");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        List<ShiftEvent> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                ShiftEvent item = fromCursor(cursor);
                list.add(item);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return list;
    }

    /**
     * Вернет количество сменных событий за период
     *
     * @param shiftId
     * @return
     */
    public int getShiftEventsCountByPeriod(@NonNull String shiftId) {

        /*
         * формируем запрос
         *
         * SELECT count() FROM CashRegisterWorkingShift * WHERE CashRegisterWorkingShift.ShiftId = ?
         *
         */

        SqlQueryBuilder queryBuilder = SqlQueryBuilder.newBuilder();
        queryBuilder.select("count()").from(TABLE_NAME)
                .whereEquals(Properties.ShiftId, shiftId)
                .and(Properties.ProgressStatus + " = " + ShiftEvent.ShiftProgressStatus.COMPLETED.getCode()
                        + " OR " + Properties.ProgressStatus + " = " + ShiftEvent.ShiftProgressStatus.CHECK_PRINTED.getCode());
        String query = queryBuilder.buildQuery();

        int count = 0;
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(query, null);
            if (cursor.moveToNext()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return count;
    }

    /**
     * Возвращает timestamp создания последнего события печати тестового ПД
     *
     * @return
     */
    public long getLastShiftEventCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * формируем запрос
         *
         * SELECT max(CreationTimestamp) FROM CashRegisterWorkingShift
         * JOIN Event ON CashRegisterWorkingShift.EventId = Event._id
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(EventDao.Properties.CreationTimestamp).append(") FROM ").append(ShiftEventDao.TABLE_NAME);
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
    public String getDeletedMarkField() {
        return Properties.DeletedMark;
    }

    @Override
    public boolean gcHandleRemoveCascadeLink(Database database, String referenceTable, String referenceField) {
        // оставляем стандартную реализацию сборщика мусора
        return false;
    }

}
