package ru.ppr.cppk.db.local;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.database.Database;

/**
 * Базовый DAO для доступа к данным локальной БД.
 *
 * @author Aleksandr Brazhkin
 */
public class BaseDao {

    private final LocalDaoSession localDaoSession;

    public BaseDao(LocalDaoSession localDaoSession) {
        this.localDaoSession = localDaoSession;
    }

    protected LocalDaoSession getLocalDaoSession() {
        return localDaoSession;
    }

    protected Database db() {
        return getLocalDaoSession().getLocalDb();
    }
}
