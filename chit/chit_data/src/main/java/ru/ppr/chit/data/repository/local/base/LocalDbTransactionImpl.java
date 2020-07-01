package ru.ppr.chit.data.repository.local.base;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;

/**
 * Транзакция локальной БД.
 *
 * @author Aleksandr Brazhkin
 */
public class LocalDbTransactionImpl implements LocalDbTransaction {

    private final LocalDbManager localDbManager;

    @Inject
    LocalDbTransactionImpl(LocalDbManager localDbManager) {
        this.localDbManager = localDbManager;
    }

    /**
     * Начало транзакции. Бросает исключение, если транзакция уже начата
     */
    @Override
    public void begin() {
        localDbManager.daoSession().getDatabase().beginTransaction();
    }

    /**
     * Завершение транзакции
     */
    @Override
    public void end() {
        localDbManager.daoSession().getDatabase().endTransaction();
    }

    /**
     * Применение изменений
     */
    @Override
    public void commit() {
        localDbManager.daoSession().getDatabase().setTransactionSuccessful();
    }

}
