package ru.ppr.ipos.stub.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.database.Database;
import ru.ppr.ipos.model.Transaction;

/**
 * @author Dmitry Vinogradov
 */
public class TransactionDao {

    private final PosStubDaoSession localDaoSession;

    public static final String TABLE_NAME = "BankTransaction";

    public static class Properties {
        public static final String Id = "_id";
        public static final String TimeStamp = "TimeStamp";
        public static final String Approved = "Approved";
        public static final String TerminalId = "TerminalId";
        public static final String InvoiceNumber = "InvoiceNumber";
        public static final String BankResponseCode = "BankResponseCode";
        public static final String BankResponse = "BankResponse";
        public static final String Amount = "Amount";
        public static final String CardPAN = "CardPAN";
        public static final String RRN = "RRN";
        public static final String MerchantId = "MerchantId";
        public static final String AuthorizationId = "AuthorizationId";
        public static final String IssuerName = "IssuerName";
        public static final String CurrencyCode = "CurrencyCode";
        public static final String ApplicationName = "ApplicationName";
        public static final String DayId = "DayId";
        public static final String BankOperationType = "BankOperationType";
        public static final String TransactionId = "TransactionId";
    }

    public TransactionDao(@NonNull PosStubDaoSession localDaoSession) {
        this.localDaoSession = localDaoSession;
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public Transaction fromCursor(@NonNull final Cursor cursor) {
        Transaction transactionResult = new Transaction();
        transactionResult.setId(cursor.getInt(cursor.getColumnIndex(Properties.Id)));
        transactionResult.setTimeStamp(new Date((cursor.getLong(cursor.getColumnIndex(Properties.TimeStamp)))));
        transactionResult.setApproved(cursor.getInt(cursor.getColumnIndex(Properties.Approved)) != 0);
        transactionResult.setTerminalId(cursor.getString(cursor.getColumnIndex(Properties.TerminalId)));
        transactionResult.setInvoiceNumber(cursor.getInt(cursor.getColumnIndex(Properties.InvoiceNumber)));
        transactionResult.setBankResponseCode(cursor.getInt(cursor.getColumnIndex(Properties.BankResponseCode)));
        transactionResult.setBankResponse(cursor.getString(cursor.getColumnIndex(Properties.BankResponse)));
        transactionResult.setAmount(cursor.getInt(cursor.getColumnIndex(Properties.Amount)));
        transactionResult.setCardPAN(cursor.getString(cursor.getColumnIndex(Properties.CardPAN)));
        transactionResult.setRRN(cursor.getString(cursor.getColumnIndex(Properties.RRN)));
        transactionResult.setMerchantId(cursor.getString(cursor.getColumnIndex(Properties.MerchantId)));
        transactionResult.setAuthorizationId(cursor.getString(cursor.getColumnIndex(Properties.AuthorizationId)));
        transactionResult.setIssuerName(cursor.getString(cursor.getColumnIndex(Properties.IssuerName)));
        transactionResult.setCurrencyCode(cursor.getString(cursor.getColumnIndex(Properties.CurrencyCode)));
        transactionResult.setApplicationName(cursor.getString(cursor.getColumnIndex(Properties.ApplicationName)));
        transactionResult.setDayId(cursor.getLong(cursor.getColumnIndex(Properties.DayId)));
        transactionResult.setBankOperationType(Transaction.TransactionType.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.BankOperationType))));
        transactionResult.setTransactionId(cursor.getInt(cursor.getColumnIndex(Properties.TransactionId)));
        return transactionResult;
    }

    public ContentValues toContentValues(@NonNull final Transaction entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.TimeStamp, entity.getTimeStamp().getTime());
        contentValues.put(Properties.Approved, entity.isApproved());
        contentValues.put(Properties.TerminalId, entity.getTerminalId());
        contentValues.put(Properties.InvoiceNumber, entity.getInvoiceNumber());
        contentValues.put(Properties.BankResponseCode, entity.getBankResponseCode());
        contentValues.put(Properties.BankResponse, entity.getBankResponse());
        contentValues.put(Properties.Amount, entity.getAmount());
        contentValues.put(Properties.CardPAN, entity.getCardPAN());
        contentValues.put(Properties.RRN, entity.getRRN());
        contentValues.put(Properties.MerchantId, entity.getMerchantId());
        contentValues.put(Properties.AuthorizationId, entity.getAuthorizationId());
        contentValues.put(Properties.IssuerName, entity.getIssuerName());
        contentValues.put(Properties.CurrencyCode, entity.getCurrencyCode());
        contentValues.put(Properties.ApplicationName, entity.getApplicationName());
        contentValues.put(Properties.DayId, entity.getDayId());
        contentValues.put(Properties.BankOperationType, entity.getBankOperationType().getCode());
        contentValues.put(Properties.TransactionId, entity.getTransactionId());
        return contentValues;
    }

    public int getKey(Transaction entity) {
        return entity.getId();
    }

    protected PosStubDaoSession getLocalDaoSession() {
        return localDaoSession;
    }

    protected Database db() {
        return getLocalDaoSession().getLocalDb();
    }

    /**
     * Возвращает наименование колонки id с именем таблицы.
     *
     * @return Наименование колонки
     */
    public String getIdWithTableName() {
        return getTableName() + "." + Properties.Id;
    }

    /**
     * Возвращает наименование колонки PK.
     *
     * @return Наименование колонки
     */
    protected String getPKColumnName() {
        return Properties.Id;
    }


    /**
     * Обёртка над {@link #fromCursor} для Greendao
     */
    public Transaction readEntity(Cursor cursor, int offset) {
        return fromCursor(cursor);
    }

    /**
     * Получает запись таблицы локальной БД по коду (PrimaryKey).
     *
     * @param id Идентификатор
     * @return Сущность с указанным кодом
     */
    public Transaction load(int id) {

        StringBuilder stringBuilder = new StringBuilder();

        List<String> selectionArgsList = new ArrayList<>();

        stringBuilder.append("SELECT ");
        stringBuilder.append(" * ");
        stringBuilder.append(" FROM ");
        stringBuilder.append(getTableName());
        stringBuilder.append(" WHERE ");
        stringBuilder.append(getPKColumnName()).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(id));

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Cursor cursor = null;
        Transaction entity = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgs);
            if (cursor.moveToFirst()) {
                entity = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return entity;
    }

    /**
     * Выполняет добавление сущности в БД по идентификатору.
     *
     * @param entity Сущность
     * @return row_id
     */
    public long insertOrThrow(@NonNull Transaction entity) {
        ContentValues contentValues = toContentValues(entity);
        return db().insertOrThrow(getTableName(), null, contentValues);
    }

    /**
     * Выполняет обновление сущности в БД по идентификатору.
     *
     * @param entity Сущность
     */
    public void update(@NonNull Transaction entity) {
        String whereClause = getPKColumnName() + " = " + "?";
        String[] whereArgs = new String[]{String.valueOf(getKey(entity))};

        ContentValues contentValues = toContentValues(entity);
        db().update(getTableName(), contentValues, whereClause, whereArgs);
    }

    /**
     * Выполняет удаление сущности в БД по идентификатору.
     *
     * @param entity Сущность
     */
    public void delete(@NonNull Transaction entity) {
        String whereClause = getPKColumnName() + " = " + "?";
        String[] whereArgs = new String[]{String.valueOf(getKey(entity))};

        db().delete(getTableName(), whereClause, whereArgs);
    }

    /**
     * Выполняет запрос к таблице.
     *
     * @param columns       Запрашиваемые имена колонок
     * @param selection     Условие выбора
     * @param selectionArgs Аргументы для условия выбора
     * @param orderBy       Тип сортировки
     * @return Курсор с данными
     */
    public Cursor query(String[] columns, String selection, String[] selectionArgs, String orderBy) {
        return db().query(getTableName(), columns, selection, selectionArgs, null, null, orderBy);
    }

    public static void createTable(Database db, boolean ifNoExist) {

        String constraint = ifNoExist ? "IF NOT EXISTS " : "";
        String builder = "CREATE TABLE " + constraint + "\"" + TABLE_NAME + "\" (" +
                Properties.Id + " INTEGER PRIMARY KEY, " +
                Properties.TimeStamp + " INTEGER NOT NULL, " +
                Properties.Approved + " INTEGER NOT NULL, " +
                Properties.TerminalId + " TEXT NOT NULL, " +
                Properties.InvoiceNumber + " INTEGER NOT NULL, " +
                Properties.BankResponseCode + " INTEGER NOT NULL, " +
                Properties.BankResponse + " TEXT NOT NULL, " +
                Properties.Amount + " INTEGER NOT NULL, " +
                Properties.CardPAN + " TEXT NOT NULL, " +
                Properties.RRN + " TEXT NOT NULL, " +
                Properties.MerchantId + " TEXT NOT NULL, " +
                Properties.AuthorizationId + " TEXT NOT NULL, " +
                Properties.IssuerName + " TEXT NOT NULL, " +
                Properties.CurrencyCode + " TEXT NOT NULL, " +
                Properties.ApplicationName + " TEXT NOT NULL, " +
                Properties.DayId + " INTEGER NOT NULL, " +
                Properties.BankOperationType + " INTEGER NOT NULL, " +
                Properties.TransactionId + " INTEGER NOT NULL)";
        db.execSQL(builder);
    }

    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"" + TABLE_NAME + "\"";
        db.execSQL(sql);
    }
}
