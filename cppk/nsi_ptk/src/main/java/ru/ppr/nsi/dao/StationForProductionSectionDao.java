package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;

/**
 * DAO для таблицы НСИ <i>StationsForProductionSections</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class StationForProductionSectionDao extends BaseEntityDao<String, Integer> {

    public static final String TABLE_NAME = "StationsForProductionSections";

    public static class Properties {
        public static final String StationCode = "StationCode";
        public static final String ProductionSectionCode = "ProductionSectionCode";
    }

    public StationForProductionSectionDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
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
