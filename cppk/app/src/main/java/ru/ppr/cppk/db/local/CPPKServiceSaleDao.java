package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.SqlQueryBuilder;
import ru.ppr.cppk.entity.event.base34.CPPKServiceSale;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;
import ru.ppr.cppk.localdb.model.AuditTrailEventType;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.logic.builder.AuditTrailEventBuilder;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * Класс для работы с событиями продажи услуг
 *
 * @author Aleksandr Brazhkin
 */
public class CPPKServiceSaleDao extends BaseEntityDao<CPPKServiceSale, Long> {

    private static String TAG = Logger.makeLogTag(CPPKServiceSaleDao.class);

    public static final String TABLE_NAME = "CPPKServiceSale";

    public static class Properties {
        public static final String CheckId = "CheckId";
        public static final String EventId = "EventId";
        public static final String PriceId = "PriceId";
        public static final String TicketTapeEventId = "TicketTapeEventId";
        public static final String CashRegisterWorkingShiftId = "CashRegisterWorkingShiftId";
        public static final String ServiceFeeCode = "ServiceFeeCode";
        public static final String ServiceFeeName = "ServiceFeeName";
        public static final String SaleDateTime = "SaleDateTime";
    }

    public CPPKServiceSaleDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.CheckId, CheckDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.PriceId, PriceDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.TicketTapeEventId, TicketTapeEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public CPPKServiceSale fromCursor(Cursor cursor) {
        CPPKServiceSale cppkServiceSale = new CPPKServiceSale();
        cppkServiceSale.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        cppkServiceSale.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        cppkServiceSale.setCheckId(cursor.getLong(cursor.getColumnIndex(Properties.CheckId)));
        cppkServiceSale.setShiftEventId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterWorkingShiftId)));
        int ticketTapeEventIdIndex = cursor.getColumnIndex(Properties.TicketTapeEventId);
        if (!cursor.isNull(ticketTapeEventIdIndex)) {
            cppkServiceSale.setTicketTapeEventId(cursor.getLong(ticketTapeEventIdIndex));
        }
        cppkServiceSale.setPriceId(cursor.getLong(cursor.getColumnIndex(Properties.PriceId)));
        cppkServiceSale.setServiceFeeCode(cursor.getLong(cursor.getColumnIndex(Properties.ServiceFeeCode)));
        cppkServiceSale.setServiceFeeName(cursor.getString(cursor.getColumnIndex(Properties.ServiceFeeName)));
        //SaleDateTime В бд сохранено в секундах, поэтому при выборки из БД его надо перевести в милисекунды
        cppkServiceSale.setSaleDateTime(new Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndex(Properties.SaleDateTime)))));
        return cppkServiceSale;
    }

    @Override
    public ContentValues toContentValues(CPPKServiceSale entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.CashRegisterWorkingShiftId, entity.getShiftEventId());
        contentValues.put(Properties.TicketTapeEventId, entity.getTicketTapeEventId() > 0 ? entity.getTicketTapeEventId() : null);
        contentValues.put(Properties.CheckId, entity.getCheckId());
        contentValues.put(Properties.PriceId, entity.getPriceId());
        contentValues.put(Properties.ServiceFeeCode, entity.getServiceFeeCode());
        contentValues.put(Properties.ServiceFeeName, entity.getServiceFeeName());
        contentValues.put(Properties.SaleDateTime, TimeUnit.MILLISECONDS.toSeconds(entity.getSaleDateTime().getTime())); //SaleDateTime СОХРАНЯЕМ В СЕКУНДАХ);
        return contentValues;
    }

    @Override
    public Long getKey(CPPKServiceSale entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull CPPKServiceSale entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    /**
     * Возврашщает событие продажи услуги по id
     *
     * @param id Идентификатор события
     * @return Событие продажи услуги
     */
    public CPPKServiceSale load(long id) {

        String selection = BaseEntityDao.Properties.Id + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        CPPKServiceSale cppkServiceSale = null;
        Cursor cursor = null;
        try {
            cursor = db().query(CPPKServiceSaleDao.TABLE_NAME, null, selection, selectionArgs,
                    null, null, null);

            if (cursor.moveToFirst()) {
                cppkServiceSale = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return cppkServiceSale;
    }

    /**
     * Возвращает количество проданных услуг для месяца
     *
     * @param monthEvent
     * @return
     */
    public int getCountServicePdForMonth(@NonNull MonthEvent monthEvent) {

        int count = 0;

        // select count(*) from CPPKServiceSale
        // JOIN CashRegisterWorkingShift ON CPPKServiceSale.CashRegisterWorkingShiftId = CashRegisterWorkingShift._id
        // JOIN Months ON CashRegisterWorkingShift.MonthId = Months._id WHERE Months.Number = 1

        StringBuilder builder = new StringBuilder();
        builder.append("Select count(*) from ").append(CPPKServiceSaleDao.TABLE_NAME)
                .append(" JOIN ").append(ShiftEventDao.TABLE_NAME)
                .append(" ON ").append(CPPKServiceSaleDao.TABLE_NAME)
                .append(".").append(CPPKServiceSaleDao.Properties.CashRegisterWorkingShiftId)
                .append(" = ").append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .append(" JOIN ").append(MonthEventDao.TABLE_NAME)
                .append(" ON ").append(ShiftEventDao.TABLE_NAME)
                .append(".").append(ShiftEventDao.Properties.MonthEventId)
                .append(" = ").append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .append(" WHERE ").append(MonthEventDao.TABLE_NAME).append(".").append(MonthEventDao.Properties.Number)
                .append(" = ").append(monthEvent.getMonthNumber());

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

        Logger.info(TAG, "getCountServicePdForMonth /n " + builder.toString() + "/n result: " + count);
        return count;
    }

    /**
     * Выполняет поиск событий оформления услуги по указанным параметрам.
     *
     * @param shiftId       UUID смены
     * @param monthId       UUID месяца
     * @param shiftStatuses Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @param orderDesc     Использовать сортировку в обратном порядке
     * @param limit         Количество записей, 0 - вернуть все записи
     * @return Список событий оформления услуги
     */
    @NonNull
    private List<CPPKServiceSale> getCPPKServiceSalesByParams(@Nullable String shiftId,
                                                              @Nullable String monthId,
                                                              @Nullable String ticketTapeId,
                                                              @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                              boolean orderDesc,
                                                              int limit) {

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(CPPKServiceSaleDao.TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(CPPKServiceSaleDao.TABLE_NAME);
        if (shiftId != null || monthId != null) {
            builder.append(" JOIN ");
            builder.append(ShiftEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(CPPKServiceSaleDao.TABLE_NAME).append(".").append(CPPKServiceSaleDao.Properties.CashRegisterWorkingShiftId);
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
            builder.append(CPPKServiceSaleDao.TABLE_NAME).append(".").append(CPPKServiceSaleDao.Properties.TicketTapeEventId);
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
        ///////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY " + getIdWithTableName()).append(orderDesc ? " DESC " : " ASC ");
        if (limit > 0) {
            builder.append(" LIMIT ").append(limit);
        }

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        List<CPPKServiceSale> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                CPPKServiceSale item = fromCursor(cursor);
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
     * Возвращает первое событие оформления услуги на смену.
     *
     * @param shiftUid UUID смены
     * @return Событие оформления услуги.
     */
    public CPPKServiceSale getFirstServiceSaleForShift(@NonNull String shiftUid) {
        List<CPPKServiceSale> CPPKServiceSaleList = getCPPKServiceSalesByParams(shiftUid, null, null, null, false, 1);
        return CPPKServiceSaleList.isEmpty() ? null : CPPKServiceSaleList.get(0);
    }

    /**
     * Возвращает последнее событие оформления услуги на смену.
     *
     * @param shiftUid UUID смены
     * @return Событие оформления услуги.
     */
    public CPPKServiceSale getLastServiceSaleForShift(@NonNull String shiftUid) {
        List<CPPKServiceSale> CPPKServiceSaleList = getCPPKServiceSalesByParams(shiftUid, null, null, null, true, 1);
        return CPPKServiceSaleList.isEmpty() ? null : CPPKServiceSaleList.get(0);
    }

    /**
     * Возвращает первое обытие оформления услуги на месяц.
     *
     * @param monthUid      UUID месяца
     * @param shiftStatuses Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @return Событие оформления услуги.
     */
    public CPPKServiceSale getFirstServiceSaleForMonth(@NonNull String monthUid,
                                                       @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKServiceSale> CPPKServiceSaleList = getCPPKServiceSalesByParams(null, monthUid, null, shiftStatuses, false, 1);
        return CPPKServiceSaleList.isEmpty() ? null : CPPKServiceSaleList.get(0);
    }

    /**
     * Возвращает последнее событие оформления услуги на месяц.
     *
     * @param monthUid      UUID месяца
     * @param shiftStatuses Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @return Событие оформления услуги.
     */
    public CPPKServiceSale getLastServiceSaleForMonth(@NonNull String monthUid,
                                                      @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKServiceSale> CPPKServiceSaleList = getCPPKServiceSalesByParams(null, monthUid, null, shiftStatuses, true, 1);
        return CPPKServiceSaleList.isEmpty() ? null : CPPKServiceSaleList.get(0);
    }

    /**
     * Возвращает первое событие оформления услуги на билетной ленте.
     *
     * @param ticketTapeId UUID билетной ленты
     * @return Событие оформления услуги.
     */
    public CPPKServiceSale getFirstServiceSaleForTicketTape(String ticketTapeId,
                                                            @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKServiceSale> CPPKServiceSaleList = getCPPKServiceSalesByParams(null, null, ticketTapeId, shiftStatuses, false, 1);
        return CPPKServiceSaleList.isEmpty() ? null : CPPKServiceSaleList.get(0);
    }

    /**
     * Возвращает последнее событие оформления услуги на билетной ленте.
     *
     * @param ticketTapeId UUID билетной ленты
     * @return Событие оформления услуги.
     */
    public CPPKServiceSale getLastServiceSaleForTicketTape(String ticketTapeId,
                                                           @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKServiceSale> CPPKServiceSaleList = getCPPKServiceSalesByParams(null, null, ticketTapeId, shiftStatuses, true, 1);
        return CPPKServiceSaleList.isEmpty() ? null : CPPKServiceSaleList.get(0);
    }

    /**
     * Возвращает список событий оформления услуги за смену.
     *
     * @param shiftUid UUID смены
     * @return Список событий оформления услуги
     */
    @NonNull
    public List<CPPKServiceSale> getServiceSaleEventsForShift(
            @NonNull String shiftUid) {
        return getCPPKServiceSalesByParams(shiftUid, null, null, null, false, 0);
    }

    /**
     * Возвращает список событий оформления услуги за месяц.
     *
     * @param monthUid      UUID месяца
     * @param shiftStatuses Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @return Список событий оформления услуги/
     */
    @NonNull
    public List<CPPKServiceSale> getServiceSaleEventsForMonth(
            @NonNull String monthUid,
            @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        return getCPPKServiceSalesByParams(null, monthUid, null, shiftStatuses, false, 0);
    }

    /**
     * Возвращает количество услуг, оформленных на билетных лентах.
     *
     * @param ticketTapeIds Список идентификаторов билетных лент, для которых нужно получить информацию
     * @return количество услуг
     */
    public int getServiceSalesCountForTicketTapes(List<String> ticketTapeIds) {

        String countColumnName = "count";
        int count = 0;

        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append("COUNT(" + getIdWithTableName() + ") AS " + countColumnName);
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(CPPKServiceSaleDao.TABLE_NAME);
        builder.append(" JOIN ");
        builder.append(TicketTapeEventDao.TABLE_NAME);
        builder.append(" ON ");
        builder.append(CPPKServiceSaleDao.TABLE_NAME).append(".").append(CPPKServiceSaleDao.Properties.TicketTapeEventId);
        builder.append(" = ");
        builder.append(getLocalDaoSession().getTicketTapeEventDao().getIdWithTableName());
        builder.append(" AND ");
        builder.append(TicketTapeEventDao.Properties.TicketTapeId);
        builder.append(" IN ");
        builder.append("(").append(SqLiteUtils.makePlaceholders(ticketTapeIds.size())).append(")");
        // /////////////////////////////////////////////////////////////////////////////

        String[] selectionArgs = new String[ticketTapeIds.size()];
        ticketTapeIds.toArray(selectionArgs);

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);

            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndex(countColumnName));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    /**
     * Загружает список услуг начиная с даты отсортированных по дате.
     *
     * @param timestamp время, с которого необходимо загрузить список услуг
     * @return список услуг
     */
    @NonNull
    public List<CPPKServiceSale> loadEventsFromDate(@NonNull Date timestamp) {

        // Select CPPKServiceSale.* from CPPKServiceSale join Event
        // ON CPPKServiceSale.EventId = Event._id
        // WHERE Event.CreationTimestamp > 1465539326724 ORDER BY Event.CreationTimestamp

        final SqlQueryBuilder queryBuilder = SqlQueryBuilder.newBuilder();
        queryBuilder.select(CPPKServiceSaleDao.TABLE_NAME + ".*").from(CPPKServiceSaleDao.TABLE_NAME)
                .join(EventDao.TABLE_NAME).onEquals(CPPKServiceSaleDao.Properties.EventId, getLocalDaoSession().getEventDao().getIdWithTableName())
                .whereLarger(EventDao.Properties.CreationTimestamp, timestamp.getTime())
                .orderBy(EventDao.Properties.CreationTimestamp);

        List<CPPKServiceSale> list = new ArrayList<>();
        Cursor cursor = db().rawQuery(queryBuilder.buildQuery(), null);
        try {
            while (cursor.moveToNext()) {
                CPPKServiceSale event = fromCursor(cursor);
                if (event != null) {
                    list.add(event);
                }
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    /**
     * Возвращает timestamp создания последнего события продажи услуги
     *
     * @return
     */
    public long getLastServiceSaleEventCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * формируем запрос
         *
         * SELECT max(CreationTimestamp) FROM CPPKServiceSale
         * JOIN Event ON CPPKServiceSale.EventId = Event._id
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(EventDao.Properties.CreationTimestamp).append(") FROM ").append(CPPKServiceSaleDao.TABLE_NAME);
        sql.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ON ").append(CPPKServiceSaleDao.Properties.EventId).append(" = ").append(getLocalDaoSession().getEventDao().getIdWithTableName());

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
