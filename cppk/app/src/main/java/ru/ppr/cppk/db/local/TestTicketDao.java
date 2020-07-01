package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.SqlQueryBuilder;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>TestTicketEvent</i>.
 */
public class TestTicketDao extends BaseEntityDao<TestTicketEvent, Long> {

    private static final String TAG = Logger.makeLogTag(TestTicketDao.class);

    public static final String TABLE_NAME = "TestTicketEvent";

    public static class Properties {
        public static final String CashRegisterWorkingShiftId = "CashRegisterWorkingShiftId";
        public static final String CheckId = "CheckId";
        public static final String EventId = "EventId";
        public static final String TicketTapeEventId = "TicketTapeEventId";
        public static final String Status = "Status";
    }

    public TestTicketDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.CheckId, CheckDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.TicketTapeEventId, TicketTapeEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public TestTicketEvent fromCursor(Cursor cursor) {
        TestTicketEvent testTicketEvent = new TestTicketEvent();
        testTicketEvent.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        testTicketEvent.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        testTicketEvent.setShiftEventId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterWorkingShiftId)));
        int index = cursor.getColumnIndex(Properties.CheckId);
        testTicketEvent.setCheckId(cursor.isNull(index) ? 0 : cursor.getLong(index));
        testTicketEvent.setTicketTapeEventId(cursor.getLong(cursor.getColumnIndex(Properties.TicketTapeEventId)));
        testTicketEvent.setStatus(TestTicketEvent.Status.fromCode(cursor.getInt(cursor.getColumnIndex(Properties.Status))));
        return testTicketEvent;
    }

    @Override
    public ContentValues toContentValues(TestTicketEvent entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.CheckId, entity.getCheckId() > 0 ? entity.getCheckId() : null);
        contentValues.put(Properties.CashRegisterWorkingShiftId, entity.getShiftEventId());
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.TicketTapeEventId, entity.getTicketTapeEventId());
        contentValues.put(Properties.Status, entity.getStatus().getCode());
        return contentValues;
    }

    @Override
    public Long getKey(TestTicketEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull TestTicketEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    /**
     * Загружает список событий печати тестовых билетов для выгрузки, начиная с даты отсортированных по дате.
     *
     * @param timestamp время, с которого необходимо загрузить список тестовых билетов
     * @return список билетов
     * @Deprecated метод удалится, в 2.7 когда перейдем на новый механизм выгрузки
     */
    @NonNull
    public List<TestTicketEvent> loadEventsForExport(@NonNull Date timestamp) {

        // Select TestTicketEvent.* from TestTicketEvent join Event
        // ON TestTicketEvent.EventId = Event._id
        // WHERE Event.CreationTimestamp > 1465539326724 ORDER BY Event.CreationTimestamp

        final SqlQueryBuilder queryBuilder = SqlQueryBuilder.newBuilder();
        queryBuilder.select(TestTicketDao.TABLE_NAME + ".*").from(TestTicketDao.TABLE_NAME)
                .join(EventDao.TABLE_NAME).onEquals(Properties.EventId, getLocalDaoSession().getEventDao().getIdWithTableName())
                .whereLarger(EventDao.Properties.CreationTimestamp, timestamp.getTime())
                .append("AND")
                .append("(")
                .append(TestTicketDao.TABLE_NAME + "." + Properties.Status).append("=").append(String.valueOf(TestTicketEvent.Status.CHECK_PRINTED.getCode()))
                .append("OR")
                .append(TestTicketDao.TABLE_NAME + "." + Properties.Status).append("=").append(String.valueOf(TestTicketEvent.Status.COMPLETED.getCode()))
                .append(")")
                .orderBy(EventDao.Properties.CreationTimestamp);

        List<TestTicketEvent> list = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(queryBuilder.buildQuery(), null);
            while (cursor.moveToNext()) {
                TestTicketEvent event = fromCursor(cursor);
                if (event != null) {
                    list.add(event);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return list;
    }

    /**
     * Возвращает список продаж ПД за смену в обратном порядке.
     *
     * @param shiftUid UUID смены
     * @return Список продаж ПД
     */
    @NonNull
    public List<TestTicketEvent> getTestTicketEventsForShift(
            @NonNull String shiftUid,
            @Nullable EnumSet<TestTicketEvent.Status> statuses) {
        return getTestTicketEventsByParams(shiftUid, null, null, null, statuses, false, 0);
    }

    /**
     * Возвращает список продаж ПД за месяц.
     *
     * @param monthUid      UUID месяца
     * @param shiftStatuses Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @return Список продаж ПД
     */
    @NonNull
    public List<TestTicketEvent> getTestTicketEventsForMonth(
            @NonNull String monthUid,
            @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
            @Nullable EnumSet<TestTicketEvent.Status> statuses) {
        return getTestTicketEventsByParams(null, monthUid, null, shiftStatuses, statuses, false, 0);
    }

    /**
     * Возвращает количество тестовых билетов для месяца
     *
     * @param monthEvent
     * @return
     */
    public int getCountTestPdForMonth(@NonNull MonthEvent monthEvent) {

        // select count(*) from TestTicketEvent
        // JOIN CashRegisterWorkingShift ON TestTicketEvent.CashRegisterWorkingShiftId = CashRegisterWorkingShift._id
        // JOIN Months ON CashRegisterWorkingShift.MonthId = Months._id WHERE Months.Number = 1

        StringBuilder builder = new StringBuilder();
        builder.append("Select count(*) from ").append(TestTicketDao.TABLE_NAME)
                .append(" JOIN ").append(ShiftEventDao.TABLE_NAME)
                .append(" ON ").append(TestTicketDao.TABLE_NAME)
                .append(".").append(Properties.CashRegisterWorkingShiftId)
                .append(" = ").append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .append(" JOIN ").append(MonthEventDao.TABLE_NAME)
                .append(" ON ").append(ShiftEventDao.TABLE_NAME)
                .append(".").append(ShiftEventDao.Properties.MonthEventId)
                .append(" = ").append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .append(" WHERE ").append(MonthEventDao.TABLE_NAME).append(".").append(MonthEventDao.Properties.Number)
                .append(" = ").append(monthEvent.getMonthNumber())
                .append(" AND (")
                .append(TABLE_NAME).append(".").append(Properties.Status).append("=").append(TestTicketEvent.Status.COMPLETED.getCode())
                .append(" OR ")
                .append(TABLE_NAME).append(".").append(Properties.Status).append("=").append(TestTicketEvent.Status.CHECK_PRINTED.getCode())
                .append(")");

        int count = 0;

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Logger.info(TestTicketDao.class, "getCountTestPdForMonth /n " + builder.toString() + "/n result: " + count);
        return count;
    }

    /**
     * Возвращает количество напечатанных тестовых ПД начиная с начала смены, и до operationTime текущей смены
     *
     * @param shiftId  смена, для которой получаем количество напечатанных тестовых билетов
     * @param statuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @return
     */
    public int getCountTestPdForShift(@NonNull String shiftId,
                                      @Nullable EnumSet<TestTicketEvent.Status> statuses) {

        // select count() from TestTicketEvent
        // JOIN CashRegisterWorkingShift
        // on TestTicketEvent.CashRegisterWorkingShiftId = CashRegisterWorkingShift._id
        // where CashRegisterWorkingShift.ShiftId = '?'

        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder builder = new StringBuilder();

        builder.append("SELECT count() FROM ").append(TABLE_NAME);

        builder.append(" JOIN ");
        builder.append(ShiftEventDao.TABLE_NAME);
        builder.append(" ON ");
        builder.append(TestTicketDao.TABLE_NAME).append(".").append(Properties.CashRegisterWorkingShiftId);
        builder.append(" = ");
        builder.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);

        builder.append(" WHERE ");
        /////////////////////////////////////////////////
        builder.append(ShiftEventDao.Properties.ShiftId).append(" = ").append("?");
        selectionArgsList.add(shiftId);
        /////////////////////////////////////////////////
        if (statuses != null) {
            builder.append(" AND ");
            builder.append(TABLE_NAME).append(".").append(Properties.Status);
            builder.append(" IN ");
            builder.append(" ( ");
            builder.append(SqLiteUtils.makePlaceholders(statuses.size()));
            for (TestTicketEvent.Status status : statuses) {
                selectionArgsList.add(String.valueOf(status.getCode()));
            }
            builder.append(" ) ");
        }

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        int count = 0;

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    /**
     * Выполняет поиск событий печати пробного ПД по указанным параметрам.
     *
     * @param shiftId       UUID смены
     * @param monthId       UUID месяца
     * @param ticketTapeId  UUID билетной ленты
     * @param shiftStatuses Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @param statuses      Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @param orderDesc     Использовать сортировку в обратном порядке
     * @param limit         Количество записей, 0 - вернуть все записи
     * @return Список событий печати пробного ПД
     */
    @NonNull
    private List<TestTicketEvent> getTestTicketEventsByParams(@Nullable String shiftId,
                                                              @Nullable String monthId,
                                                              @Nullable String ticketTapeId,
                                                              @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                              @Nullable EnumSet<TestTicketEvent.Status> statuses,
                                                              boolean orderDesc,
                                                              int limit) {

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(TestTicketDao.TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TestTicketDao.TABLE_NAME);
        if (shiftId != null || monthId != null) {
            builder.append(" JOIN ");
            builder.append(ShiftEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TestTicketDao.TABLE_NAME).append(".").append(Properties.CashRegisterWorkingShiftId);
            builder.append(" = ");
            builder.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            if (monthId != null) {
                builder.append(" JOIN ");
                builder.append(MonthEventDao.TABLE_NAME);
                builder.append(" ON ");
                builder.append(ShiftEventDao.TABLE_NAME).append(".").append(ShiftEventDao.Properties.MonthEventId);
                builder.append(" = ");
                builder.append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            }
        }
        if (ticketTapeId != null) {
            builder.append(" JOIN ");
            builder.append(TicketTapeEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TestTicketDao.TABLE_NAME).append(".").append(Properties.TicketTapeEventId);
            builder.append(" = ");
            builder.append(getLocalDaoSession().getTicketTapeEventDao().getIdWithTableName());
        }
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE 1 = 1");
        if (shiftId != null) {
            builder.append(" AND ");
            builder.append(ShiftEventDao.Properties.ShiftId).append(" = ").append("?");
            selectionArgsList.add(shiftId);
        }
        if (monthId != null) {
            builder.append(" AND ");
            builder.append(MonthEventDao.Properties.MonthId).append(" = ").append("?");
            selectionArgsList.add(monthId);
        }
        if (ticketTapeId != null) {
            builder.append(" AND ");
            builder.append(TicketTapeEventDao.Properties.TicketTapeId).append(" = ").append("?");
            selectionArgsList.add(ticketTapeId);
        }
        if (shiftStatuses != null) {
            builder.append(" AND ");
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
            builder.append(" ) ");
        }
        if (statuses != null) {
            builder.append(" AND ");
            builder.append(TABLE_NAME).append(".").append(Properties.Status);
            builder.append(" IN ");
            builder.append(" ( ");
            builder.append(SqLiteUtils.makePlaceholders(statuses.size()));
            for (TestTicketEvent.Status status : statuses) {
                selectionArgsList.add(String.valueOf(status.getCode()));
            }
            builder.append(" ) ");
        }
        ///////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY " + TestTicketDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id).append(orderDesc ? " DESC " : " ASC ");
        if (limit > 0) {
            builder.append(" LIMIT ").append(limit);
        }

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        List<TestTicketEvent> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                TestTicketEvent item = fromCursor(cursor);
                list.add(item);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * Возвращает первое событие печати пробного ПД на смену.
     *
     * @param shiftUid UUID смены
     * @return Событие печати пробного ПД.
     */
    public TestTicketEvent getFirstTestTicketForShift(@NonNull String shiftUid, @Nullable EnumSet<TestTicketEvent.Status> statuses) {
        List<TestTicketEvent> testTicketEventList = getTestTicketEventsByParams(shiftUid, null, null, null, statuses, false, 1);
        return testTicketEventList.isEmpty() ? null : testTicketEventList.get(0);
    }

    /**
     * Возвращает последнее событие печати пробного ПД на смену.
     *
     * @param shiftUid UUID смены
     * @return Событие печати пробного ПД.
     */
    public TestTicketEvent getLastTestTicketForShift(@NonNull String shiftUid, @Nullable EnumSet<TestTicketEvent.Status> statuses) {
        List<TestTicketEvent> testTicketEventList = getTestTicketEventsByParams(shiftUid, null, null, null, statuses, true, 1);
        return testTicketEventList.isEmpty() ? null : testTicketEventList.get(0);
    }

    /**
     * Возвращает первое обытие печати пробного ПД на месяц.
     *
     * @param monthUid      UUID месяца
     * @param shiftStatuses Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @return Событие печати пробного ПД.
     */
    public TestTicketEvent getFirstTestTicketForMonth(@NonNull String monthUid,
                                                      @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                      @Nullable EnumSet<TestTicketEvent.Status> statuses) {
        List<TestTicketEvent> testTicketEventList = getTestTicketEventsByParams(null, monthUid, null, shiftStatuses, statuses, false, 1);
        return testTicketEventList.isEmpty() ? null : testTicketEventList.get(0);
    }

    /**
     * Возвращает последнее событие печати пробного ПД на месяц.
     *
     * @param monthUid      UUID месяца
     * @param shiftStatuses Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @return Событие печати пробного ПД.
     */
    public TestTicketEvent getLastTestTicketForMonth(@NonNull String monthUid,
                                                     @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                     @Nullable EnumSet<TestTicketEvent.Status> statuses) {
        List<TestTicketEvent> testTicketEventList = getTestTicketEventsByParams(null, monthUid, null, shiftStatuses, statuses, true, 1);
        return testTicketEventList.isEmpty() ? null : testTicketEventList.get(0);
    }

    /**
     * Возвращает первое событие печати пробного ПД на билетной ленте.
     *
     * @param ticketTapeId UUID билетной ленты
     * @return Событие печати пробного ПД.
     */
    public TestTicketEvent getFirstTestTicketForTicketTape(String ticketTapeId,
                                                           @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                           @Nullable EnumSet<TestTicketEvent.Status> statuses) {
        List<TestTicketEvent> testTicketEventList = getTestTicketEventsByParams(null, null, ticketTapeId, shiftStatuses, statuses, false, 1);
        return testTicketEventList.isEmpty() ? null : testTicketEventList.get(0);
    }

    /**
     * Возвращает последнее событие печати пробного ПД на билетной ленте.
     *
     * @param ticketTapeId UUID билетной ленты
     * @return Событие печати пробного ПД.
     */
    public TestTicketEvent getLastTestTicketForTicketTape(String ticketTapeId,
                                                          @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                          @Nullable EnumSet<TestTicketEvent.Status> statuses) {
        List<TestTicketEvent> testTicketEventList = getTestTicketEventsByParams(null, null, ticketTapeId, shiftStatuses, statuses, true, 1);
        return testTicketEventList.isEmpty() ? null : testTicketEventList.get(0);
    }

    /**
     * Возвращает timestamp создания последнего события печати тестового ПД
     *
     * @return
     */
    public long getLastTestTicketEventCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * формируем запрос
         *
         * SELECT max(CreationTimestamp) FROM TestTicketEvent
         * JOIN Event ON TestTicketEvent.EventId = Event._id
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(EventDao.Properties.CreationTimestamp).append(") FROM ").append(TestTicketDao.TABLE_NAME);
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
}
