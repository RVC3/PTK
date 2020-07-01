package ru.ppr.security.dao;

import ru.ppr.database.Database;
import ru.ppr.security.SecurityDaoSession;

/**
 * Базовый DAO для доступа к данным базы безопасности.
 *
 * @author Aleksandr Brazhkin
 */
public class BaseDao {

    /**
     * Сессия для доступа к остальным DAO.
     */
    private final SecurityDaoSession securityDaoSession;

    public BaseDao(SecurityDaoSession securityDaoSession) {
        this.securityDaoSession = securityDaoSession;
    }

    protected SecurityDaoSession getSecurityDaoSession() {
        return securityDaoSession;
    }

    /**
     * Возвращает базу безопасности.
     *
     * @return База безопасности
     */
    protected Database db() {
        return getSecurityDaoSession().getSecurityDb();
    }
}
