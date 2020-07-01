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
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.entity.event.model34.ReturnOperationType;
import ru.ppr.cppk.localdb.model.BankOperationType;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.database.QueryBuilder;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * Dao объект для сущности {@link CPPKTicketReturn}
 * Created by Артем on 14.12.2015.
 */
public class CppkTicketReturnDao extends BaseEntityDao<CPPKTicketReturn, Long> {

    private static final String TAG = Logger.makeLogTag(CppkTicketReturnDao.class);

    public static final String TABLE_NAME = "CPPKTicketReturn";

    public static class Properties {
        public static final String EventId = "EventId";
        public static final String CppkTicketSaleId = "CppkTicketSaleEventId";
        public static final String ReturnOperationTypeCode = "ReturnOperationTypeCode";
        public static final String RecallDateTime = "RecallDateTime";
        public static final String RecallReason = "RecallReason";
        public static final String ReturnPaymentTypeCode = "ReturmPaymentTypeCode";
        public static final String ReturnBankTransactionCashRegisterEventId = "ReturnBankTransactionCashRegisterEventId";
        public static final String ReturnCheckId = "ReturnCheckId";
        public static final String ReturnPriceId = "PriceId";
        public static final String TicketTapeEventId = "TicketTapeEventId";
        public static final String CashRegisterWorkingShiftId = "CashRegisterWorkingShiftId";
        public static final String SumToReturn = "SumToReturn";
        public static final String ProgressStatus = "ProgressStatus";
        public static final String ReturnBankTerminalSlip = "ReturnBankTerminalSlip";
    }

