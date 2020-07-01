package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.SqlQueryBuilder;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>Fee</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTapeEventDao extends BaseEntityDao<TicketTapeEvent, Long> {

    private static String TAG = Logger.makeLogTag(TicketTapeEventDao.class);

    public static final String TABLE_NAME = "TicketTapeEvent";

    public static class Properties {
        public static final String EventId = "EventId";
        public static final String Series = "Series";
        public static final String CashRegisterWorkingShiftId = "CashRegisterWorkingShiftId";
        public static final String MonthEventId = "MonthEventId";
        public static final String TicketTapeId = "TicketTapeId";
        public static final String StartTime = "StartTime";
        public static final String EndTime = "EndTime";
        public static final String CashRegisterEventId = "CashRegisterEventId";
        public static final String Number = "Number";
        public static final String ExpectedFirstDocNumber = "ExpectedFirstDocNumber";
        public static final String PaperConsumption = "PaperConsumption";
        public static final String IsPaperCounterRestarted = "IsPaperCounterRestarted";
    }

    public TicketTapeEventDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.CashRegisterEventId, CashRegisterEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.MonthEventId, MonthEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public TicketTapeEvent fromCursor(Cursor cursor) {
        TicketTapeEvent ticketTapeEvent = new TicketTapeEvent();
        ticketTapeEvent.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        ticketTapeEvent.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        ticketTapeEvent.setCashRegisterEventId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterEventId)));
        ticketTapeEvent.setTicketTapeId(cursor.getString(cursor.getColumnIndex(Properties.TicketTapeId)));
        int cashRegisterWorkingShiftIdIndex = cursor.getColumnIndex(Properties.CashRegisterWorkingShiftId);
        if (!cursor.isNull(cashRegisterWorkingShiftIdIndex)) {
            ticketTapeEvent.setShiftEventId(cursor.getLong(cashRegisterWorkingShiftIdIndex));
        }
        int monthEventIdIndex = cursor.getColumnIndex(Properties.MonthEventId);
        if (!cursor.isNull(monthEventIdIndex)) {
            ticketTapeEvent.setMonthEventId(cursor.getLong(monthEventIdIndex));
        }
        ticketTapeEvent.setStartTime(new Date(cursor.getLong((cursor.getColumnIndex(Properties.StartTime)))));
        int endTimeIndex = cursor.getColumnIndex(Properties.EndTime);
        if (!cursor.isNull(endTimeIndex)) {
            ticketTapeEvent.setEndTime(new Date(cursor.getLong(endTimeIndex)));
        }
        ticketTapeEvent.setSeries(cursor.getString(cursor.getColumnIndex(Properties.Series)));
        ticketTapeEvent.setExpectedFirstDocNumber(cursor.getInt(cursor.getColumnIndex(Properties.ExpectedFirstDocNumber)));
        ticketTapeEvent.setPaperConsumption(cursor.getLong(cursor.getColumnIndex(Properties.PaperConsumption)));
        ticketTapeEvent.setPaperCounterRestarted(cursor.getInt(cursor.getColumnIndex(Properties.IsPaperCounterRestarted)) == 1);
        ticketTapeEvent.setNumber(cursor.getInt(cursor.getColumnIndex(Properties.Number)));
        return ticketTapeEvent;
    }

    @Override
    public ContentValues toContentValues(TicketTapeEvent entity) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.CashRegisterEventId, entity.getCashRegisterEventId());
        contentValues.put(Properties.TicketTapeId, entity.getTicketTapeId());
        contentValues.put(Properties.CashRegisterWorkingShiftId, entity.getShiftEventId());
        contentValues.put(Properties.MonthEventId, entity.getMonthEventId());
        if (entity.getStartTime() != null) {
            contentValues.put(Properties.StartTime, entity.getStartTime().getTime());
        }
        if (entity.getEndTime() != null) {
            contentValues.put(Properties.EndTime, entity.getEndTime().getTime());
        }
        contentValues.put(Properties.Series, entity.getSeries());
        contentValues.put(Properties.Number, entity.getNumber());
        contentValues.put(Properties.ExpectedFirstDocNumber, entity.getExpectedFirstDocNumber());
        contentValues.put(Properties.PaperConsumption, entity.getPaperConsumption());
        contentValues.put(Properties.IsPaperCounterRestarted, entity.isPaperCounterRestarted());
        return contentValues;
    }

    @Override
    public Long getKey(TicketTapeEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull TicketTapeEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Nullable
    public TicketTapeEvent getInstalledTicketTape() {
        TicketTapeEvent ticketTapeEvent = getLastTicketTapeEvent();
        if (ticketTapeEvent != null && ticketTapeEvent.getEndTime() != null) {
            ticketTapeEvent = null;
        }
        return ticketTapeEvent;
    }

    public TicketTapeEvent getLastTicketTapeEvent() {
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TABLE_NAME);
        builder.append(" JOIN ");
        builder.append(EventDao.TABLE_NAME);
        builder.append(" ON ");
        builder.append(TABLE_NAME).append(".").append(Properties.EventId);
        builder.append(" = ");
        builder.append(getLocalDaoSession().getEventDao().getIdWithTableName());
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY " + BaseEntityDao.Properties.Id + " DESC");
        builder.append(", ");
        builder.append(EventDao.Properties.CreationTimestamp + " DESC");
        builder.append(" LIMIT 1");

        Cursor cursor = db().rawQuery(builder.toString(), null);
        try {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }


        return null;
    }

    public TicketTapeEvent getStartTicketTapeEventByTicketTapeId(String ticketTapeId) {

        List<String> selectionArgsList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TABLE_NAME);
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(Properties.TicketTapeId).append(" = ").append("?");
        selectionArgsList.add(ticketTapeId);
        builder.append(" AND ");
        builder.append(Properties.EndTime).append(" IS NULL ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY " + BaseEntityDao.Properties.Id + " ASC");
        builder.append(" LIMIT 1");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        Cursor cursor = db().rawQuery(builder.toString(), selectionArgs);
        try {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    public TicketTapeEvent getEndTicketTapeEventByTicketTapeId(String ticketTapeId) {

        List<String> selectionArgsList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TABLE_NAME);
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(Properties.TicketTapeId).append(" = ").append("?");
        selectionArgsList.add(ticketTapeId);
        builder.append(" AND ");
        builder.append(Properties.EndTime).append(" IS NOT NULL ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY " + BaseEntityDao.Properties.Id + " DESC");
        builder.append(" LIMIT 1");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        Cursor cursor = db().rawQuery(builder.toString(), selectionArgs);
        try {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public boolean isTicketTapeSet() {
        TicketTapeEvent ticketTapeEvent = getLastTicketTapeEvent();
        return ticketTapeEvent != null && ticketTapeEvent.getEndTime() == null;
    }

    /**
     * Возвращает события окончания билетной ленты за всё время.
     *
     * @return События окончания билетной ленты
     */
    @NonNull
    public List<TicketTapeEvent> getFinishedTicketTapeEvents() {
        return getFinishedTicketTapeEventsByParams(null, null, null, false);
    }

    /**
     * Возвращает события окончания билетной ленты за смену.
     *
     * @param shiftUid UUID смены
     * @return События окончания билетной ленты
     */
    @NonNull
    public List<TicketTapeEvent> getFinishedTicketTapeEventsForShift(@NonNull String shiftUid) {
        return getFinishedTicketTapeEventsByParams(shiftUid, null, null, false);
    }

    /**
     * Возвращает события окончания билетной ленты за месяц.
     *
     * @param monthUid                UUID месяца
     * @param shiftStatuses           Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @param includeOutOfShiftEvents {@code true} - учитывать события вне смен, {@code false} - иначе.
     * @return События окончания билетной ленты
     */
    @NonNull
    public List<TicketTapeEvent> getFinishedTicketTapeEventsForMonth(@NonNull String monthUid,
                                                                     @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                                     boolean includeOutOfShiftEvents) {
        return getFinishedTicketTapeEventsByParams(null, monthUid, shiftStatuses, includeOutOfShiftEvents);
    }

    /**
     * Возвращает события окончания билетной ленты по указанным параметрам.
     *
     * @param shiftId                 UUID смены
     * @param monthId                 UUID месяца
     * @param shiftStatuses           Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @param includeOutOfShiftEvents {@code true} - учитывать события вне смен, {@code false} - иначе.
     * @return События окончания билетной ленты
     */
    @NonNull
    private List<TicketTapeEvent> getFinishedTicketTapeEventsByParams(@Nullable String shiftId,
                                                                      @Nullable String monthId,
                                                                      @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                                      boolean includeOutOfShiftEvents) {

        List<String> selectionArgsList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TABLE_NAME);
        if (shiftId != null) {
            builder.append(" JOIN ");
            builder.append(ShiftEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TABLE_NAME).append(".").append(Properties.CashRegisterWorkingShiftId);
            builder.append(" = ");
            builder.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            builder.append(" AND ");
            builder.append(ShiftEventDao.Properties.ShiftId).append(" = ").append("?");
            selectionArgsList.add(shiftId);
        }
        if (monthId != null) {
            if (shiftStatuses != null) {
                builder.append(" LEFT JOIN ");
                builder.append(ShiftEventDao.TABLE_NAME);
                builder.append(" ON ");
                builder.append(TABLE_NAME).append(".").append(Properties.CashRegisterWorkingShiftId);
                builder.append(" = ");
                builder.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            }
            builder.append(" JOIN ");
            builder.append(MonthEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TABLE_NAME).append(".").append(Properties.MonthEventId);
            builder.append(" = ");
            builder.append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            builder.append(" AND ");
            builder.append(MonthEventDao.Properties.MonthId).append(" = ").append("?");
            selectionArgsList.add(monthId);
            builder.append(" AND ");
            builder.append(MonthEventDao.TABLE_NAME).append(".").append(MonthEventDao.Properties.Status).append(" = ").append(MonthEvent.Status.OPENED.getCode());
        }
        /////////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(Properties.EndTime).append(" IS NOT NULL ");
        if (monthId != null) {
            if (shiftStatuses != null) {
                builder.append(" AND ");
                builder.append(" ( ");
                {
                    builder.append(" EXISTS ");
                    builder.append(" ( ");
                    {
                        builder.append("SELECT ");
                        builder.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
                        builder.append(" FROM ");
                        builder.append(ShiftEventDao.TABLE_NAME).append(" AS ").append("SHIFTS");
                        builder.append(" WHERE ");
                        builder.append("SHIFTS").append(".").append(ShiftEventDao.Properties.ShiftId);
                        builder.append(" = ");
                        builder.append(ShiftEventDao.TABLE_NAME).append(".").append(ShiftEventDao.Properties.ShiftId);
                        builder.append(" AND ");
                        builder.append("SHIFTS").append(".").append(ShiftEventDao.Properties.ShiftStatus);
                        builder.append(" IN ");
                        builder.append(" ( ");
                        builder.append(SqLiteUtils.makePlaceholders(shiftStatuses.size()));
                        for (ShiftEvent.Status shiftStatus : shiftStatuses) {
                            selectionArgsList.add(String.valueOf(shiftStatus.getCode()));
                        }
                        builder.append(" ) ");
                    }
                    if (includeOutOfShiftEvents) {
                        builder.append(" OR ");
                        builder.append(Properties.CashRegisterWorkingShiftId).append(" IS NULL ");
                    }
                    builder.append(" ) ");
                }
                builder.append(" ) ");
            } else {
                if (!includeOutOfShiftEvents) {
                    builder.append(" AND ");
                    builder.append(Properties.CashRegisterWorkingShiftId).append(" IS NOT NULL ");
                }
            }
        }
        /////////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY ").append(getIdWithTableName()).append(" ASC");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        List<TicketTapeEvent> list = new ArrayList<>();
        Cursor cursor = db().rawQuery(builder.toString(), selectionArgs);
        try {
            while (cursor.moveToNext()) {
                TicketTapeEvent ticketTapeEvent = fromCursor(cursor);
                list.add(ticketTapeEvent);
            }
        } finally {
            cursor.close();
        }

        return list;
    }

    public long getTicketTapeLength(String ticketTapeId) {
        List<String> selectionArgsList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TABLE_NAME);
        /////////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(Properties.TicketTapeId).append(" = ").append("?");
        selectionArgsList.add(ticketTapeId);
        /////////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY ").append(getIdWithTableName()).append(" ASC");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        List<TicketTapeEvent> list = new ArrayList<>();
        Cursor cursor = db().rawQuery(builder.toString(), selectionArgs);
        try {
            while (cursor.moveToNext()) {
                TicketTapeEvent ticketTapeEvent = fromCursor(cursor);
                list.add(ticketTapeEvent);
            }
        } finally {
            cursor.close();
        }

        long length = 0;
        if (list.size() > 1) {
            // Значит, есть событие окончания билетной ленты
            length = list.get(1).getPaperConsumption();
        }

        return length;
    }

    public HashMap<ReportType, Integer> getPrintReportEventsCountForTicketTapes(List<String> ticketTapeIds) {

        String countColumnName = "count";
        HashMap<ReportType, Integer> hashMap = new HashMap<>();

        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(PrintReportEventDao.Properties.ReportType + ",");
        builder.append("COUNT(" + PrintReportEventDao.Properties.ReportType + ") AS " + countColumnName);
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(PrintReportEventDao.TABLE_NAME);
        builder.append(" JOIN ");
        builder.append(TABLE_NAME);
        builder.append(" ON ");
        builder.append(PrintReportEventDao.TABLE_NAME).append(".").append(PrintReportEventDao.Properties.TicketTapeEventId);
        builder.append(" = ");
        builder.append(getIdWithTableName());
        builder.append(" AND ");
        builder.append(Properties.TicketTapeId);
        builder.append(" IN ");
        builder.append("(").append(SqLiteUtils.makePlaceholders(ticketTapeIds.size())).append(")");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(" GROUP BY " + PrintReportEventDao.Properties.ReportType);

        String[] selectionArgs = new String[ticketTapeIds.size()];
        ticketTapeIds.toArray(selectionArgs);

        Cursor cursor = db().rawQuery(builder.toString(), selectionArgs);
        try {
            while (cursor.moveToNext()) {
                ReportType reportType = null;
                int reportTypeIndex = cursor.getColumnIndex(PrintReportEventDao.Properties.ReportType);
                if (!cursor.isNull(reportTypeIndex)) {
                    reportType = ReportType.getByCode(cursor.getInt(reportTypeIndex));
                }
                int count = cursor.getInt(cursor.getColumnIndex(countColumnName));
                hashMap.put(reportType, count);
            }
        } finally {
            cursor.close();
        }

        return hashMap;
    }

    public int getTestTicketEventsCountForTicketTapes(List<String> ticketTapeIds) {

        String countColumnName = "count";
        int count = 0;

        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append("COUNT(" + TestTicketDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id + ") AS " + countColumnName);
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TestTicketDao.TABLE_NAME);
        builder.append(" JOIN ");
        builder.append(TABLE_NAME);
        builder.append(" ON ");
        builder.append(TestTicketDao.TABLE_NAME).append(".").append(TestTicketDao.Properties.TicketTapeEventId);
        builder.append(" = ");
        builder.append(getIdWithTableName());
        builder.append(" AND ");
        builder.append(Properties.TicketTapeId);
        builder.append(" IN ");
        builder.append("(").append(SqLiteUtils.makePlaceholders(ticketTapeIds.size())).append(")");
        // /////////////////////////////////////////////////////////////////////////////

        String[] selectionArgs = new String[ticketTapeIds.size()];
        ticketTapeIds.toArray(selectionArgs);

        Cursor cursor = db().rawQuery(builder.toString(), selectionArgs);
        try {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndex(countColumnName));
            }
        } finally {
            cursor.close();
        }

        return count;
    }

    public int getCPPKTicketSalesCountForTicketTapes(List<String> ticketTapeIds) {

        String countColumnName = "count";
        int count = 0;

        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append("COUNT(" + CppkTicketSaleDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id + ") AS " + countColumnName);
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(CppkTicketSaleDao.TABLE_NAME);
        builder.append(" JOIN ");
        builder.append(TABLE_NAME);
        builder.append(" ON ");
        builder.append(CppkTicketSaleDao.TABLE_NAME).append(".").append(CppkTicketSaleDao.Properties.TicketTapeEventId);
        builder.append(" = ");
        builder.append(getLocalDaoSession().getTicketTapeEventDao().getIdWithTableName());
        builder.append(" AND ");
        builder.append(Properties.TicketTapeId);
        builder.append(" IN ");
        builder.append("(").append(SqLiteUtils.makePlaceholders(ticketTapeIds.size())).append(")");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(CppkTicketSaleDao.TABLE_NAME).append(".").append(CppkTicketSaleDao.Properties.ProgressStatus);
        builder.append(" IN ");
        builder.append(" ( ");
        builder.append(ProgressStatus.Completed.getCode()).append(",");
        builder.append(ProgressStatus.CheckPrinted.getCode());
        builder.append(" ) ");
        ///////////////////////////////////////////////////////////////////////////////

        String[] selectionArgs = new String[ticketTapeIds.size()];
        ticketTapeIds.toArray(selectionArgs);

        Cursor cursor = db().rawQuery(builder.toString(), selectionArgs);
        try {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndex(countColumnName));
            }
        } finally {
            cursor.close();
        }

        return count;
    }

    public int getCPPKTicketReturnsCountForTicketTapes(List<String> ticketTapeIds) {

        String countColumnName = "count";
        int count = 0;

        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append("COUNT(" + CppkTicketReturnDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id + ") AS " + countColumnName);
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(CppkTicketReturnDao.TABLE_NAME);
        builder.append(" JOIN ");
        builder.append(TABLE_NAME);
        builder.append(" ON ");
        builder.append(CppkTicketReturnDao.TABLE_NAME).append(".").append(CppkTicketReturnDao.Properties.TicketTapeEventId);
        builder.append(" = ");
        builder.append(getLocalDaoSession().getTicketTapeEventDao().getIdWithTableName());
        builder.append(" AND ");
        builder.append(Properties.TicketTapeId);
        builder.append(" IN ");
        builder.append("(").append(SqLiteUtils.makePlaceholders(ticketTapeIds.size())).append(")");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(CppkTicketReturnDao.TABLE_NAME).append(".").append(CppkTicketReturnDao.Properties.ProgressStatus);
        builder.append(" IN ");
        builder.append(" ( ");
        builder.append(ProgressStatus.Completed.getCode()).append(",");
        builder.append(ProgressStatus.CheckPrinted.getCode());
        builder.append(" ) ");
        ///////////////////////////////////////////////////////////////////////////////

        String[] selectionArgs = new String[ticketTapeIds.size()];
        ticketTapeIds.toArray(selectionArgs);

        Cursor cursor = db().rawQuery(builder.toString(), selectionArgs);
        try {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndex(countColumnName));
            }
        } finally {
            cursor.close();
        }

        return count;
    }

    /**
     * Возвращает события смены ленты с переданного времени/
     *
     * @param date дата, с которой необходимо выгрузить события смены ленты
     * @return НЕИЗМЕНЯЕМЫЙ список с событиями аннулирвоания, если событйи нет, то список пустой
     */
    @NonNull
    public List<TicketTapeEvent> getTicketTapeEventByTime(Date date) {

//        SELECT TicketTapeEvent.*
//        FROM TicketTapeEvent
//        JOIN Event ON TicketTapeEvent.EventId = Event._id
//        WHERE Event.CreationTimestamp > 121212121

        String query = "Select " + TABLE_NAME + ".* " +
                " FROM " + TABLE_NAME +
                " JOIN " + EventDao.TABLE_NAME +
                " ON " + Properties.EventId +
                " = " + getLocalDaoSession().getEventDao().getIdWithTableName() +
                " WHERE " + EventDao.Properties.CreationTimestamp + " > " + date.getTime() +
                " ORDER BY " + EventDao.Properties.CreationTimestamp;

        ImmutableList.Builder<TicketTapeEvent> listBuilder = ImmutableList.builder();
        Cursor cursor = db().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                TicketTapeEvent ticketTapeEvent = fromCursor(cursor);
                listBuilder.add(ticketTapeEvent);
            }
        } finally {
            cursor.close();
        }
        return listBuilder.build();
    }

    public int getTicketTapeCountForShift(@NonNull String shiftId) {

        int count = 0;

        /*
         * формируем запрос
         * select * from TicketTapeEvent join CashRegisterWorkingShift
         * on TicketTapeEvent.CashRegisterWorkingShiftId = CashRegisterWorkingShift._id
         * where CashRegisterWorkingShift.ShiftId = '5ad98a9a-e895-43ae-b5ec-1656aec37af8'
         */
        final SqlQueryBuilder sqlQueryBuilder = SqlQueryBuilder.newBuilder();
        sqlQueryBuilder.select("count()").from(TABLE_NAME)
                .join(ShiftEventDao.TABLE_NAME)
                .onEquals(Properties.CashRegisterWorkingShiftId,
                        ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .whereEquals(ShiftEventDao.Properties.ShiftId, shiftId);

        Cursor cursor = db().rawQuery(sqlQueryBuilder.buildQuery(), null);
        try {
            if (cursor.moveToNext()) {
                count = cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return count;

    }

    /**
     * Возвращает timestamp создания последнего события учета билетной ленты
     *
     * @return
     */
    public long getLastTicketPaperRollEventCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * формируем запрос
         *
         * SELECT max(CreationTimestamp) FROM TicketTapeEvent
         * JOIN Event ON TicketTapeEvent.EventId = Event._id
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(EventDao.Properties.CreationTimestamp).append(") FROM ").append(TABLE_NAME);
        sql.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ON ").append(Properties.EventId).append(" = ").append(getLocalDaoSession().getEventDao().getIdWithTableName());

        Cursor cursor = db().rawQuery(sql.toString(), null);
        try {
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    lastCreationTimeStamp = cursor.getLong(0);
                }
            }
        } finally {
            cursor.close();
        }

        return lastCreationTimeStamp;
    }

}
