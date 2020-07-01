package ru.ppr.cppk.db.local.repository.base;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseDao;
import ru.ppr.cppk.db.local.LocalDbSessionManager;
import ru.ppr.cppk.localdb.repository.base.LocalDbRepository;
import ru.ppr.database.Database;

/**
 * Репозиторий для доступа к данным локальной БД.
 *
 * @param <M> Тип модели слоя логики
 * @author Aleksandr Brazhkin
 */
public abstract class BaseLocalDbRepository<M> implements LocalDbRepository {

    private final LocalDbSessionManager localDbSessionManager;

    public BaseLocalDbRepository(LocalDbSessionManager localDbSessionManager) {
        this.localDbSessionManager = localDbSessionManager;
    }

    protected abstract BaseDao dao();

    protected LocalDaoSession daoSession() {
        return localDbSessionManager.getDaoSession();
    }

    protected Database db() {
        return daoSession().getLocalDb();
    }

    protected void beginTransaction() {
        localDbSessionManager.getDaoSession().getLocalDb().beginTransaction();
    }

    protected void endTransaction() {
        localDbSessionManager.getDaoSession().getLocalDb().endTransaction();
    }

    protected void setTransactionSuccessful() {
        localDbSessionManager.getDaoSession().getLocalDb().setTransactionSuccessful();
    }

}
