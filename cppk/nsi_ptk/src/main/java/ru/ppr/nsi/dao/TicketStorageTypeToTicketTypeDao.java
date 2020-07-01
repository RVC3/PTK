package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;

/**
 * DAO для таблицы НСИ <i>TicketStorageTypesToTicketTypes</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketStorageTypeToTicketTypeDao extends BaseEntityDao<String, Integer> {

    public static final String TABLE_NAME = "TicketStorageTypesToTicketTypes";

    public static class Properties {
        public static final String TicketStorageTypeCode = "TicketStorageTypeCode";
        public static final String TicketTypeCode = "TicketTypeCode";
        public static final String VersionId = "VersionId";
        public static final String DeleteInVersionId = "DeleteInVersionId";
    }

    public TicketStorageTypeToTicketTypeDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
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
