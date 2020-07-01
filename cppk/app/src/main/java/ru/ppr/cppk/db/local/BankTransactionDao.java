package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.localdb.model.BankOperationResult;
import ru.ppr.cppk.localdb.model.BankOperationType;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * Created by Артем on 15.12.2015.
 */
public class BankTransactionDao extends BaseEntityDao<BankTransactionEvent, Long> {

    private static final String TAG = Logger.makeLogTag(BankTransactionDao.class);

    public static final String TABLE_NAME = "BankTransactionCashRegisterEvent";

    public static class Properties {
        public static final String TransactionId = "TransactionId";
        public static final String EventId = "EventId";
        public static final String CashRegisterWorkingShiftId = "CashRegisterWorkingShiftId";
        public static final String TerminalDayId = "TerminalDayId";
        public static final String MonthId = "MonthId";
        public static final String TerminalNumber = "TerminalNumber";
        public static final String PointOfSaleNumber = "PointOfSaleNumber";
        public static final String MerchantId = "MerchantId";
        public static final String BankCode = "BankCode";
        public static final String OperationType = "OperationType";
        public static final String OperationResult = "OperationResult";
        public static final String Rrn = "Rrn";
        public static final String AuthorizationCode = "AuthorizationCode";
        public static final String SmartCardApplicationName = "SmartCardApplicationName";
        public static final String CardPan = "CardPan";
        public static final String CardEmitentName = "CardEmitentName";
        public static final String BankCheckNumber = "BankCheckNumber";
        public static final String TransactionDateTime = "TransactionDateTime";
        public static final String Total = "Total";
        public static final String CurrencyCode = "CurrencyCode";
        public static final String Status = "Status";
    }

