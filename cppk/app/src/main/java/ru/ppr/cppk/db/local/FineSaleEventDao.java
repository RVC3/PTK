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
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.references.ReferenceInfo;

/**
 * DAO для таблицы локальной БД <i>FineSaleEvent</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class FineSaleEventDao extends BaseEntityDao<FineSaleEvent, Long> {

    public static final String TABLE_NAME = "FineSaleEvent";

    public static class Properties {
        public static final String Amount = "Amount";
        public static final String OperationDateTime = "OperationDateTime";
        public static final String PaymentMethodCode = "PaymentMethodCode";
        public static final String EventId = "EventId";
        public static final String CashRegisterWorkingShiftId = "CashRegisterWorkingShiftId";
        public static final String TicketTapeEventId = "TicketTapeEventId";
        public static final String CheckId = "CheckId";
        public static final String BankTransactionEventId = "BankTransactionEventId";
        public static final String FineCode = "FineCode";
        public static final String Status = "Status";
    }

    public FineSaleEventDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.TicketTapeEventId, TicketTapeEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.CheckId, CheckDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.BankTransactionEventId, BankTransactionDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public FineSaleEvent fromCursor(@NonNull final Cursor cursor) {
        final FineSaleEvent fineSaleEvent = new FineSaleEvent();

        fineSaleEvent.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        fineSaleEvent.setAmount(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.Amount))));
        fineSaleEvent.setOperationDateTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.OperationDateTime))));
        fineSaleEvent.setPaymentMethod(PaymentType.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.PaymentMethodCode))));
        fineSaleEvent.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        fineSaleEvent.setShiftEventId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterWorkingShiftId)));
        int ticketTapeEventIdIndex = cursor.getColumnIndex(Properties.TicketTapeEventId);
        if (!cursor.isNull(ticketTapeEventIdIndex)) {
            fineSaleEvent.setTicketTapeEventId(cursor.getLong(ticketTapeEventIdIndex));
        }
        int checkIdIndex = cursor.getColumnIndex(Properties.CheckId);
        if (!cursor.isNull(checkIdIndex)) {
            fineSaleEvent.setCheckId(cursor.getLong(checkIdIndex));
        }
        int bankTransactionEventIdIndex = cursor.getColumnIndex(Properties.BankTransactionEventId);
        if (!cursor.isNull(bankTransactionEventIdIndex)) {
            fineSaleEvent.setBankTransactionEventId(cursor.getLong(cursor.getColumnIndex(Properties.BankTransactionEventId)));
        }
        fineSaleEvent.setFineCode(cursor.getLong(cursor.getColumnIndex(Properties.FineCode)));
        fineSaleEvent.setStatus(FineSaleEvent.Status.fromCode(cursor.getInt(cursor.getColumnIndex(Properties.Status))));

        return fineSaleEvent;
    }

    @Override
    public ContentValues toContentValues(@NonNull final FineSaleEvent entity) {
        final ContentValues contentValues = new ContentValues();

        contentValues.put(Properties.Amount, entity.getAmount().toPlainString());
        contentValues.put(Properties.OperationDateTime, entity.getOperationDateTime().getTime());
        contentValues.put(Properties.PaymentMethodCode, entity.getPaymentMethod().getCode());
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.CashRegisterWorkingShiftId, entity.getShiftEventId());
        if (entity.getTicketTapeEventId() != null) {
            contentValues.put(Properties.TicketTapeEventId, entity.getTicketTapeEventId());
        }
        if (entity.getCheckId() != null) {
            contentValues.put(Properties.CheckId, entity.getCheckId());
        }
        if (entity.getBankTransactionEventId() != null) {
            contentValues.put(Properties.BankTransactionEventId, entity.getBankTransactionEventId());
        }
        contentValues.put(Properties.FineCode, entity.getFineCode());
        contentValues.put(Properties.Status, entity.getStatus().getCode());

        return contentValues;
    }

    @Override
    public Long getKey(FineSaleEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull FineSaleEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    /**
     * Возвращает список событий оформления штрафа за смену.
     *
     * @param shiftUid UUID смены
     * @return Список событий оформления услуги
     */
    @NonNull
    public List<FineSaleEvent> getFineSaleEventsForShift(
            @NonNull String shiftUid,
            @Nullable EnumSet<FineSaleEvent.Status> statuses) {
        return getFineSaleEventsByParams(shiftUid, null, null, null, statuses, false, 0);
    }

    /**
     * Возвращает первое событие оформления штрафа за смену.
     *
     * @param shiftUid
     * @param statuses
     * @return
     */
    @Nullable
    public FineSaleEvent getFirstFineSaleEventForShift(@NonNull String shiftUid,
                                                       @Nullable EnumSet<FineSaleEvent.Status> statuses) {
        List<FineSaleEvent> fineSaleEventsForShift = getFineSaleEventsByParams(shiftUid, null, null, null, statuses, false, 1);
        return fineSaleEventsForShift.isEmpty() ? null : fineSaleEventsForShift.get(0);
    }

    /**
     * Возвращает последнее событие оформления штрафа за смену.
     *
     * @param shiftUid
     * @param statuses
     * @return
     */
    @Nullable
    public FineSaleEvent getLastFineSaleEventForShift(@NonNull String shiftUid,
                                                      @Nullable EnumSet<FineSaleEvent.Status> statuses) {
        List<FineSaleEvent> fineSaleEventsForShift = getFineSaleEventsByParams(shiftUid, null, null, null, statuses, true, 1);
        return fineSaleEventsForShift.isEmpty() ? null : fineSaleEventsForShift.get(0);
    }

    /**
     * Возвращает список событий оформления штрафа за месяц.
     *
     * @param monthUid      UUID месяца
     * @param shiftStatuses Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @return Список событий оформления услуги/
     */
    @NonNull
    public List<FineSaleEvent> getFineSaleEventsForMonth(
            @NonNull String monthUid,
            @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
            @Nullable EnumSet<FineSaleEvent.Status> statuses) {
        return getFineSaleEventsByParams(null, monthUid, null, shiftStatuses, statuses, false, 0);
    }

    /**
     * Возвращает первое событие оформления штрафа за месяц.
     *
     * @param monthUid
     * @param shiftStatuses
     * @param statuses
     * @return
     */
    @Nullable
    public FineSaleEvent getFirstFineSaleEventForMonth(@NonNull String monthUid,
                                                       @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                       @Nullable EnumSet<FineSaleEvent.Status> statuses) {
        List<FineSaleEvent> fineSaleEventsForMonth = getFineSaleEventsByParams(null, monthUid, null, shiftStatuses, statuses, false, 1);
        return fineSaleEventsForMonth.isEmpty() ? null : fineSaleEventsForMonth.get(0);
    }

    /**
     * Возвращает последнее событие оформления штрафа за месяц.
     *
     * @param monthUid
     * @param shiftStatuses
     * @param statuses
     * @return
     */
    @Nullable
    public FineSaleEvent getLastFineSaleEventForMonth(@NonNull String monthUid,
                                                      @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                      @Nullable EnumSet<FineSaleEvent.Status> statuses) {
        List<FineSaleEvent> fineSaleEventsForMonth = getFineSaleEventsByParams(null, monthUid, null, shiftStatuses, statuses, true, 1);
        return fineSaleEventsForMonth.isEmpty() ? null : fineSaleEventsForMonth.get(0);
    }

    /**
     * Выполняет поиск событий оформления штрафа по указанным параметрам.
     *
     * @param shiftId       UUID смены
     * @param monthId       UUID месяца
     * @param shiftStatuses Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @param statuses      Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @param orderDesc     Использовать сортировку в обратном порядке
     * @param limit         Количество записей, 0 - вернуть все записи
     * @return Список событий оформления услуги
     */
    @NonNull
    private List<FineSaleEvent> getFineSaleEventsByParams(@Nullable String shiftId,
                                                          @Nullable String monthId,
                                                          @Nullable String ticketTapeId,
                                                          @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                          @Nullable EnumSet<FineSaleEvent.Status> statuses,
                                                          boolean orderDesc,
                                                          int limit) {

        StringBuilder sb = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        sb.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        sb.append(TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        sb.append(" FROM ");
        sb.append(TABLE_NAME);
        if (shiftId != null || monthId != null) {
            sb.append(" JOIN ");
            sb.append(ShiftEventDao.TABLE_NAME);
            sb.append(" ON ");
            sb.append(TABLE_NAME).append(".").append(Properties.CashRegisterWorkingShiftId);
            sb.append(" = ");
            sb.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            if (monthId != null) {
                sb.append(" JOIN ");
                sb.append(MonthEventDao.TABLE_NAME);
                sb.append(" ON ");
                sb.append(ShiftEventDao.TABLE_NAME).append(".").append(ShiftEventDao.Properties.MonthEventId);
                sb.append(" = ");
                sb.append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            }
        }
        if (ticketTapeId != null) {
            sb.append(" JOIN ");
            sb.append(TicketTapeEventDao.TABLE_NAME);
            sb.append(" ON ");
            sb.append(TABLE_NAME).append(".").append(Properties.TicketTapeEventId);
            sb.append(" = ");
            sb.append(getLocalDaoSession().getTicketTapeEventDao().getIdWithTableName());
        }
        ////////////////////////////////////////////////////////////////////////////////
        sb.append(" WHERE 1 = 1");
        if (shiftId != null) {
            sb.append(" AND ");
            sb.append(ShiftEventDao.Properties.ShiftId).append(" = ").append("?");
            selectionArgsList.add(shiftId);
        }
        if (monthId != null) {
            sb.append(" AND ");
            sb.append(MonthEventDao.Properties.MonthId).append(" = ").append("?");
            selectionArgsList.add(monthId);
        }
        if (ticketTapeId != null) {
            sb.append(" AND ");
            sb.append(TicketTapeEventDao.Properties.TicketTapeId).append(" = ").append("?");
            selectionArgsList.add(ticketTapeId);
        }
        if (shiftStatuses != null) {
            sb.append(" AND ");
            sb.append(" EXISTS ");
            sb.append(" ( ");
            {
                sb.append("SELECT ");
                sb.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
                sb.append(" FROM ");
                sb.append(ShiftEventDao.TABLE_NAME).append(" AS ").append("SHIFTS");
                sb.append(" WHERE ");
                sb.append("SHIFTS").append(".").append(ShiftEventDao.Properties.ShiftId);
                sb.append(" = ");
                sb.append(ShiftEventDao.TABLE_NAME).append(".").append(ShiftEventDao.Properties.ShiftId);
                sb.append(" AND ");
                sb.append("SHIFTS").append(".").append(ShiftEventDao.Properties.ShiftStatus);
                sb.append(" IN ");
                sb.append(" ( ");
                sb.append(SqLiteUtils.makePlaceholders(shiftStatuses.size()));
                for (ShiftEvent.Status shiftStatus : shiftStatuses) {
                    selectionArgsList.add(String.valueOf(shiftStatus.getCode()));
                }
                sb.append(" ) ");
            }
            sb.append(" ) ");
        }
        if (statuses != null) {
            sb.append(" AND ");
            sb.append(TABLE_NAME).append(".").append(Properties.Status);
            sb.append(" IN ");
            sb.append(" ( ");
            sb.append(SqLiteUtils.makePlaceholders(statuses.size()));
            for (FineSaleEvent.Status status : statuses) {
                selectionArgsList.add(String.valueOf(status.getCode()));
            }
            sb.append(" ) ");
        }
        ///////////////////////////////////////////////////////////////////////////////
        sb.append(" ORDER BY ").append(TABLE_NAME).append('.').append(BaseEntityDao.Properties.Id).append(orderDesc ? " DESC " : " ASC ");
        if (limit > 0) {
            sb.append(" LIMIT ").append(limit);
        }

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        List<FineSaleEvent> fineSaleEvents = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(sb.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                FineSaleEvent fineSaleEvent = fromCursor(cursor);
                fineSaleEvents.add(fineSaleEvent);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fineSaleEvents;
    }

    /**
     * Возвращает количество оформленных штрафов за месяц.
     *
     * @param monthEvent Событие месяца
     * @param statuses   Статусы событий
     * @return Количество событий
     */
    public int getEventsCountForMonth(@NonNull MonthEvent monthEvent, @Nullable EnumSet<FineSaleEvent.Status> statuses) {

        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append("COUNT(*)").append(" FROM ").append(TABLE_NAME);
        sb.append(" JOIN ").append(ShiftEventDao.TABLE_NAME);
        sb.append(" ON ");
        sb.append(TABLE_NAME).append(".").append(Properties.CashRegisterWorkingShiftId);
        sb.append(" = ");
        sb.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
        sb.append(" JOIN ").append(MonthEventDao.TABLE_NAME);
        sb.append(" ON ");
        sb.append(ShiftEventDao.TABLE_NAME);
        sb.append(".").append(ShiftEventDao.Properties.MonthEventId);
        sb.append(" = ");
        sb.append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
        sb.append(" WHERE ");
        sb.append(MonthEventDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(monthEvent.getId()));
        if (statuses != null) {
            sb.append(" AND ");
            sb.append(TABLE_NAME).append(".").append(Properties.Status);
            sb.append(" IN ");
            sb.append(" ( ");
            sb.append(SqLiteUtils.makePlaceholders(statuses.size()));
            for (FineSaleEvent.Status status : statuses) {
                selectionArgsList.add(String.valueOf(status.getCode()));
            }
            sb.append(" ) ");
        }

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        int count = 0;
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(sb.toString(), selectionArgs);
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
     * Возвращает количество оформленных штрафов за смену.
     *
     * @param shiftId  Id смены
     * @param statuses Статусы событий
     * @return Количество событий
     */
    public int getFinePaidEventsCountForShift(String shiftId, @Nullable EnumSet<FineSaleEvent.Status> statuses) {
        int count = 0;

        List<Integer> statusesList = null;

        if (statuses != null) {
            statusesList = new ArrayList<>();

            for (FineSaleEvent.Status status : statuses) {
                statusesList.add(status.getCode());
            }
        }

        QueryBuilder qb = new QueryBuilder();

        qb.select().count("*").from(FineSaleEventDao.TABLE_NAME);
        qb.innerJoin(ShiftEventDao.TABLE_NAME).on();
        qb.f1EqF2(FineSaleEventDao.TABLE_NAME, Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, BaseEntityDao.Properties.Id);
        qb.where().field(ShiftEventDao.TABLE_NAME, ShiftEventDao.Properties.ShiftId).eq(shiftId);

        if (statusesList != null) {
            qb.and().field(FineSaleEventDao.TABLE_NAME, Properties.Status).in(statusesList);
        }

        Query query = qb.build();
        Cursor cursor = query.run(db());

        try {
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    count = cursor.getInt(0);
                }
            }
        } finally {
            cursor.close();
        }

        return count;
    }

    /**
     * Возвращает timestamp создания последнего события продажи штрафа
     *
     * @return
     */
    public long getLastFinePaidEventCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * формируем запрос
         *
         * SELECT max(CreationTimestamp) FROM FineSaleEvent
         * JOIN Event ON FineSaleEvent.EventId = Event._id
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
