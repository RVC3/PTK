package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.SqlQueryBuilder;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.entity.event.model34.WritePdToBscError;
import ru.ppr.cppk.localdb.model.AuditTrailEventType;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * Класс для работы с ивентами продажи
 * <p>
 * Created by Артем on 14.12.2015.
 */
public class CppkTicketSaleDao extends BaseEntityDao<CPPKTicketSales, Long> {

    private static final String TAG = Logger.makeLogTag(CppkTicketSaleDao.class);

    public static final String TABLE_NAME = "CPPKTicketSales";

    public static class Properties {
        public static final String TicketSaleReturnEventBaseId = "TicketSaleReturnEventBaseId";
        public static final String TripsCount = "TripsCount";
        public static final String StorageTypeCode = "StorageTypeCode";
        public static final String EDSKeyNumber = "EDSKeyNumber";
        public static final String EventId = "EventId";
        public static final String TicketTapeEventId = "TicketTapeEventId";
        public static final String ProgressStatus = "ProgressStatus";
        public static final String FullTicketPrice = "FullTicketPrice";
        public static final String WriteErrorCode = "WriteErrorCode";
        public static final String CouponReadEventId = "CouponReadEventId";
        public static final String ConnectionType = "ConnectionType";
    }

    public CppkTicketSaleDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.TicketSaleReturnEventBaseId, TicketSaleReturnEventBaseDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public CPPKTicketSales fromCursor(@NonNull final Cursor cursor) {
        final CPPKTicketSales event = new CPPKTicketSales();

        event.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        event.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        event.setTicketSaleReturnEventBaseId(cursor.getLong(cursor.getColumnIndex(Properties.TicketSaleReturnEventBaseId)));

        if (!cursor.isNull(cursor.getColumnIndex(Properties.TripsCount)))
            event.setTripsCount(cursor.getInt(cursor.getColumnIndex(Properties.TripsCount)));

        event.setStorageTypeCode(TicketStorageType.getTypeByDBCode(cursor.getInt(cursor.getColumnIndex(Properties.StorageTypeCode))));
        event.setEDSKeyNumber(cursor.getLong(cursor.getColumnIndex(Properties.EDSKeyNumber)));
        event.setTicketTapeEventId(cursor.getLong(cursor.getColumnIndex(Properties.TicketTapeEventId)));
        event.setProgressStatus(ProgressStatus.get(cursor.getInt(cursor.getColumnIndex(Properties.ProgressStatus))));
        event.setFullTicketPrice(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.FullTicketPrice))));

        int errorColumnIndex = cursor.getColumnIndex(Properties.WriteErrorCode);
        if (!cursor.isNull(errorColumnIndex)) {
            event.setErrors(WritePdToBscError.createByCode(cursor.getInt(errorColumnIndex)));
        }

        int couponReadEventIdColumnIndex = cursor.getColumnIndex(Properties.CouponReadEventId);
        if (!cursor.isNull(couponReadEventIdColumnIndex)) {
            event.setCouponReadEventId(cursor.getLong(couponReadEventIdColumnIndex));
        }

        int connectionType = cursor.getColumnIndex(Properties.ConnectionType);
        if (!cursor.isNull(connectionType)) {
            event.setConnectionType(ConnectionType.valueOf(cursor.getInt(connectionType)));
        }

        return event;
    }

    @Override
    public ContentValues toContentValues(CPPKTicketSales entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.EDSKeyNumber, entity.getEDSKeyNumber() == -1 ? null : entity.getEDSKeyNumber());
        contentValues.put(Properties.TicketSaleReturnEventBaseId, entity.getTicketSaleReturnEventBaseId());
        contentValues.put(Properties.TripsCount, entity.getTripsCount());
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.StorageTypeCode, entity.getStorageTypeCode().getDBCode());
        contentValues.put(Properties.TicketTapeEventId, entity.getTicketTapeEventId());
        contentValues.put(Properties.ProgressStatus, entity.getProgressStatus().getCode());
        contentValues.put(Properties.FullTicketPrice, entity.getFullTicketPrice().toString());
        contentValues.put(Properties.ConnectionType, entity.getConnectionType() != null ? entity.getConnectionType().getCode() : null);
        contentValues.put(Properties.WriteErrorCode, entity.getErrors() == null ? null : entity.getErrors().getCode());
        contentValues.put(Properties.CouponReadEventId, entity.getCouponReadEventId() == -1 ? null : entity.getCouponReadEventId());
        return contentValues;
    }

    @Override
    public Long getKey(CPPKTicketSales entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull CPPKTicketSales entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    /**
     * Возвращает спикок проданных билетов за смену или за месяц, определенных статусов
     *
     * @param shiftId            id смены
     * @param progressStatusList список интересующих статусов событий, если null - вернутся все!
     * @return НЕИЗМЕНЯЕМЫЙ список с событиями продажи, если событий е было, то список будет пустым
     */
    public List<CPPKTicketSales> getSaleEventsForEtt(@Nullable String shiftId,
                                                     @Nullable List<ProgressStatus> progressStatusList) {

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TABLE_NAME);
        builder.append(" JOIN ");
        builder.append(TicketSaleReturnEventBaseDao.TABLE_NAME);
        builder.append(" ON ");
        builder.append(TABLE_NAME).append(".").append(Properties.TicketSaleReturnEventBaseId);
        builder.append(" = ");
        builder.append(TicketSaleReturnEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
        builder.append(" JOIN ");
        builder.append(ExemptionDao.TABLE_NAME);
        builder.append(" ON ");
        builder.append(TicketSaleReturnEventBaseDao.TABLE_NAME).append(".").append(TicketSaleReturnEventBaseDao.Properties.ExemptionId);
        builder.append(" = ");
        builder.append(getLocalDaoSession().exemptionDao().getIdWithTableName());
        builder.append(" JOIN ");
        builder.append(SmartCardDao.TABLE_NAME);
        builder.append(" ON ");
        builder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.SmartCardId);
        builder.append(" = ");
        builder.append(getLocalDaoSession().getSmartCardDao().getIdWithTableName());
        if (shiftId != null) {
            builder.append(" JOIN ");
            builder.append(TicketEventBaseDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TicketSaleReturnEventBaseDao.TABLE_NAME).append(".").append(TicketSaleReturnEventBaseDao.Properties.TicketEventBaseId);
            builder.append(" = ");
            builder.append(TicketEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            builder.append(" JOIN ");
            builder.append(ShiftEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TicketEventBaseDao.TABLE_NAME).append(".").append(TicketEventBaseDao.Properties.CashRegisterWorkingShiftId);
            builder.append(" = ");
            builder.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            if (shiftId != null) {
                builder.append(" AND ");
                builder.append(ShiftEventDao.Properties.ShiftId).append(" = ").append("?");
                selectionArgsList.add(shiftId);
            }
        }
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(SmartCardDao.TABLE_NAME).append(".").append(SmartCardDao.Properties.TypeCode).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(TicketStorageType.ETT.getDBCode()));
        builder.append(" AND ");
        builder.append(" NOT EXISTS ");
        builder.append("(");
        {
            builder.append("SELECT ");
            builder.append(CppkTicketReturnDao.TABLE_NAME).append(".*");
            builder.append(" FROM ");
            builder.append(CppkTicketReturnDao.TABLE_NAME);
            builder.append(" WHERE ");
            builder.append(CppkTicketReturnDao.TABLE_NAME).append(".").append(CppkTicketReturnDao.Properties.CppkTicketSaleId).append(" = ").append(getIdWithTableName());
            builder.append(" AND ");
            if (progressStatusList != null) {
                builder.append(CppkTicketReturnDao.TABLE_NAME).append(".").append(CppkTicketReturnDao.Properties.ProgressStatus);
                builder.append(" IN ");
                builder.append(" ( ");
                for (int i = 0; i < progressStatusList.size(); i++) {
                    if (i > 0) builder.append(", ");
                    builder.append(progressStatusList.get(i).getCode());
                }
                builder.append(" ) ");
            }
        }
        builder.append(")");
        builder.append(" AND ");
        if (progressStatusList != null) {
            builder.append(TABLE_NAME).append(".").append(Properties.ProgressStatus);
            builder.append(" IN ");
            builder.append(" ( ");
            for (int i = 0; i < progressStatusList.size(); i++) {
                if (i > 0) builder.append(", ");
                builder.append(progressStatusList.get(i).getCode());
            }
            builder.append(" ) ");
        }
        ///////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY " + getIdWithTableName()).append(" ASC ");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        List<CPPKTicketSales> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                CPPKTicketSales item = fromCursor(cursor);
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
     * Возвращает список продаж ПД за смену в обратном порядке.
     *
     * @param shiftUid         UUID смены
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @param orderDesc        Использовать сортировку в обратном порядке
     * @return Список продаж ПД
     */
    @NonNull
    public List<CPPKTicketSales> getSaleEventsForShift(
            @NonNull String shiftUid,
            @Nullable EnumSet<ProgressStatus> progressStatuses,
            boolean orderDesc
    ) {
        return getSaleEventsByParams(shiftUid, null, null, progressStatuses, null, orderDesc, 0);
    }

    /**
     * Возвращает список продаж ПД за месяц.
     *
     * @param monthUid         UUID месяца
     * @param shiftStatuses    Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @param orderDesc        Использовать сортировку в обратном порядке
     * @return Список продаж ПД
     */
    @NonNull
    public List<CPPKTicketSales> getSaleEventsForMonth(
            @NonNull String monthUid,
            @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
            @Nullable EnumSet<ProgressStatus> progressStatuses,
            boolean orderDesc
    ) {
        return getSaleEventsByParams(null, monthUid, null, progressStatuses, shiftStatuses, orderDesc, 0);
    }

    /**
     * Выполняет поиск продаж ПД по указанным параметрам.
     *
     * @param shiftId          UUID смены
     * @param monthId          UUID месяца
     * @param ticketTapeId     UUID билетной ленты
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @param shiftStatuses    Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @param orderDesc        Использовать сортировку в обратном порядке
     * @param limit            Количество записей, 0 - вернуть все записи
     * @return Список продаж ПД
     */
    @NonNull
    private List<CPPKTicketSales> getSaleEventsByParams(@Nullable String shiftId,
                                                        @Nullable String monthId,
                                                        @Nullable String ticketTapeId,
                                                        @Nullable EnumSet<ProgressStatus> progressStatuses,
                                                        @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                        boolean orderDesc,
                                                        int limit) {

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TABLE_NAME);
        if (shiftId != null || monthId != null) {
            builder.append(" JOIN ");
            builder.append(TicketSaleReturnEventBaseDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TABLE_NAME).append(".").append(Properties.TicketSaleReturnEventBaseId);
            builder.append(" = ");
            builder.append(TicketSaleReturnEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            builder.append(" JOIN ");
            builder.append(TicketEventBaseDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TicketSaleReturnEventBaseDao.TABLE_NAME).append(".").append(TicketSaleReturnEventBaseDao.Properties.TicketEventBaseId);
            builder.append(" = ");
            builder.append(TicketEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            builder.append(" JOIN ");
            builder.append(ShiftEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TicketEventBaseDao.TABLE_NAME).append(".").append(TicketEventBaseDao.Properties.CashRegisterWorkingShiftId);
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
            builder.append(TABLE_NAME).append(".").append(Properties.TicketTapeEventId);
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
        if (progressStatuses != null) {
            builder.append(" AND ");
            builder.append(TABLE_NAME).append(".").append(Properties.ProgressStatus);
            builder.append(" IN ");
            builder.append(" ( ");
            builder.append(SqLiteUtils.makePlaceholders(progressStatuses.size()));
            for (ProgressStatus progressStatus : progressStatuses) {
                selectionArgsList.add(String.valueOf(progressStatus.getCode()));
            }
            builder.append(" ) ");
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

        List<CPPKTicketSales> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                CPPKTicketSales item = fromCursor(cursor);
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
     * Возвращает события продаж отсортированных по датам
     *
     * @param fromTimeStamp время, с какого момента необходимо получить события,
     *                      либо null если время не важно
     * @param toTimeStamp   время, до которого необходимо получить события продажи,
     *                      либо null если время не важно
     * @param statuses      список нужных статусов события продажи,
     *                      либо null если нужны все
     * @return НЕИЗМЕНЯЕМЫЙ список с событиями продажи, если событий не было, то список будет пустым
     */
    public List<CPPKTicketSales> getSalesEvents(@Nullable Date fromTimeStamp,
                                                @Nullable Date toTimeStamp,
                                                @Nullable List<ProgressStatus> statuses,
                                                boolean orderDesc) {

        StringBuilder builder = new StringBuilder();

        // SELECT CPPKTicketSales.*
        // FROM CPPKTicketSales
        // JOIN Event
        // ON CPPKTicketSales.EventId = Event._id
        // WHERE CreationTimestamp > fromTimeStamp
        // AND CreationTimestamp < toTimeStamp
        // ORDER BY CreationTimestamp DESC

        builder.append("SELECT ").append(TABLE_NAME).append(".*")
                .append(" FROM ").append(TABLE_NAME)
                .append(" JOIN ").append(EventDao.TABLE_NAME)
                .append(" ON ").append(TABLE_NAME).append(".").append(Properties.EventId)
                .append(" = ").append(getLocalDaoSession().getEventDao().getIdWithTableName())
                .append(" WHERE 1 = 1 ");
        if (fromTimeStamp != null) {
            builder.append(" AND ");
            builder.append(EventDao.Properties.CreationTimestamp).append(" > ").append(fromTimeStamp.getTime());
        }
        if (toTimeStamp != null) {
            builder.append(" AND ");
            builder.append(EventDao.Properties.CreationTimestamp).append(" < ").append(toTimeStamp.getTime());
        }
        if (statuses != null && !statuses.isEmpty()) {
            builder.append(" AND ( ");

            for (int i = 0; i < statuses.size(); i++) {
                ProgressStatus status = statuses.get(i);

                builder.append(Properties.ProgressStatus).append(" = ").append(status.getCode());

                if (i != statuses.size() - 1)
                    builder.append(" OR ");
            }

            builder.append(" )");
        }
        ///////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY " + EventDao.Properties.CreationTimestamp).append(orderDesc ? " DESC " : " ASC ");

        ImmutableList.Builder<CPPKTicketSales> listBuilder = ImmutableList.builder();
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), null);
            while (cursor.moveToNext()) {
                CPPKTicketSales item = fromCursor(cursor);
                listBuilder.add(item);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return listBuilder.build();
    }

    public int getSaleCountForShift(@NonNull String shiftId) {

        // Необходимо получить количество событий продажи для текущего события смены.
        // Для этого будем использовать shiftStartTime и shiftOperationTime.
        // В эти поля записывается время принтера, следовательно и события продажи надо искать
        // по тому полю, значения для которого беруться из принтера. Т.е. будем искать по saleDateTime

        SqlQueryBuilder sqlQueryBuilder = SqlQueryBuilder.newBuilder();
        sqlQueryBuilder.select("count()").from(TABLE_NAME)
                .join(TicketSaleReturnEventBaseDao.TABLE_NAME)
                .onEquals(Properties.TicketSaleReturnEventBaseId,
                        TicketSaleReturnEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .join(TicketEventBaseDao.TABLE_NAME)
                .onEquals(TicketSaleReturnEventBaseDao.Properties.TicketEventBaseId,
                        TicketEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .join(ShiftEventDao.TABLE_NAME)
                .onEquals(TicketEventBaseDao.Properties.CashRegisterWorkingShiftId,
                        ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .whereEquals(ShiftEventDao.Properties.ShiftId, shiftId)
                .and(TABLE_NAME + "." + Properties.ProgressStatus + " = " + ProgressStatus.Completed.getCode()
                        + " OR " + TABLE_NAME + "." + Properties.ProgressStatus + " = " + ProgressStatus.CheckPrinted.getCode());

        int count = 0;
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(sqlQueryBuilder.buildQuery(), null);
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
     * Возвращает количество проданных билетах в рамках месяца
     *
     * @param monthEvent месяц, для которого необходимо получить количество проданных билетов
     * @return
     */
    public int getCountSalePdForMonth(@NonNull MonthEvent monthEvent) {

        /**
         * формируем запрос
         *
         *
         * SELECT count(*) from tableName JOIN TicketSaleReturnEventBase ON tableName.TicketSaleReturnEventBaseId = TicketSaleReturnEventBase._id
         * JOIN TicketEventBase ON TicketSaleReturnEventBase.TicketEventBaseId = TicketEventBase._id
         * JOIN CashRegisterWorkingShift ON TicketEventBase.CashRegisterWorkingShiftId = CashRegisterWorkingShift._id
         * JOIN Months ON CashRegisterWorkingShift.MonthId = Months._id
         * WHERE Months.Number = 1;
         *
         */

        StringBuilder builder = new StringBuilder();
        builder.append("Select count(*) from ").append(TABLE_NAME)
                .append(" join ").append(TicketSaleReturnEventBaseDao.TABLE_NAME)
                .append(" on ").append(TABLE_NAME)
                .append(".").append(Properties.TicketSaleReturnEventBaseId)
                .append(" = ").append(TicketSaleReturnEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .append(" join ").append(TicketEventBaseDao.TABLE_NAME)
                .append(" on ").append(TicketSaleReturnEventBaseDao.Properties.TicketEventBaseId)
                .append(" = ").append(TicketEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .append(" join ").append(ShiftEventDao.TABLE_NAME)
                .append(" on ").append(TicketEventBaseDao.Properties.CashRegisterWorkingShiftId)
                .append(" = ").append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .append(" join ").append(MonthEventDao.TABLE_NAME)
                .append(" on ").append(ShiftEventDao.Properties.MonthEventId)
                .append(" = ").append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .append(" where ").append(MonthEventDao.TABLE_NAME).append(".").append(MonthEventDao.Properties.MonthId)
                .append(" = '").append(monthEvent.getMonthId()).append("'")
                .append(" AND (")
                .append(TABLE_NAME).append(".").append(Properties.ProgressStatus).append("=").append(ProgressStatus.Completed.getCode())
                .append(" OR ")
                .append(TABLE_NAME).append(".").append(Properties.ProgressStatus).append("=").append(ProgressStatus.CheckPrinted.getCode())
                .append(")");

        int number = 0;
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), null);
            if (cursor.moveToFirst()) {
                number = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Logger.trace(CppkTicketSaleDao.class, "getCountSalePdForMonth \n " + builder.toString() + "\n result: " + number);
        return number;
    }

    /**
     * @param id
     * @return
     */
    @Nullable
    public CPPKTicketSales getCPPKTicketSalesEventById(long id) {

        CPPKTicketSales event = null;

        String selection = BaseEntityDao.Properties.Id + " = " + id;

        Cursor cursor = null;
        try {
            cursor = db().query(TABLE_NAME,
                    null,
                    selection,
                    null,
                    null,
                    null,
                    null);
            if (cursor.moveToFirst()) {
                event = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return event;
    }

    /**
     * Возвращает последнее событие продажи
     *
     * @return
     */
    @Nullable
    public CPPKTicketSales getLastSaleEvent() {

        String query = "Select " + TABLE_NAME + ".*" +
                " from " + TABLE_NAME +
                " join " + EventDao.TABLE_NAME +
                " on " + Properties.EventId +
                " = " + EventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id +
                " AND (" + TABLE_NAME + "." + Properties.ProgressStatus + " = " + ProgressStatus.CheckPrinted.getCode() +
                " OR " + TABLE_NAME + "." + Properties.ProgressStatus + " = " + ProgressStatus.Completed.getCode() + ") " +
                " ORDER BY " + BaseEntityDao.Properties.Id + " DESC " +
                " LIMIT 1";

        CPPKTicketSales sale = null;
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(query, null);
            if (cursor.moveToFirst()) {
                sale = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return sale;
    }

    /**
     * Ищет проданный билет с переданными параметрами
     *
     * @param pdNumber порядковый номер билета
     * @param edsKey   номер ключа эцп
     * @param saleDate время продажи ПД
     * @return событие продажи если оно было найдено, либо null если событие не найдено
     */
    @Nullable
    public CPPKTicketSales findSaleByParam(int pdNumber, long edsKey, Date saleDate) {

        /*
         * Формируем запрос вида:
         *
         * select *
         * from CPPKTicketSales
         * WHERE TicketSaleReturnEventBase
         *    IN
         *       (select TicketSaleReturnEventBase._id
         *        from TicketSaleReturnEventBase
         *          join TicketEventBase on TicketEventBaseId = TicketEventBase._id
         *          join CheckTable on CheckId = CheckTable._id
         *        where SaleDateTime = 1452591587
         *              AND SerialNumber = 7)
         * AND CPPKTicketSales.EDSKeyNumber =  1
         *
         */

        QueryBuilder qb = new QueryBuilder();

        qb.selectAll().from(TABLE_NAME);
        qb.where().field(Properties.TicketSaleReturnEventBaseId).in();
        qb.appendInBrackets(() -> {
            qb.select().field(TicketSaleReturnEventBaseDao.TABLE_NAME, BaseEntityDao.Properties.Id);
            qb.from(TicketSaleReturnEventBaseDao.TABLE_NAME);
            ////JOIN TicketEventBase
            qb.innerJoin(TicketEventBaseDao.TABLE_NAME).on();
            qb.f1EqF2(TicketSaleReturnEventBaseDao.TABLE_NAME, TicketSaleReturnEventBaseDao.Properties.TicketEventBaseId, TicketEventBaseDao.TABLE_NAME, BaseEntityDao.Properties.Id);
            ////JOIN Check
            qb.innerJoin(CheckDao.TABLE_NAME).on();
            qb.f1EqF2(TicketSaleReturnEventBaseDao.TABLE_NAME, TicketSaleReturnEventBaseDao.Properties.CheckId, CheckDao.TABLE_NAME, BaseEntityDao.Properties.Id);
            qb.where();
            qb.field(TicketEventBaseDao.Properties.SaleDateTime).eq(saleDate.getTime() / 1000);
            qb.and().field(CheckDao.TABLE_NAME, CheckDao.Properties.SerialNumber).eq(pdNumber);

        });
        qb.and().field(TABLE_NAME, Properties.EDSKeyNumber).eq(edsKey);

        CPPKTicketSales cppkTicketSales = null;
        Cursor cursor = null;
        try {
            Query query = qb.build();
            cursor = query.run(db());
            if (cursor.getCount() > 1) {
//                query.logQuery();
                Logger.error(TAG, "Database error. Expected 1 value. But was return " + cursor.getCount());
            }
            if (cursor.moveToFirst()) {
                cppkTicketSales = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return cppkTicketSales;
    }

    /**
     * Ищет последнее событие продажи, которое было совершено на карту
     *
     * @param smartCard данные о карте
     * @return событие продажи
     */
    public CPPKTicketSales findLastSellForSmartCard(SmartCard smartCard) {

        /*
            select CPPKTicketSales.* from CPPKTicketSales
            join TicketSaleReturnEventBase ON CPPKTicketSales.TicketSaleReturnEventBaseId = TicketSaleReturnEventBase._id
            join TicketEventBase ON TicketSaleReturnEventBase.TicketEventBaseId = TicketEventBase._id
            join SmartCard on TicketEventBase.SmartCardId = SmartCard._id
            join Event ON CPPKTicketSales.EventId = Event._id
            where SmartCard.CrystalSerialNumber = 2381171999 and SmartCard.OuterNumber = 3300002157
            order by Event.CreationTimestamp DESC
         */

        SqlQueryBuilder builder = SqlQueryBuilder.newBuilder();
        builder.select(TABLE_NAME + ".*").from(TABLE_NAME)
                .join(TicketSaleReturnEventBaseDao.TABLE_NAME)
                .onEquals(Properties.TicketSaleReturnEventBaseId, TicketSaleReturnEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .join(TicketEventBaseDao.TABLE_NAME)
                .onEquals(TicketSaleReturnEventBaseDao.Properties.TicketEventBaseId,
                        TicketEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .join(SmartCardDao.TABLE_NAME).onEquals(TicketEventBaseDao.Properties.SmartCardId,
                SmartCardDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .join(EventDao.TABLE_NAME).onEquals(Properties.EventId,
                getLocalDaoSession().getEventDao().getIdWithTableName())
                .where(SmartCardDao.Properties.CrystalSerialNumber + "=" + smartCard.getCrystalSerialNumber())
                .and(SmartCardDao.Properties.OuterNumber + "=" + smartCard.getOuterNumber())
                .and(TABLE_NAME + "." + Properties.ProgressStatus + " = "
                        + ProgressStatus.Completed.getCode() +
                        " OR " + TABLE_NAME + "." + Properties.ProgressStatus + " = "
                        + ProgressStatus.CheckPrinted.getCode())
                .orderBy(EventDao.Properties.CreationTimestamp).desc().limit(1);

        CPPKTicketSales sales = null;
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.buildQuery(), null);
            if (cursor.moveToFirst()) {
                sales = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return sales;
    }

    /**
     * Ищет последнюю продажу с использованием льготы на данную карту
     *
     * @param smartCard данные о карте
     * @return событие продажи
     */
    public CPPKTicketSales findLastSellWithExemptionForSmartCard(@NonNull SmartCard smartCard,
                                                                 @Nullable List<ProgressStatus> progressStatusList) {

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TABLE_NAME);
        builder.append(" JOIN ");
        builder.append(TicketSaleReturnEventBaseDao.TABLE_NAME);
        builder.append(" ON ");
        builder.append(TABLE_NAME).append(".").append(Properties.TicketSaleReturnEventBaseId);
        builder.append(" = ");
        builder.append(TicketSaleReturnEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
        builder.append(" JOIN ");
        builder.append(ExemptionDao.TABLE_NAME);
        builder.append(" ON ");
        builder.append(TicketSaleReturnEventBaseDao.TABLE_NAME).append(".").append(TicketSaleReturnEventBaseDao.Properties.ExemptionId);
        builder.append(" = ");
        builder.append(getLocalDaoSession().exemptionDao().getIdWithTableName());
        builder.append(" JOIN ");
        builder.append(SmartCardDao.TABLE_NAME);
        builder.append(" ON ");
        builder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.SmartCardId);
        builder.append(" = ");
        builder.append(getLocalDaoSession().getSmartCardDao().getIdWithTableName());
        builder.append(" WHERE ");
        builder.append(SmartCardDao.Properties.CrystalSerialNumber).append(" = ").append("?");
        selectionArgsList.add(smartCard.getCrystalSerialNumber());
        builder.append(" AND ");
        builder.append(SmartCardDao.Properties.OuterNumber).append(" = ").append("?");
        selectionArgsList.add(smartCard.getOuterNumber());
        if (progressStatusList != null) {
            builder.append(" AND ");
            builder.append(TABLE_NAME).append(".").append(Properties.ProgressStatus);
            builder.append(" IN ");
            builder.append(" ( ");
            builder.append(SqLiteUtils.makePlaceholders(progressStatusList.size()));
            for (ProgressStatus progressStatus : progressStatusList) {
                selectionArgsList.add(String.valueOf(progressStatus.getCode()));
            }
            builder.append(" ) ");
        }
        ///////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY " + getIdWithTableName()).append(" DESC");
        builder.append(" LIMIT ").append(1);

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        CPPKTicketSales sales = null;
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            if (cursor.moveToFirst()) {
                sales = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return sales;
    }

    /**
     * Возвращает первое событие оформления ПД на смену.
     *
     * @param shiftUid         UUID смены
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @return Событие оформления ПД.
     */
    public CPPKTicketSales getFirstSaleForShift(@NonNull String shiftUid,
                                                @Nullable EnumSet<ProgressStatus> progressStatuses) {
        List<CPPKTicketSales> cppkTicketSalesList = getSaleEventsByParams(shiftUid, null, null, progressStatuses, null, false, 1);
        return cppkTicketSalesList.isEmpty() ? null : cppkTicketSalesList.get(0);
    }

    /**
     * Возвращает последнее событие оформления ПД на смену.
     *
     * @param shiftUid         UUID смены
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @return Событие оформления ПД.
     */
    public CPPKTicketSales getLastSaleForShift(@NonNull String shiftUid,
                                               @Nullable EnumSet<ProgressStatus> progressStatuses) {
        List<CPPKTicketSales> cppkTicketSalesList = getSaleEventsByParams(shiftUid, null, null, progressStatuses, null, true, 1);
        return cppkTicketSalesList.isEmpty() ? null : cppkTicketSalesList.get(0);
    }

    /**
     * Возвращает первое событие оформления ПД на месяц.
     *
     * @param monthUid         UUID месяца
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @param shiftStatuses    Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @return Событие оформления ПД.
     */
    public CPPKTicketSales getFirstSaleForMonth(@NonNull String monthUid,
                                                @Nullable EnumSet<ProgressStatus> progressStatuses,
                                                @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKTicketSales> cppkTicketSalesList = getSaleEventsByParams(null, monthUid, null, progressStatuses, shiftStatuses, false, 1);
        return cppkTicketSalesList.isEmpty() ? null : cppkTicketSalesList.get(0);
    }

    /**
     * Возвращает последнее событие оформления ПД на месяц.
     *
     * @param monthUid         UUID месяца
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @param shiftStatuses    Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @return Событие оформления ПД.
     */
    public CPPKTicketSales getLastSaleForMonth(@NonNull String monthUid,
                                               @Nullable EnumSet<ProgressStatus> progressStatuses,
                                               @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKTicketSales> cppkTicketSalesList = getSaleEventsByParams(null, monthUid, null, progressStatuses, shiftStatuses, true, 1);
        return cppkTicketSalesList.isEmpty() ? null : cppkTicketSalesList.get(0);
    }

    /**
     * Возвращает первое событие оформления ПД на билетной ленте.
     *
     * @param ticketTapeId UUID билетной ленты
     * @return Событие оформления ПД.
     */
    @Nullable
    public CPPKTicketSales getFirstSaleForTicketTape(@NonNull String ticketTapeId,
                                                     @Nullable EnumSet<ProgressStatus> progressStatuses,
                                                     @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKTicketSales> cppkTicketSalesList = getSaleEventsByParams(null, null, ticketTapeId, progressStatuses, shiftStatuses, false, 1);
        return cppkTicketSalesList.isEmpty() ? null : cppkTicketSalesList.get(0);
    }

    /**
     * Возвращает последнее событие оформления ПД на билетной ленте.
     *
     * @param ticketTapeId UUID билетной ленты
     * @return Событие оформления ПД.
     */
    @Nullable
    public CPPKTicketSales getLastSaleForTicketTape(@NonNull String ticketTapeId,
                                                    @Nullable EnumSet<ProgressStatus> progressStatuses,
                                                    @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKTicketSales> cppkTicketSalesList = getSaleEventsByParams(null, null, ticketTapeId, progressStatuses, shiftStatuses, true, 1);
        return cppkTicketSalesList.isEmpty() ? null : cppkTicketSalesList.get(0);
    }

    public void clearUncompletedSales() throws Exception {

        getLocalDaoSession().beginTransaction();

        try {

            StringBuilder builder = new StringBuilder();
            List<String> selectionArgsList = new ArrayList<>();
            String[] selectionArgs = null;
            List<Long> ticketSalesIds = new ArrayList<>();
            List<Long> eventIds = new ArrayList<>();
            List<Long> saleReturnEventBaseIds = new ArrayList<>();
            List<Long> ticketEventBaseIds = new ArrayList<>();
            List<Long> additionInfoForEttIds = new ArrayList<>();
            List<Long> parentTicketInfoIds = new ArrayList<>();
            List<Long> exemptionIds = new ArrayList<>();
            List<Long> smartCardIds = new ArrayList<>();
            List<Long> checkIds = new ArrayList<>();
            List<Long> trainInfoIds = new ArrayList<>();
            List<Long> priceIds = new ArrayList<>();
            List<Long> feeIds = new ArrayList<>();
            List<Long> legalEntityIds = new ArrayList<>();
            List<Long> auditTrailEventIds = new ArrayList<>();

            /**
             * CPPKTicketSalesTable
             */
            selectionArgsList.clear();
            builder.delete(0, builder.length());
            ///////////////////////////////////////////////////////////////////////////////
            builder.append("SELECT ");
            builder.append(BaseEntityDao.Properties.Id).append(", ");
            builder.append(Properties.EventId).append(", ");
            builder.append(Properties.TicketSaleReturnEventBaseId);
            builder.append(" FROM ");
            builder.append(TABLE_NAME);
            builder.append(" WHERE ");
            builder.append(TABLE_NAME + "." + Properties.ProgressStatus);
            builder.append(" NOT IN ");
            builder.append(" ( ");
            builder.append(ProgressStatus.Completed.getCode()).append(",");
            builder.append(ProgressStatus.CheckPrinted.getCode());
            builder.append(" ) ");
            ///////////////////////////////////////////////////////////////////////////////
            selectionArgs = new String[selectionArgsList.size()];
            selectionArgsList.toArray(selectionArgs);

            Cursor cppkTicketSalesCursor = null;
            try {
                cppkTicketSalesCursor = db().rawQuery(builder.toString(), selectionArgs);
                while (cppkTicketSalesCursor.moveToNext()) {
                    if (!cppkTicketSalesCursor.isNull(cppkTicketSalesCursor.getColumnIndex(BaseEntityDao.Properties.Id))) {
                        ticketSalesIds.add(cppkTicketSalesCursor.getLong(cppkTicketSalesCursor.getColumnIndex(BaseEntityDao.Properties.Id)));
                    }
                    if (!cppkTicketSalesCursor.isNull(cppkTicketSalesCursor.getColumnIndex(Properties.EventId))) {
                        eventIds.add(cppkTicketSalesCursor.getLong(cppkTicketSalesCursor.getColumnIndex(Properties.EventId)));
                    }
                    if (!cppkTicketSalesCursor.isNull(cppkTicketSalesCursor.getColumnIndex(Properties.TicketSaleReturnEventBaseId))) {
                        saleReturnEventBaseIds.add(cppkTicketSalesCursor.getLong(cppkTicketSalesCursor.getColumnIndex(Properties.TicketSaleReturnEventBaseId)));
                    }
                }
            } finally {
                if (cppkTicketSalesCursor != null) {
                    cppkTicketSalesCursor.close();
                }
            }

            /**
             * TicketSaleReturnEventBaseTable
             */
            if (!saleReturnEventBaseIds.isEmpty()) {
                selectionArgsList.clear();
                builder.delete(0, builder.length());
                ///////////////////////////////////////////////////////////////////////////////
                builder.append("SELECT ");
                builder.append(TicketSaleReturnEventBaseDao.Properties.TicketEventBaseId).append(", ");
                builder.append(TicketSaleReturnEventBaseDao.Properties.AdditionInfoForEttId).append(", ");
                builder.append(TicketSaleReturnEventBaseDao.Properties.ParentTicketId).append(", ");
                builder.append(TicketSaleReturnEventBaseDao.Properties.ExemptionId).append(", ");
                builder.append(TicketSaleReturnEventBaseDao.Properties.CheckId).append(", ");
                builder.append(TicketSaleReturnEventBaseDao.Properties.TrainInfoId).append(", ");
                builder.append(TicketSaleReturnEventBaseDao.Properties.PriceId).append(", ");
                builder.append(TicketSaleReturnEventBaseDao.Properties.FeeId).append(", ");
                builder.append(TicketSaleReturnEventBaseDao.Properties.LegalEntityId);
                builder.append(" FROM ");
                builder.append(TicketSaleReturnEventBaseDao.TABLE_NAME);
                builder.append(" WHERE ");
                builder.append(BaseEntityDao.Properties.Id);
                builder.append(" IN ");
                builder.append(" ( ");
                builder.append(SqLiteUtils.makePlaceholders(saleReturnEventBaseIds.size()));
                for (Long saleReturnEventBaseId : saleReturnEventBaseIds) {
                    selectionArgsList.add(String.valueOf(saleReturnEventBaseId));
                }
                builder.append(" ) ");
                ///////////////////////////////////////////////////////////////////////////////
                selectionArgs = new String[selectionArgsList.size()];
                selectionArgsList.toArray(selectionArgs);

                Cursor ticketSaleReturnEventBaseCursor = null;
                try {
                    ticketSaleReturnEventBaseCursor = db().rawQuery(builder.toString(), selectionArgs);
                    while (ticketSaleReturnEventBaseCursor.moveToNext()) {
                        if (!ticketSaleReturnEventBaseCursor.isNull(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.TicketEventBaseId))) {
                            ticketEventBaseIds.add(ticketSaleReturnEventBaseCursor.getLong(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.TicketEventBaseId)));
                        }
                        if (!ticketSaleReturnEventBaseCursor.isNull(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.AdditionInfoForEttId))) {
                            additionInfoForEttIds.add(ticketSaleReturnEventBaseCursor.getLong(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.AdditionInfoForEttId)));
                        }
                        if (!ticketSaleReturnEventBaseCursor.isNull(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.ParentTicketId))) {
                            parentTicketInfoIds.add(ticketSaleReturnEventBaseCursor.getLong(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.ParentTicketId)));
                        }
                        if (!ticketSaleReturnEventBaseCursor.isNull(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.ExemptionId))) {
                            exemptionIds.add(ticketSaleReturnEventBaseCursor.getLong(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.ExemptionId)));
                        }
                        if (!ticketSaleReturnEventBaseCursor.isNull(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.CheckId))) {
                            checkIds.add(ticketSaleReturnEventBaseCursor.getLong(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.CheckId)));
                        }
                        if (!ticketSaleReturnEventBaseCursor.isNull(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.TrainInfoId))) {
                            trainInfoIds.add(ticketSaleReturnEventBaseCursor.getLong(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.TrainInfoId)));
                        }
                        if (!ticketSaleReturnEventBaseCursor.isNull(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.PriceId))) {
                            priceIds.add(ticketSaleReturnEventBaseCursor.getLong(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.PriceId)));
                        }
                        if (!ticketSaleReturnEventBaseCursor.isNull(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.FeeId))) {
                            feeIds.add(ticketSaleReturnEventBaseCursor.getLong(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.FeeId)));
                        }
                        if (!ticketSaleReturnEventBaseCursor.isNull(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.LegalEntityId))) {
                            legalEntityIds.add(ticketSaleReturnEventBaseCursor.getLong(ticketSaleReturnEventBaseCursor.getColumnIndex(TicketSaleReturnEventBaseDao.Properties.LegalEntityId)));
                        }
                    }
                } finally {
                    if (ticketSaleReturnEventBaseCursor != null) {
                        ticketSaleReturnEventBaseCursor.close();
                    }
                }
            }

            /**
             * TicketEventBaseTable
             */
            if (!ticketEventBaseIds.isEmpty()) {
                selectionArgsList.clear();
                builder.delete(0, builder.length());
                ///////////////////////////////////////////////////////////////////////////////
                builder.append("SELECT ");
                builder.append(TicketEventBaseDao.Properties.SmartCardId);
                builder.append(" FROM ");
                builder.append(TicketEventBaseDao.TABLE_NAME);
                builder.append(" WHERE ");
                builder.append(BaseEntityDao.Properties.Id);
                builder.append(" IN ");
                builder.append(" ( ");
                builder.append(SqLiteUtils.makePlaceholders(ticketEventBaseIds.size()));
                for (Long ticketEventBaseId : ticketEventBaseIds) {
                    selectionArgsList.add(String.valueOf(ticketEventBaseId));
                }
                builder.append(" ) ");
                ///////////////////////////////////////////////////////////////////////////////
                selectionArgs = new String[selectionArgsList.size()];
                selectionArgsList.toArray(selectionArgs);

                Cursor ticketEventBaseCursor = null;
                try {
                    ticketEventBaseCursor = db().rawQuery(builder.toString(), selectionArgs);
                    while (ticketEventBaseCursor.moveToNext()) {
                        if (!ticketEventBaseCursor.isNull(ticketEventBaseCursor.getColumnIndex(TicketEventBaseDao.Properties.SmartCardId))) {
                            smartCardIds.add(ticketEventBaseCursor.getLong(ticketEventBaseCursor.getColumnIndex(TicketEventBaseDao.Properties.SmartCardId)));
                        }
                    }
                } finally {
                    if (ticketEventBaseCursor != null) {
                        ticketEventBaseCursor.close();
                    }
                }
            }

            /**
             * ExemptionForEventTable
             */
            if (!exemptionIds.isEmpty()) {
                selectionArgsList.clear();
                builder.delete(0, builder.length());
                ///////////////////////////////////////////////////////////////////////////////
                builder.append("SELECT ");
                builder.append(ExemptionDao.Properties.SmartCardId);
                builder.append(" FROM ");
                builder.append(ExemptionDao.TABLE_NAME);
                builder.append(" WHERE ");
                builder.append(BaseEntityDao.Properties.Id);
                builder.append(" IN ");
                builder.append(" ( ");
                builder.append(SqLiteUtils.makePlaceholders(exemptionIds.size()));
                for (Long exemptionId : exemptionIds) {
                    selectionArgsList.add(String.valueOf(exemptionId));
                }
                builder.append(" ) ");
                ///////////////////////////////////////////////////////////////////////////////
                selectionArgs = new String[selectionArgsList.size()];
                selectionArgsList.toArray(selectionArgs);

                Cursor exemptionForEventCursor = null;
                try {
                    exemptionForEventCursor = db().rawQuery(builder.toString(), selectionArgs);
                    while (exemptionForEventCursor.moveToNext()) {
                        if (!exemptionForEventCursor.isNull(exemptionForEventCursor.getColumnIndex(ExemptionDao.Properties.SmartCardId))) {
                            smartCardIds.add(exemptionForEventCursor.getLong(exemptionForEventCursor.getColumnIndex(ExemptionDao.Properties.SmartCardId)));
                        }
                    }
                } finally {
                    if (exemptionForEventCursor != null) {
                        exemptionForEventCursor.close();
                    }
                }
            }

            /**
             * SmartCardTable
             */
            if (!smartCardIds.isEmpty()) {
                selectionArgsList.clear();
                builder.delete(0, builder.length());
                ///////////////////////////////////////////////////////////////////////////////
                builder.append("SELECT ");
                builder.append(SmartCardDao.Properties.PresentTicket1).append(", ");
                builder.append(SmartCardDao.Properties.PresentTicket2);
                builder.append(" FROM ");
                builder.append(SmartCardDao.TABLE_NAME);
                builder.append(" WHERE ");
                builder.append(BaseEntityDao.Properties.Id);
                builder.append(" IN ");
                builder.append(" ( ");
                builder.append(SqLiteUtils.makePlaceholders(smartCardIds.size()));
                for (Long smartCardId : smartCardIds) {
                    selectionArgsList.add(String.valueOf(smartCardId));
                }
                builder.append(" ) ");
                ///////////////////////////////////////////////////////////////////////////////
                selectionArgs = new String[selectionArgsList.size()];
                selectionArgsList.toArray(selectionArgs);

                Cursor smartCardCursor = null;
                try {
                    smartCardCursor = db().rawQuery(builder.toString(), selectionArgs);
                    while (smartCardCursor.moveToNext()) {
                        if (!smartCardCursor.isNull(smartCardCursor.getColumnIndex(SmartCardDao.Properties.PresentTicket1))) {
                            parentTicketInfoIds.add(smartCardCursor.getLong(smartCardCursor.getColumnIndex(SmartCardDao.Properties.PresentTicket1)));
                        }
                        if (!smartCardCursor.isNull(smartCardCursor.getColumnIndex(SmartCardDao.Properties.PresentTicket2))) {
                            parentTicketInfoIds.add(smartCardCursor.getLong(smartCardCursor.getColumnIndex(SmartCardDao.Properties.PresentTicket2)));
                        }
                    }
                } finally {
                    if (smartCardCursor != null) {
                        smartCardCursor.close();
                    }
                }
            }

            /**
             * AuditTrailEventTable
             */
            if (!ticketSalesIds.isEmpty()) {
                selectionArgsList.clear();
                builder.delete(0, builder.length());
                ///////////////////////////////////////////////////////////////////////////////
                builder.append("SELECT ");
                builder.append(BaseEntityDao.Properties.Id);
                builder.append(" FROM ");
                builder.append(AuditTrailEventDao.TABLE_NAME);
                builder.append(" WHERE ");
                builder.append(AuditTrailEventDao.Properties.ExtEventId);
                builder.append(" IN ");
                builder.append(" ( ");
                builder.append(SqLiteUtils.makePlaceholders(ticketSalesIds.size()));
                for (Long ticketSalesId : ticketSalesIds) {
                    selectionArgsList.add(String.valueOf(ticketSalesId));
                }
                builder.append(" ) ");
                builder.append(" AND ");
                builder.append(AuditTrailEventDao.Properties.Type);
                builder.append(" IN ");
                builder.append(" ( ");
                builder.append(AuditTrailEventType.SALE.getCode()).append(",");
                builder.append(AuditTrailEventType.SALE_WITH_ADD_PAYMENT.getCode());
                builder.append(" ) ");
                ///////////////////////////////////////////////////////////////////////////////
                selectionArgs = new String[selectionArgsList.size()];
                selectionArgsList.toArray(selectionArgs);

                Cursor auditTrailEventCursor = null;
                try {
                    auditTrailEventCursor = db().rawQuery(builder.toString(), selectionArgs);
                    while (auditTrailEventCursor.moveToNext()) {
                        if (!auditTrailEventCursor.isNull(auditTrailEventCursor.getColumnIndex(BaseEntityDao.Properties.Id))) {
                            auditTrailEventIds.add(auditTrailEventCursor.getLong(auditTrailEventCursor.getColumnIndex(BaseEntityDao.Properties.Id)));
                        }
                    }
                } finally {
                    if (auditTrailEventCursor != null) {
                        auditTrailEventCursor.close();
                    }
                }
            }

            HashMap<String, List> rowsForDelete = new HashMap<>();
            rowsForDelete.put(TABLE_NAME, ticketSalesIds);
            rowsForDelete.put(EventDao.TABLE_NAME, eventIds);
            rowsForDelete.put(TicketSaleReturnEventBaseDao.TABLE_NAME, saleReturnEventBaseIds);
            rowsForDelete.put(TicketEventBaseDao.TABLE_NAME, ticketEventBaseIds);
            rowsForDelete.put(AdditionalInfoForEttDao.TABLE_NAME, additionInfoForEttIds);
            rowsForDelete.put(ParentTicketInfoDao.TABLE_NAME, parentTicketInfoIds);
            rowsForDelete.put(ExemptionDao.TABLE_NAME, exemptionIds);
            rowsForDelete.put(SmartCardDao.TABLE_NAME, smartCardIds);
            rowsForDelete.put(CheckDao.TABLE_NAME, checkIds);
            rowsForDelete.put(TrainInfoDao.TABLE_NAME, trainInfoIds);
            rowsForDelete.put(PriceDao.TABLE_NAME, priceIds);
            rowsForDelete.put(FeeDao.TABLE_NAME, feeIds);
            rowsForDelete.put(LegalEntityDao.TABLE_NAME, legalEntityIds);
            rowsForDelete.put(AuditTrailEventDao.TABLE_NAME, auditTrailEventIds);

            for (Map.Entry<String, List> entry : rowsForDelete.entrySet()) {
                String tableName = entry.getKey();
                List ids = entry.getValue();

                if (!ids.isEmpty()) {
                    selectionArgsList.clear();
                    builder.delete(0, builder.length());
                    /////////////////////////////////////
                    builder.append(BaseEntityDao.Properties.Id);
                    builder.append(" IN ");
                    builder.append(" ( ");
                    builder.append(SqLiteUtils.makePlaceholders(ids.size()));
                    for (Object id : ids) {
                        selectionArgsList.add(String.valueOf(id));
                    }
                    builder.append(" ) ");
                    /////////////////////////////////////
                    selectionArgs = new String[selectionArgsList.size()];
                    selectionArgsList.toArray(selectionArgs);

                    db().delete(tableName, builder.toString(), selectionArgs);
                }
            }
            getLocalDaoSession().setTransactionSuccessful();

        } catch (Exception e) {
            Logger.error(TAG, e);
            throw e;
        } finally {
            getLocalDaoSession().endTransaction();
        }
    }

    /**
     * Устанавливает ошибку записи на бск для события продажи
     *
     * @param newPdId ид события продажи
     * @param error   тип ошибки
     */
    public void saveErrorForSaleEvent(long newPdId, WritePdToBscError error) {

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();
        String[] selectionArgs;

        builder.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(newPdId));
        selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        // Обновим код ошибки
        ContentValues contentValuesCPPKTicketSales = new ContentValues();
        contentValuesCPPKTicketSales.put(Properties.WriteErrorCode, error.getCode());
        db().update(TABLE_NAME, contentValuesCPPKTicketSales, builder.toString(), selectionArgs);

    }

    /*
     * Возвращает timestamp создания последнего события продажи
     *
     * @return
     */
    public long getLastSaleEventCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * формируем запрос
         *
         * SELECT max(CreationTimestamp) FROM CPPKTicketSales
         * JOIN Event ON CPPKTicketControl.EventId = Event._id
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(EventDao.Properties.CreationTimestamp).append(") FROM ").append(TABLE_NAME);
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
