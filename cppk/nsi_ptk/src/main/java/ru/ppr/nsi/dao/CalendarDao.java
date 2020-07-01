package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;

/**
 * DAO для таблицы НСИ <i>Calendars</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class CalendarDao extends BaseEntityDao<String, Integer> {

    private static final String TAG = Logger.makeLogTag(CalendarDao.class);

    public static final String TABLE_NAME = "Calendars";

    public static class Properties {
        public static final String Year = "Year";
        public static final String Date = "Date";
        public static final String TypeOfDate = "TypeOfDate";
        public static final String VersionId = "VersionId";
        public static final String DeleteInVersionId = "DeleteInVersionId";
    }

    public CalendarDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
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
