package ru.ppr.cppk.localdb.repository.base;

import java.util.concurrent.Callable;

/**
 * Транзакция локальной БД.
 *
 * @author Aleksandr Brazhkin
 */
public interface LocalDbTransaction {
    void begin();

    void end();

    void commit();

    void runInTx(Runnable runnable);

    <V> V runInTx(Callable<V> callable);
}
