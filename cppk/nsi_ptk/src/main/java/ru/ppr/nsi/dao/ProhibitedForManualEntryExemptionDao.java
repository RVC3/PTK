package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;

/**
 * DAO для таблицы НСИ <i>ProhibitedForManualEntryExemptions</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ProhibitedForManualEntryExemptionDao extends BaseEntityDao<String, Integer> {

    public static final String TABLE_NAME = "ProhibitedForManualEntryExemptions";

    public static class Properties {
        public static final String ExemptionCode = "ExemptionCode";
        public static final String RegionCode = "RegionCode";
        public static final String RegionOkatoCode = "RegionOkatoCode";
    }

    public ProhibitedForManualEntryExemptionDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String fromCursor(Cursor cursor) {

        return null;
    }

    /**
     * Проверяет, допутсимо ли использовать ручной ввод для указанной льготы
     *
     * @param exemption
     * @param regionCode
     * @param versionId
     * @return
     */
    public boolean isCantBeEnteredManually(Exemption exemption, int regionCode, int versionId) {

        final String query = "select count(*) as count from " + TABLE_NAME + " where " +
                Properties.ExemptionCode + " = " + exemption.getExemptionExpressCode() +
                " AND " + Properties.RegionOkatoCode +
                (exemption.getRegionOkatoCode() == null ? " is null" : " = " + exemption.getRegionOkatoCode()) +
                " AND (" + Properties.RegionCode + " = " + regionCode +
                " OR " + Properties.RegionCode + " IS NULL)" +
                " AND " + checkVersion(TABLE_NAME, versionId);

        Cursor cursor = null;
        boolean result;
        try {
            cursor = db().rawQuery(query, null);

            result = cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndex("count")) > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }
}
