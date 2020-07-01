package ru.ppr.ipos.stub.db;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import ru.ppr.database.Database;
import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.ipos.model.PosDay;
import ru.ppr.ipos.model.PosProperty;
import ru.ppr.ipos.model.Transaction;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Vinogradov
 */
public class PosDbStorage implements PosStorage {

    private static final String TAG = Logger.makeLogTag(PosDbStorage.class);

    private final PosStubDbManager posStubDbManager;

    public PosDbStorage(Context context) {
        this.posStubDbManager = new PosStubDbManager(context);
    }

    private PosStubDaoSession daoSession() {
        PosStubDaoSession ds = posStubDbManager.daoSession();
        if (ds == null) {
            Logger.trace(TAG, "daoSession == null");
        }
        return ds;
    }

    /**
     * Открытие и закрытие соединения с БД
     */
    @Override
    public void openConnection() {
        posStubDbManager.openConnection();
    }

    @Override
    public void closeConnection() {
        posStubDbManager.closeConnection();
    }

    /**
     * Методы работы с таблицей PosProperty
     */
    private String getValueOfPosPropertyForKey(String key) {
        String value = null;
        if (posStubDbManager != null) {
            PosPropertyDao posPropertyDao = daoSession().getPosPropertyDao();
            QueryBuilder qb = new QueryBuilder();
            qb.selectAll().from(posPropertyDao.TABLE_NAME).where().field(PosPropertyDao.Properties.Key).eq(key);
            Database db = posPropertyDao.db();

            Query query = qb.build();
            query.logQuery();
            Cursor cursor = query.run(db);
            try {
                if (cursor.moveToFirst()) {
                    value = cursor.getString(1);
                }
            } finally {
                cursor.close();
            }
        }
        return value;
    }

    @Override
    public int getLastLocalSaleTransactionId() {

        String saleTransactionIdValue = getValueOfPosPropertyForKey(PosProperty.Keys.SaleTransactionId);
        int lastSaleTransactionId = -1;
        try {
            lastSaleTransactionId = Integer.parseInt(saleTransactionIdValue);
        } catch (NumberFormatException e) {
            Logger.error(TAG, e);
        }
        return lastSaleTransactionId;
    }

    @Override
    public void setLastLocalSaleTransactionId(@NonNull PosProperty entity) {

        PosPropertyDao posPropertyDao = daoSession().getPosPropertyDao();
        int lastSaleTransactionId = getLastLocalSaleTransactionId();
        if (lastSaleTransactionId == -1) {
            posPropertyDao.insertOrThrow(entity);
        } else {
            posPropertyDao.update(entity);
        }
    }

    /**
     * Методы работы с таблицей Transaction
     */
    @Override
    public Transaction getLastTransaction(Transaction.TransactionType bankOperationType) {

        if (posStubDbManager != null) {
            TransactionDao transactionDao = daoSession().getTransactionDao();
            QueryBuilder qb = new QueryBuilder();
            qb.selectAll().from(transactionDao.TABLE_NAME);
            if (bankOperationType != Transaction.TransactionType.ALL) {
                qb.where()
                        .field(TransactionDao.Properties.BankOperationType)
                        .eq(bankOperationType.getCode());
            }

            qb.orderBy(TransactionDao.Properties.Id)
                    .desc()
                    .limit(1);

            Database db = transactionDao.db();

            Query query = qb.build();
            query.logQuery();
            Cursor cursor = query.run(db);

            try {
                if (cursor.moveToFirst()) {
                    return transactionDao.fromCursor(cursor);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    @Override
    public int getMaxTransactionId() {

        if (posStubDbManager != null) {
            TransactionDao transactionDao = daoSession().getTransactionDao();
            QueryBuilder qb = new QueryBuilder();
            qb.select().max(TransactionDao.Properties.TransactionId).from(transactionDao.TABLE_NAME);

            Database db = transactionDao.db();

            Query query = qb.build();
            query.logQuery();
            Cursor cursor = query.run(db);
            try {
                if (cursor.moveToFirst()) {
                    int maxTransactionId = cursor.getInt(0);
                    return maxTransactionId;
                }
            } finally {
                cursor.close();
            }
        }
        return -1;
    }

    @Override
    public Transaction getTransaction(int transactionId, Transaction.TransactionType bankOperationType) {

        if (posStubDbManager != null) {
            TransactionDao transactionDao = daoSession().getTransactionDao();
            QueryBuilder qb = new QueryBuilder();
            qb.selectAll().from(transactionDao.TABLE_NAME)
                    .where()
                    .field(TransactionDao.Properties.BankOperationType)
                    .eq(bankOperationType.getCode())
                    .and()
                    .field(TransactionDao.Properties.TransactionId).eq(transactionId);
            Database db = transactionDao.db();

            Query query = qb.build();
            query.logQuery();
            Cursor cursor = query.run(db);
            try {
                if (cursor.moveToFirst()) {
                    return transactionDao.fromCursor(cursor);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    @Override
    public void saveNewTransaction(@NonNull Transaction entity) {
        if (posStubDbManager != null) {
            TransactionDao transactionDao = daoSession().getTransactionDao();
            transactionDao.insertOrThrow(entity);
        }
    }

    /**
     * Методы работы с таблицей PosDay
     */
    @Override
    public PosDay getLastPosDay() {
        if (posStubDbManager != null) {
            PosDayDao posDayDao = daoSession().getPosDayDao();
            QueryBuilder qb = new QueryBuilder();
            qb.selectAll().from(PosDayDao.TABLE_NAME)
                    .orderBy(PosDayDao.Properties.Id)
                    .desc()
                    .limit(1);
            Database db = posDayDao.db();

            Query query = qb.build();
            query.logQuery();
            Cursor cursor = query.run(db);
            try {
                if (cursor.moveToFirst()) {
                    return posDayDao.fromCursor(cursor);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    @Override
    public void saveNewPosDay(@NonNull PosDay entity) {
        if (posStubDbManager != null) {
            PosDayDao posDayDao = daoSession().getPosDayDao();
            posDayDao.insertOrThrow(entity);
        }
    }

    private boolean posDayExists(@NonNull PosDay entity) {
        int entityPosDayId = entity.getId();
        int posDayId = -1;

        if (posStubDbManager != null) {
            PosDayDao posDayDao = daoSession().getPosDayDao();

            QueryBuilder qb = new QueryBuilder();
            qb.select().field(PosDayDao.Properties.Id)
                    .from(PosDayDao.TABLE_NAME)
                    .where()
                    .field(PosDayDao.Properties.Id)
                    .eq(String.valueOf(entityPosDayId));
            Database db = posDayDao.db();

            Query query = qb.build();
            query.logQuery();
            Cursor cursor = query.run(db);
            try {
                if (cursor.moveToFirst()) {
                    posDayId = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        boolean posDayExists = true;

        if (posDayId == -1)
            posDayExists = false;

        return posDayExists;
    }

    @Override
    public void updatePosDay(@NonNull PosDay entity) {
        if (posStubDbManager != null) {
            PosDayDao posDayDao = daoSession().getPosDayDao();
            boolean posDayExists = posDayExists(entity);
            if (posDayExists) {
                posDayDao.update(entity);
            }
        }
    }
}