    public CppkTicketReturnDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.CppkTicketSaleId, CppkTicketSaleDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.ReturnBankTransactionCashRegisterEventId, BankTransactionDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.ReturnPriceId, PriceDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.ReturnCheckId, CheckDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public CPPKTicketReturn fromCursor(Cursor cursor) {
        CPPKTicketReturn cppkTicketReturn = new CPPKTicketReturn();

        cppkTicketReturn.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        cppkTicketReturn.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        cppkTicketReturn.setPdSaleEventId(cursor.getLong(cursor.getColumnIndex(Properties.CppkTicketSaleId)));
        cppkTicketReturn.setOperation(ReturnOperationType.getType(cursor.getInt(cursor.getColumnIndex(Properties.ReturnOperationTypeCode))));
        cppkTicketReturn.setRecallDateTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.RecallDateTime))));
        cppkTicketReturn.setRecallReason(cursor.getString(cursor.getColumnIndex(Properties.RecallReason)));
        cppkTicketReturn.setReturnPaymentMethod(PaymentType.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.ReturnPaymentTypeCode))));

        if (!cursor.isNull(cursor.getColumnIndex(Properties.ReturnBankTransactionCashRegisterEventId)))
            cppkTicketReturn.setBankTransactionCashRegisterEventId(cursor.getLong(cursor.getColumnIndex(Properties.ReturnBankTransactionCashRegisterEventId)));

        cppkTicketReturn.setCheckId(cursor.getLong(cursor.getColumnIndex(Properties.ReturnCheckId)));
        cppkTicketReturn.setPriceId(cursor.getLong(cursor.getColumnIndex(Properties.ReturnPriceId)));
        cppkTicketReturn.setTicketTapeEventId(cursor.getLong(cursor.getColumnIndex(Properties.TicketTapeEventId)));
        cppkTicketReturn.setCashRegisterWorkingShiftId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterWorkingShiftId)));
        cppkTicketReturn.setSumToReturn(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.SumToReturn))));
        cppkTicketReturn.setProgressStatus(ProgressStatus.get(cursor.getInt(cursor.getColumnIndex(Properties.ProgressStatus))));

        if (!cursor.isNull(cursor.getColumnIndex(Properties.ReturnBankTerminalSlip)))
            cppkTicketReturn.setReturnBankTerminalSlip(cursor.getString(cursor.getColumnIndex(Properties.ReturnBankTerminalSlip)));

        return cppkTicketReturn;
    }

    @Override
    public ContentValues toContentValues(CPPKTicketReturn entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.CashRegisterWorkingShiftId, entity.getCashRegisterWorkingShiftId());
        contentValues.put(Properties.CppkTicketSaleId, entity.getPdSaleEventId());
        Check check = getLocalDaoSession().getCheckDao().load(entity.getCheckId());
        contentValues.put(Properties.ReturnCheckId, check == null ? null : check.getId());
        contentValues.put(Properties.ReturnPriceId, entity.getPriceId());
        long bankTransactionEventId = entity.getBankTransactionCashRegisterEventId();
        contentValues.put(Properties.ReturnBankTransactionCashRegisterEventId, bankTransactionEventId == -1 ? null : bankTransactionEventId);
        contentValues.put(Properties.RecallDateTime, entity.getRecallDateTime() == null ? null : entity.getRecallDateTime().getTime());
        contentValues.put(Properties.ReturnPaymentTypeCode, entity.getReturnPaymentMethod().getCode());
        contentValues.put(Properties.RecallReason, entity.getRecallReason());
        contentValues.put(Properties.ReturnOperationTypeCode, entity.getOperation().getCode());
        contentValues.put(Properties.TicketTapeEventId, entity.getTicketTapeEventId());
        contentValues.put(Properties.SumToReturn, entity.getSumToReturn().toString());
        contentValues.put(Properties.ProgressStatus, entity.getProgressStatus().getCode());
        contentValues.put(Properties.ReturnBankTerminalSlip, entity.getReturnBankTerminalSlip());
        return contentValues;
    }

    @Override
    public Long getKey(CPPKTicketReturn entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull CPPKTicketReturn entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    /**
     * Ищет последнее событие аннулирования ПД.
     *
     * @param pdSaleEventId Id события продажи ПД
     * @param statuses      Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @return Событие аннулирования ПД
     */
    @Nullable
    public CPPKTicketReturn findLastPdRepealEventForPdSaleEvent(long pdSaleEventId, @Nullable EnumSet<ProgressStatus> statuses) {

        List<Integer> statusesList = null;

        if (statuses != null) {
            statusesList = new ArrayList<>();

            for (ProgressStatus status : statuses) {
                statusesList.add(status.getCode());
            }
        }

        QueryBuilder qb = new QueryBuilder();
        qb.selectAll().from(TABLE_NAME);
        qb.where().field(Properties.CppkTicketSaleId).eq(pdSaleEventId);
        if (statusesList != null) {
            qb.and().field(Properties.ProgressStatus).in(statusesList);
        }
        qb.orderBy(BaseEntityDao.Properties.Id).desc();
        qb.limit(1);

        Cursor cursor = qb.build().run(db());
        try {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }

        return null;
    }


    /**
     * Возвращает количество аннулирований для смены
     *
     * @param shiftId смены, для которой получаем события
     * @return количество событий аннулирвоания
     */
    public int getRecallCountForShift(@NonNull String shiftId) {
        SqlQueryBuilder sqlQueryBuilder = SqlQueryBuilder.newBuilder();
        sqlQueryBuilder.select("count()").from(CppkTicketReturnDao.TABLE_NAME)
                .join(ShiftEventDao.TABLE_NAME)
                .onEquals(CppkTicketReturnDao.Properties.CashRegisterWorkingShiftId,
                        ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .whereEquals(ShiftEventDao.Properties.ShiftId, shiftId)
                .and(TABLE_NAME + "." + CppkTicketReturnDao.Properties.ProgressStatus + " = " + ProgressStatus.Completed.getCode()
                        + " OR " + TABLE_NAME + "." + CppkTicketReturnDao.Properties.ProgressStatus + " = " + ProgressStatus.CheckPrinted.getCode());


        final Cursor cursor = db().rawQuery(sqlQueryBuilder.buildQuery(), null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        return count;
    }

    /**
     * Возвращает количество аннулирований в рамках месяца
     *
     * @param monthEvent
     * @return
     */
    public int getCountRepealPdInMonth(@NonNull MonthEvent monthEvent) {

        StringBuilder builder = new StringBuilder();
        builder.append("Select count(*) from ").append(CppkTicketReturnDao.TABLE_NAME)
                .append(" join ").append(ShiftEventDao.TABLE_NAME)
                .append(" on ").append(TicketEventBaseDao.Properties.CashRegisterWorkingShiftId)
                .append(" = ").append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .append(" join ").append(MonthEventDao.TABLE_NAME)
                .append(" on ").append(ShiftEventDao.Properties.MonthEventId)
                .append(" = ").append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .append(" where ").append(MonthEventDao.TABLE_NAME).append(".").append(MonthEventDao.Properties.MonthId)
                .append(" = '").append(monthEvent.getMonthId()).append("'")
                .append(" AND (")
                .append(CppkTicketReturnDao.TABLE_NAME).append(".").append(CppkTicketReturnDao.Properties.ProgressStatus).append("=").append(ProgressStatus.Completed.getCode())
                .append(" OR ")
                .append(CppkTicketReturnDao.TABLE_NAME).append(".").append(CppkTicketReturnDao.Properties.ProgressStatus).append("=").append(ProgressStatus.CheckPrinted.getCode())
                .append(")");

        Cursor cursor = db().rawQuery(builder.toString(), null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        Logger.trace(CppkTicketReturnDao.class, "getCountRepealPdInMonth \n " + builder.toString() + "\n result: " + count);
        return count;
    }

    /**
     * Возвращает первое событие аннулирования ПД на смену.
     *
     * @param shiftUid         UUID смены
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @return Событие аннулирования ПД.
     */
    public CPPKTicketReturn getFirstReturnForShift(@NonNull String shiftUid,
                                                   @Nullable EnumSet<ProgressStatus> progressStatuses) {
        List<CPPKTicketReturn> cppkTicketReturnList = getReturnEventsByParams(shiftUid, null, null, progressStatuses, null, false, 1);
        return cppkTicketReturnList.isEmpty() ? null : cppkTicketReturnList.get(0);
    }

    /**
     * Возвращает последнее событие аннулирования ПД на смену.
     *
     * @param shiftUid         UUID смены
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @return Событие аннулирования ПД.
     */
    public CPPKTicketReturn getLastReturnForShift(@NonNull String shiftUid,
                                                  @Nullable EnumSet<ProgressStatus> progressStatuses) {
        List<CPPKTicketReturn> cppkTicketReturnList = getReturnEventsByParams(shiftUid, null, null, progressStatuses, null, true, 1);
        return cppkTicketReturnList.isEmpty() ? null : cppkTicketReturnList.get(0);
    }

    /**
     * Возвращает первое обытие аннулирования ПД на месяц.
     *
     * @param monthUid         UUID месяца
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @param shiftStatuses    Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @return Событие аннулирования ПД.
     */
    public CPPKTicketReturn getFirstReturnForMonth(@NonNull String monthUid,
                                                   @Nullable EnumSet<ProgressStatus> progressStatuses,
                                                   @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKTicketReturn> cppkTicketReturnList = getReturnEventsByParams(null, monthUid, null, progressStatuses, shiftStatuses, false, 1);
        return cppkTicketReturnList.isEmpty() ? null : cppkTicketReturnList.get(0);
    }

    /**
     * Возвращает последнее событие аннулирования ПД на месяц.
     *
     * @param monthUid         UUID месяца
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @param shiftStatuses    Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @return Событие аннулирования ПД.
     */
    public CPPKTicketReturn getLastReturnForMonth(@NonNull String monthUid,
                                                  @Nullable EnumSet<ProgressStatus> progressStatuses,
                                                  @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKTicketReturn> cppkTicketReturnList = getReturnEventsByParams(null, monthUid, null, progressStatuses, shiftStatuses, true, 1);
        return cppkTicketReturnList.isEmpty() ? null : cppkTicketReturnList.get(0);
    }

    /**
     * Выполняет поиск событий аннулирования ПД по указанным параметрам.
     *
     * @param shiftId          UUID смены
     * @param monthId          UUID месяца
     * @param ticketTapeId     UUID билетной ленты
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @param shiftStatuses    Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @param orderDesc        Использовать сортировку в обратном порядке
     * @param limit            Количество записей, 0 - вернуть все записи
     * @return Список событий аннулирования ПД
     */
    @NonNull
    private List<CPPKTicketReturn> getReturnEventsByParams(@Nullable String shiftId,
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
        builder.append(CppkTicketReturnDao.TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(CppkTicketReturnDao.TABLE_NAME);
        if (shiftId != null || monthId != null) {
            builder.append(" JOIN ");
            builder.append(ShiftEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(CppkTicketReturnDao.TABLE_NAME).append(".").append(CppkTicketReturnDao.Properties.CashRegisterWorkingShiftId);
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
            builder.append(CppkTicketReturnDao.TABLE_NAME).append(".").append(CppkTicketReturnDao.Properties.TicketTapeEventId);
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
            builder.append(CppkTicketReturnDao.TABLE_NAME).append(".").append(CppkTicketReturnDao.Properties.ProgressStatus);
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

        List<CPPKTicketReturn> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                CPPKTicketReturn item = fromCursor(cursor);
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
     * Возвращает первое событие аннулирования ПД на билетной ленте.
     *
     * @param ticketTapeId UUID билетной ленты
     * @return Событие аннулирования ПД.
     */
    public CPPKTicketReturn getFirstReturnForTicketTape(String ticketTapeId,
                                                        @Nullable EnumSet<ProgressStatus> progressStatuses,
                                                        @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKTicketReturn> cppkTicketReturnList = getReturnEventsByParams(null, null, ticketTapeId, progressStatuses, shiftStatuses, false, 1);
        return cppkTicketReturnList.isEmpty() ? null : cppkTicketReturnList.get(0);
    }

    /**
     * Возвращает последнее событие аннулирования ПД на билетной ленте.
     *
     * @param ticketTapeId UUID билетной ленты
     * @return Событие аннулирования ПД.
     */
    public CPPKTicketReturn getLastReturnForTicketTape(String ticketTapeId,
                                                       @Nullable EnumSet<ProgressStatus> progressStatuses,
                                                       @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        List<CPPKTicketReturn> cppkTicketReturnList = getReturnEventsByParams(null, null, ticketTapeId, progressStatuses, shiftStatuses, false, 1);
        return cppkTicketReturnList.isEmpty() ? null : cppkTicketReturnList.get(0);
    }

    /**
     * Возвращает список незавершенных событий аннулирования ПД.
     * Т.е. событий, для которых была отменена банковская транзакция, но по фискальнику аннулирование не было выполнено.
     *
     * @param shiftId
     * @return
     */
    public List<CPPKTicketReturn> getUncompletedEventsForShift(String shiftId) {

        EnumSet<ProgressStatus> statuses = EnumSet.of(ProgressStatus.CREATED, ProgressStatus.PrePrinting, ProgressStatus.Broken);
        List<CPPKTicketReturn> allUncompletedEvents = getReturnEventsByParams(shiftId, null, null, statuses, null, false, 0);

        List<CPPKTicketReturn> uncompletedEvents = new ArrayList<>();

        for (CPPKTicketReturn pdReturnEvent : allUncompletedEvents) {
            // Ищем событие продажи ПД
            if (pdReturnEvent.getReturnPaymentMethod() == PaymentType.INDIVIDUAL_CASH) {
                // Можно ориентироваться на тип платежа, он должен быть корректным с момента создания события аннулирования
                Logger.trace(TAG, "Uncompleted pd repeal event with cash payment, id = " + pdReturnEvent.getId() + ", skipping");
                continue;
            }
            CPPKTicketSales pdSaleEvent = getLocalDaoSession().getCppkTicketSaleDao().load(pdReturnEvent.getPdSaleEventId());
            if (pdSaleEvent == null) {
                throw new IllegalArgumentException("PdSaleEvent not found, id = " + pdReturnEvent.getPdSaleEventId());
            }
            // Получаем информацию о банковской транзакции для события продажи ПД
            TicketSaleReturnEventBase ticketSaleReturnEventBase = getLocalDaoSession().getTicketSaleReturnEventBaseDao().load(pdSaleEvent.getTicketSaleReturnEventBaseId());
            BankTransactionEvent saleBankTransactionEvent = getLocalDaoSession().getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
            BankTransactionEvent returnBankTransactionEvent = getLocalDaoSession().getBankTransactionDao()
                    .getEventByParams(saleBankTransactionEvent.getTransactionId(), BankOperationType.CANCELLATION, saleBankTransactionEvent.getTerminalDayId());
            if (returnBankTransactionEvent != null && returnBankTransactionEvent.getStatus() != BankTransactionEvent.Status.STARTED) {
                Logger.trace(TAG, "Uncompleted pd repeal event, id = " + pdReturnEvent.getId() + ", transactionId = " + returnBankTransactionEvent.getId());
                uncompletedEvents.add(pdReturnEvent);
            }
        }

        return uncompletedEvents;
    }

    /**
     * Возвращает timestamp создания последнего события аннулирования
     *
     * @return
     */
    public long getLastTicketReturnEventCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * формируем запрос
         *
         * SELECT max(CreationTimestamp) FROM CPPKTicketReturn
         * JOIN Event ON CPPKTicketReturn.EventId = Event._id
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(EventDao.Properties.CreationTimestamp).append(") FROM ").append(CppkTicketReturnDao.TABLE_NAME);
        sql.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ON ").append(CppkTicketReturnDao.Properties.EventId).append(" = ").append(getLocalDaoSession().getEventDao().getIdWithTableName());

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
     * Возвращает список событий аннулирования ПД за смену в обратном порядке.
     *
     * @param shiftUid         UUID смены
     * @param progressStatuses Список интересующих статусов событий, {@code null} - чтобы не использовать фильтрацию
     * @param orderDesc        Использовать сортировку в обратном порядке
     * @return Список событий аннулирования ПД
     */
    @NonNull
    public List<CPPKTicketReturn> getReturnEventsForShift(
            @NonNull String shiftUid,
            @Nullable EnumSet<ProgressStatus> progressStatuses,
            boolean orderDesc
    ) {
        return getReturnEventsByParams(shiftUid, null, null, progressStatuses, null, orderDesc, 0);
    }

}
