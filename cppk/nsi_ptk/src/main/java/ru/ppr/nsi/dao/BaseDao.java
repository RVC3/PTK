package ru.ppr.nsi.dao;

import ru.ppr.database.Database;
import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;

/**
 * Базовый DAO для доступа к данным НСИ.
 *
 * @author Aleksandr Brazhkin
 */
public class BaseDao {

    /**
     * Сессия для доступа к остальным DAO.
     */
    private final NsiDaoSession nsiDaoSession;
    /**
     * Кеш
     */
    private final QueryCache queryCache;

    BaseDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        this.nsiDaoSession = nsiDaoSession;
        this.queryCache = queryCache;
    }

    protected NsiDaoSession getNsiDaoSession() {
        return nsiDaoSession;
    }

    protected QueryCache cache() {
        return queryCache;
    }

    /**
     * Возвращает базу НСИ.
     *
     * @return База НСИ
     */
    protected Database db() {
        return getNsiDaoSession().getNsiDb();
    }

    /**
     * Возвращает условие SQL-запроса, используемое для выборки данных из любой таблицы НСИ
     *
     * @param tableName Наименование таблицы
     * @param version   Версия НСИ
     * @return Условие SQL-запроса
     */
    protected String checkVersion(String tableName, int version) {
        String query = tableName + "." + BaseEntityDao.Properties.VersionId + " <= " + version +
                " AND " +
                "(" +
                tableName + "." + BaseEntityDao.Properties.DeleteInVersionId + " > " + version +
                " OR " +
                tableName + "." + BaseEntityDao.Properties.DeleteInVersionId + " IS NULL" +
                ")";
        return query;
    }

    interface Operation<R> {
        R call();
    }
}
