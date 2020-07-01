package ru.ppr.nsi.query;

import ru.ppr.database.Database;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.BaseEntityDao;

/**
 * Базовый класс для SQL-запроса.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseNsiQuery {

    /**
     * Сессия для доступа к остальным DAO.
     */
    private final NsiDaoSession nsiDaoSession;

    protected BaseNsiQuery(NsiDaoSession nsiDaoSession) {
        this.nsiDaoSession = nsiDaoSession;
    }

    NsiDaoSession nsiDaoSession() {
        return nsiDaoSession;
    }

    /**
     * Возвращает базу НСИ.
     *
     * @return База НСИ
     */
    protected Database db() {
        return nsiDaoSession().getNsiDb();
    }

    /**
     * Возвращает условие SQL-запроса, используемое для выборки данных из любой таблицы НСИ
     *
     * @param tableName Наименование таблицы
     * @param version   Версия НСИ
     * @return Условие SQL-запроса
     */
    protected String checkVersion(String tableName, int version) {
        return tableName + "." + BaseEntityDao.Properties.VersionId + " <= " + version +
                " AND " +
                "(" +
                tableName + "." + BaseEntityDao.Properties.DeleteInVersionId + " > " + version +
                " OR " +
                tableName + "." + BaseEntityDao.Properties.DeleteInVersionId + " IS NULL" +
                ")";
    }

    /**
     * Возвращает условие SQL-запроса, используемое для выборки данных из любой таблицы НСИ
     *
     * @param version Версия НСИ
     * @return Условие SQL-запроса
     */
    String checkVersion(int version) {
        return BaseEntityDao.Properties.VersionId + " <= " + version +
                " AND " +
                "(" +
                BaseEntityDao.Properties.DeleteInVersionId + " > " + version +
                " OR " +
                BaseEntityDao.Properties.DeleteInVersionId + " IS NULL" +
                ")";
    }
}
