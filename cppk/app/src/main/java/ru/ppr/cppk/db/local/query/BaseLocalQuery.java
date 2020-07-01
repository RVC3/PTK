package ru.ppr.cppk.db.local.query;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.database.Database;

/**
 * Базовый класс для SQL-запроса.
 *
 * @author Aleksandr Brazhkin
 */
public class BaseLocalQuery {

    /**
     * Сессия для доступа к остальным DAO.
     */
    private final LocalDaoSession localDaoSession;

    BaseLocalQuery(LocalDaoSession localDaoSession) {
        this.localDaoSession = localDaoSession;
    }

    LocalDaoSession localDaoSession() {
        return localDaoSession;
    }

    /**
     * Возвращает базу НСИ.
     *
     * @return База НСИ
     */
    Database db() {
        return localDaoSession().getLocalDb();
    }
}
