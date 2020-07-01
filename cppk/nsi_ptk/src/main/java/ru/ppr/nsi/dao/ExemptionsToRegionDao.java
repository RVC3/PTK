package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;

/**
 * DAO для таблицы НСИ <i>ExemptionsToRegions</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ExemptionsToRegionDao extends BaseEntityDao<String, Integer> {

    public static final String TABLE_NAME = "ExemptionsToRegions";

    public static class Properties {
        public static final String RegioneCode = "RegionCode";
        public static final String ExemptionCode = "ExemptionCode";
    }

    public ExemptionsToRegionDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
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
}
