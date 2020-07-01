package ru.ppr.ipos.stub.db;

import ru.ppr.database.Database;

/**
 * @author Dmitry Vinogradov
 */
public class PosStubDaoSession {

    /**
     * Локальная БД
     */
    private final Database db;

    private final TransactionDao transactionDao;
    private final PosDayDao posDayDao;
    private final PosPropertyDao posPropertyDao;

    public PosStubDaoSession(Database db) {
        this.db = db;

        transactionDao = new TransactionDao(this);
        posDayDao = new PosDayDao(this);
        posPropertyDao = new PosPropertyDao(this);
    }

    /**
     * Возвращает локальную БД
     *
     * @return Локальная БД
     */
    public Database getLocalDb() {
        return db;
    }

    public void beginTransaction() {
        db.beginTransaction();
    }

    public void endTransaction() {
        db.endTransaction();
    }

    public void setTransactionSuccessful() {
        db.setTransactionSuccessful();
    }

    public PosDayDao getPosDayDao() {
        return posDayDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public PosPropertyDao getPosPropertyDao() {
        return posPropertyDao;
    }
}
