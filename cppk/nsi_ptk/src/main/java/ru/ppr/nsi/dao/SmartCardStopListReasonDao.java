package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;

/**
 * DAO для таблицы НСИ <i>SmartCardStopListReasons</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class SmartCardStopListReasonDao extends BaseEntityDao<String, Integer> {

    public static final String TABLE_NAME = "SmartCardStopListReasons";

    public static class Properties {
        public static final String Name = "Name";
    }

    public SmartCardStopListReasonDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
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
