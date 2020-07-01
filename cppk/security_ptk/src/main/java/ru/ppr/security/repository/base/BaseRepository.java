package ru.ppr.security.repository.base;

import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.SecurityDbSessionManager;
import ru.ppr.security.dao.BaseEntityDao;

/**
 * Базовый репозиторий для таблиц Security.
 *
 * @param <T> Тип сущности, ассоциированой с таблицей Security.
 * @param <K> Тип ключа (Primary Key) для таблицы Security.
 * @author Aleksandr Brazhkin
 */
public abstract class BaseRepository<T, K> {

    private final SecurityDbSessionManager securityDbSessionManager;

    public BaseRepository(SecurityDbSessionManager securityDbSessionManager) {
        this.securityDbSessionManager = securityDbSessionManager;
    }

    protected abstract BaseEntityDao<T, K> selfDao();

    protected SecurityDaoSession daoSession() {
        return securityDbSessionManager.getDaoSession();
    }

}
