package ru.ppr.cppk.db.local.repository.base;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import ru.ppr.cppk.db.local.LocalDbSessionManager;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;

/**
 * Транзакция локальной БД.
 *
 * @author Aleksandr Brazhkin
 */
public class LocalDbTransactionImpl implements LocalDbTransaction {

    private final LocalDbSessionManager localDbSessionManager;

    @Inject
    LocalDbTransactionImpl(LocalDbSessionManager localDbSessionManager) {
        this.localDbSessionManager = localDbSessionManager;
    }

    /**
     * Начало транзакции.
     */
    @Override
    public void begin() {
        localDbSessionManager.getDaoSession().getLocalDb().beginTransaction();
    }

    /**
     * Завершение транзакции
     */
    @Override
    public void end() {
        localDbSessionManager.getDaoSession().getLocalDb().endTransaction();
    }

    /**
     * Применение изменений
     */
    @Override
    public void commit() {
        localDbSessionManager.getDaoSession().getLocalDb().setTransactionSuccessful();
    }

    @Override
    public void runInTx(Runnable runnable) {
        begin();
        try {
            runnable.run();
            commit();
        } finally {
            end();
        }
    }

    @Override
    public <V> V runInTx(Callable<V> callable) {
        begin();
        try {
            V result = callable.call();
            commit();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            end();
        }
    }

}
