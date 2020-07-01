package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;

/**
 * DAO для таблицы НСИ <i>RegionCalendars</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class RegionCalendarDao extends BaseEntityDao<String, Integer> {

    private static final String TAG = Logger.makeLogTag(RegionCalendarDao.class);

    public static final String TABLE_NAME = "RegionCalendars";

    public static class Properties {
        public static final String Year = "Year";
        public static final String Date = "Date";
        public static final String RegionCode = "RegionCode";
        public static final String DayType = "DayType";
        public static final String VersionId = "VersionId";
        public static final String DeleteInVersionId = "DeleteInVersionId";
    }

    public RegionCalendarDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
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
