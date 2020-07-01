package ru.ppr.nsi;

import ru.ppr.nsi.dao.BaseEntityDao;

/**
 * @author Aleksandr Brazhkin
 */
public class NsiUtils {
    /**
     * Возвращает условие SQL-запроса, используемое для выборки данных из любой таблицы НСИ
     *
     * @param tableName Наименование таблицы
     * @param version   Версия НСИ
     * @return Условие SQL-запроса
     */
    public static String checkVersion(String tableName, int version) {
        String query = tableName + "." + BaseEntityDao.Properties.VersionId + " <= " + version +
                " AND " +
                "(" +
                tableName + "." + BaseEntityDao.Properties.DeleteInVersionId + " > " + version +
                " OR " +
                tableName + "." + BaseEntityDao.Properties.DeleteInVersionId + " IS NULL" +
                ")";
        return query;
    }
}