    public BankTransactionDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.MonthId, MonthEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.TerminalDayId, TerminalDayDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public BankTransactionEvent fromCursor(Cursor cursor) {
        BankTransactionEvent out = new BankTransactionEvent();
        out.setId((cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id))));
        out.setEventId((cursor.getLong(cursor.getColumnIndex(Properties.EventId))));
        out.setTransactionId((cursor.getInt(cursor.getColumnIndex(Properties.TransactionId))));
        out.setCashRegisterWorkingShiftId((cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterWorkingShiftId))));
        out.setTerminalDayId((cursor.getLong(cursor.getColumnIndex(Properties.TerminalDayId))));
        out.setMonthId((cursor.getLong(cursor.getColumnIndex(Properties.MonthId))));
        out.setTerminalNumber(cursor.getString(cursor.getColumnIndex(Properties.TerminalNumber)));
        out.setPointOfSaleNumber(cursor.getString(cursor.getColumnIndex(Properties.PointOfSaleNumber)));
        out.setMerchantId(cursor.getString(cursor.getColumnIndex(Properties.MerchantId)));
        out.setBankCode(cursor.getInt(cursor.getColumnIndex(Properties.BankCode)));
        out.setOperationType(BankOperationType.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.OperationType))));
        out.setOperationResult(BankOperationResult.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.OperationResult))));
        out.setRrn(cursor.getString(cursor.getColumnIndex(Properties.Rrn)));
        out.setAuthorizationCode(cursor.getString(cursor.getColumnIndex(Properties.AuthorizationCode)));
        out.setSmartCardApplicationName(cursor.getString(cursor.getColumnIndex(Properties.SmartCardApplicationName)));
        out.setCardPan(cursor.getString(cursor.getColumnIndex(Properties.CardPan)));
        out.setCardEmitentName(cursor.getString(cursor.getColumnIndex(Properties.CardEmitentName)));
        out.setBankCheckNumber(cursor.getLong(cursor.getColumnIndex(Properties.BankCheckNumber)));
        out.setTransactionDateTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.TransactionDateTime))));
        out.setTotal(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.Total))));
        out.setCurrencyCode(cursor.getString(cursor.getColumnIndex(Properties.CurrencyCode)));
        out.setStatus(BankTransactionEvent.Status.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.Status))));
        return out;
    }

    @Override
    public ContentValues toContentValues(BankTransactionEvent entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.CashRegisterWorkingShiftId, entity.getCashRegisterWorkingShiftId());
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.MonthId, getLocalDaoSession().getMonthEventDao().getLastMonthEvent().getId());
        contentValues.put(Properties.TerminalDayId, entity.getTerminalDayId());
        contentValues.put(Properties.TransactionId, entity.getTransactionId());
        contentValues.put(Properties.TransactionDateTime, entity.getTransactionDateTime().getTime());
        contentValues.put(Properties.OperationResult, entity.getOperationResult().getCode());
        contentValues.put(Properties.TerminalNumber, entity.getTerminalNumber());
        contentValues.put(Properties.Total, entity.getTotal().toString());
        contentValues.put(Properties.CardPan, entity.getCardPan());
        contentValues.put(Properties.Rrn, entity.getRrn());
        contentValues.put(Properties.MerchantId, entity.getMerchantId());
        contentValues.put(Properties.AuthorizationCode, entity.getAuthorizationCode());
        contentValues.put(Properties.CardEmitentName, entity.getCardEmitentName());
        contentValues.put(Properties.CurrencyCode, entity.getCurrencyCode());
        contentValues.put(Properties.SmartCardApplicationName, entity.getSmartCardApplicationName());
        contentValues.put(Properties.OperationType, entity.getOperationType().getCode());
        contentValues.put(Properties.BankCode, entity.getBankCode());
        contentValues.put(Properties.BankCheckNumber, entity.getBankCheckNumber());
        contentValues.put(Properties.PointOfSaleNumber, entity.getPointOfSaleNumber());
        contentValues.put(Properties.Status, entity.getStatus().getCode());
        return contentValues;
    }

    @Override
    public Long getKey(BankTransactionEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull BankTransactionEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    public BankTransactionEvent getEventByParams(long transactionId, @NonNull BankOperationType bankOperationType, long terminalDayId) {

        BankTransactionEvent bankTransactionEvent = null;

        List<String> selectionArgsList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append("*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TABLE_NAME);
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(Properties.TransactionId).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(transactionId));
        builder.append(" AND ");
        builder.append(Properties.OperationType).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(bankOperationType.getCode()));
        builder.append(" AND ");
        builder.append(Properties.TerminalDayId).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(terminalDayId));
        builder.append(" order by ").append(BaseEntityDao.Properties.Id).append(" desc");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            if (cursor.moveToFirst()) {
                bankTransactionEvent = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return bankTransactionEvent;
    }

    public BankTransactionEvent getLastEventByType(@NonNull BankOperationType bankOperationType) {

        BankTransactionEvent bankTransactionEvent = null;

        List<String> selectionArgsList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append("*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(BankTransactionDao.TABLE_NAME);
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(Properties.OperationType).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(bankOperationType.getCode()));
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY " + BaseEntityDao.Properties.Id + " DESC");
        builder.append(" LIMIT 1");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            if (cursor.moveToFirst()) {
                bankTransactionEvent = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return bankTransactionEvent;
    }

    /**
     * Все успешные транзакции оплаты
     *
     * @param monthId месяц выборки
     * @return список транзакций
     */
    public List<BankTransactionEvent> getSuccessfulTransactions(String monthId) {
        return rawQuery(
                "SELECT * FROM BankTransactionCashRegisterEvent as btcre " +
                        "INNER JOIN MonthEvent AS me ON me._id = btcre.MonthId " +
                        "WHERE me.MonthId = '" + monthId + "' AND OperationType = " + BankOperationType.SALE.getCode() +
                        " AND OperationResult = " + BankOperationResult.Approved.getCode() + " AND btcre.Status <> " + BankTransactionEvent.Status.STARTED.getCode()
        );
    }

    /**
     * Все успешные транзакции отмены
     *
     * @param monthId месяц выборки
     * @return список транзакций
     */
    public List<BankTransactionEvent> getSuccessfulCancels(String monthId) {
        return rawQuery(
                "SELECT * FROM BankTransactionCashRegisterEvent as btcre " +
                        "INNER JOIN MonthEvent AS me ON me._id = btcre.MonthId " +
                        "WHERE me.MonthId = '" + monthId + "' AND OperationType = " + BankOperationType.CANCELLATION.getCode() +
                        " AND OperationResult = " + BankOperationResult.Approved.getCode() + " AND btcre.Status <> " + BankTransactionEvent.Status.STARTED.getCode()
        );
    }

    /**
     * Транзакции не прикреплённые к событию продажи ПД или штрафа + события продажи ПД и штрафа со статусом checkPrinted
     *
     * @param monthId месяц выборки
     * @return список транзакций
     */
    public List<BankTransactionEvent> getSuccessfulTransactionsWithoutSale(String monthId) {
        return rawQuery(
                "SELECT btcre.* FROM BankTransactionCashRegisterEvent AS btcre " +
                        " LEFT JOIN TicketSaleReturnEventBase AS tsreb ON tsreb.BankTransactionCashRegisterEventId = btcre._id " +
                        " LEFT JOIN FineSaleEvent AS fse ON fse.BankTransactionEventId = btcre._id " +
                        " INNER JOIN MonthEvent AS me ON me._id = btcre.MonthId " +
                        " WHERE (tsreb.BankTransactionCashRegisterEventId IS NULL AND fse.BankTransactionEventId is NULL) AND " +
                        " me.MonthId = '" + monthId + "' AND btcre.OperationType = "
                        + BankOperationType.SALE.getCode() + " AND btcre.OperationResult = " + BankOperationResult.Approved.getCode() +

                        " UNION ALL " +

                        " SELECT btcre.* FROM BankTransactionCashRegisterEvent AS btcre " +
                        " INNER JOIN TicketSaleReturnEventBase AS tsreb ON tsreb.BankTransactionCashRegisterEventId = btcre._id " +
                        " INNER JOIN CPPKTicketSales AS cppkts ON cppkts.TicketSaleReturnEventBaseId = tsreb._id " +
                        " INNER JOIN MonthEvent AS me ON me._id = btcre.MonthId " +
                        " WHERE me.MonthId = '" + monthId + "' AND cppkts.ProgressStatus = " + ProgressStatus.CheckPrinted.getCode() +

                        " UNION ALL " +

                        " SELECT btcre.*" +
                        " FROM BankTransactionCashRegisterEvent AS btcre " +
                        " JOIN FineSaleEvent AS fse ON fse.BankTransactionEventId = btcre._id " +
                        " INNER JOIN MonthEvent AS me ON me._id = btcre.MonthId " +
                        " WHERE me.MonthId = '" + monthId + "' AND fse.Status = " + FineSaleEvent.Status.CHECK_PRINTED.getCode()
        );
    }

    /**
     * Транзакции не прикреплённые к событию продажи ПД и штрафа + события продажи ПД и штрафа со статусом checkPrinted, но без успешно аннулированных.
     *
     * @param monthId месяц выборки
     * @return список транзакций
     */
    public List<BankTransactionEvent> getSuccessfulTransactionsWithoutSaleAndCancellation(String monthId) {
        return rawQuery(
                "SELECT btcre2.* FROM (SELECT btcre.* FROM BankTransactionCashRegisterEvent AS btcre " +
                        " LEFT JOIN TicketSaleReturnEventBase AS tsreb ON tsreb.BankTransactionCashRegisterEventId = btcre._id " +
                        " LEFT JOIN FineSaleEvent AS fse ON fse.BankTransactionEventId = btcre._id " +
                        " INNER JOIN MonthEvent AS me ON me._id = btcre.MonthId " +
                        " WHERE (tsreb.BankTransactionCashRegisterEventId IS NULL AND fse.BankTransactionEventId is NULL) AND " +
                        " me.MonthId = '" + monthId + "' AND btcre.OperationType = "
                        + BankOperationType.SALE.getCode() + " AND btcre.OperationResult = " + BankOperationResult.Approved.getCode() +

                        " UNION ALL " +

                        " SELECT btcre.* FROM BankTransactionCashRegisterEvent AS btcre " +
                        " INNER JOIN TicketSaleReturnEventBase AS tsreb ON tsreb.BankTransactionCashRegisterEventId = btcre._id " +
                        " INNER JOIN CPPKTicketSales AS cppkts ON cppkts.TicketSaleReturnEventBaseId = tsreb._id " +
                        " INNER JOIN MonthEvent AS me ON me._id = btcre.MonthId " +
                        " WHERE me.MonthId = '" + monthId + "' AND cppkts.ProgressStatus = " + ProgressStatus.CheckPrinted.getCode() +

                        " UNION ALL " +

                        " SELECT btcre.*" +
                        " FROM BankTransactionCashRegisterEvent AS btcre " +
                        " JOIN FineSaleEvent AS fse ON fse.BankTransactionEventId = btcre._id " +
                        " INNER JOIN MonthEvent AS me ON me._id = btcre.MonthId " +
                        " WHERE me.MonthId = '" + monthId + "' AND fse.Status = " + FineSaleEvent.Status.CHECK_PRINTED.getCode() + ") AS btcre1 " +
                        " INNER JOIN BankTransactionCashRegisterEvent AS btcre2 ON btcre1.AuthorizationCode = btcre2.AuthorizationCode " +
                        " INNER JOIN MonthEvent AS me ON me._id = btcre2.MonthId " +
                        " WHERE me.MonthId = '" + monthId + "'" +
                        " GROUP BY btcre2.AuthorizationCode " +
                        " HAVING COUNT(*) = 1"
        );
    }

    /**
     * Все успешные и целиком завершенные транзакции оплаты
     *
     * @param monthId месяц выборки
     * @return список транзакций
     */
    public List<BankTransactionEvent> getSuccessfulCompletedTransactions(String monthId) {
        return rawQuery(
                "SELECT * FROM (SELECT btcre.* FROM BankTransactionCashRegisterEvent AS btcre " +
                        "INNER JOIN MonthEvent AS me ON me._id = btcre.MonthId " +
                        "WHERE me.MonthId = '" + monthId + "' AND OperationResult = " + BankOperationResult.Approved.getCode()
                        + " AND btcre.Status <> " + BankTransactionEvent.Status.STARTED.getCode() +
                        " GROUP BY AuthorizationCode " +
                        "HAVING COUNT(*) = 1) " +
                        "WHERE OperationType = " + BankOperationType.SALE.getCode()
        );
    }

    private List<BankTransactionEvent> rawQuery(final String query) {
        final List<BankTransactionEvent> successfulTransactions = new ArrayList<>();

        System.out.println(query);

        Cursor cursor = db().rawQuery(query, null);

        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    BankTransactionEvent item = fromCursor(cursor);
                    successfulTransactions.add(item);

                }
            }
        } finally {
            cursor.close();
        }

        return successfulTransactions;
    }

    public int getTransactionCount(@NonNull String shiftId) {
        int count = 0;

        QueryBuilder qb = new QueryBuilder();
        qb.select().count("*").from(BankTransactionDao.TABLE_NAME);
        qb.innerJoin(ShiftEventDao.TABLE_NAME).on();
        qb.field(Properties.CashRegisterWorkingShiftId).eq().field(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
        qb.where().field(ShiftEventDao.TABLE_NAME, ShiftEventDao.Properties.ShiftId).eq(shiftId);
        qb.and().field(BankTransactionDao.TABLE_NAME, Properties.Status).notEq(BankTransactionEvent.Status.STARTED.getCode());
        qb.and().field(BankTransactionDao.TABLE_NAME, Properties.OperationResult).eq(BankOperationResult.Approved.getCode());

        Query query = qb.build();
        query.logQuery();
        Cursor cursor = query.run(db());

        try {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }

        return count;
    }

    /**
     * Возвращает timestamp создания последнего события банковской транзакции
     *
     * @return
     */
    public long getLastBankTransactionEventCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * формируем запрос
         *
         * SELECT max(CreationTimestamp) FROM BankTransactionCashRegisterEvent
         * JOIN Event ON BankTransactionCashRegisterEvent.EventId = Event._id
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(EventDao.Properties.CreationTimestamp).append(") FROM ").append(BankTransactionDao.TABLE_NAME);
        sql.append(" JOIN ").append(EventDao.TABLE_NAME);
        sql.append(" ON ").append(Properties.EventId).append(" = ").append(getLocalDaoSession().getEventDao().getIdWithTableName());

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
