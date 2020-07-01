package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDaoSession;

/**
 * DAO для таблицы НСИ <i>TicketTypesToDeviceTypes</i>.
 * Таблица запретов (если есть запись в таблице - значит продажа запрещена)
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTypeToDeviceTypeDao extends BaseEntityDao<String, Integer> {

    public static final String TABLE_NAME = "TicketTypesToDeviceTypes";

    public static class Properties {
        public static final String DeviceTypeCode = "DeviceTypeCode";
        public static final String TicketTypeCode = "TicketTypeCode";
        public static final String VersionId = "VersionId";
        public static final String DeleteInVersionId = "DeleteInVersionId";
    }

    public TicketTypeToDeviceTypeDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
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
     * Проверяет возможность продажи типа ПД на определенном устройстве
     *
     * @param ticketTypeCode
     * @param deviceTypeCode
     * @return
     */
    public boolean canTicketTypeBeSoldOnDeviceType(long ticketTypeCode, long deviceTypeCode, int versionId) {
        boolean out = false;

        QueryBuilder qb = new QueryBuilder();
        qb.select().count("*").from(TABLE_NAME).where();
        qb.appendRaw(checkVersion(TABLE_NAME, versionId));
        qb.and().field(Properties.TicketTypeCode).eq(ticketTypeCode);
        qb.and().field(Properties.DeviceTypeCode).eq(deviceTypeCode);

        Query query = qb.build();
        Cursor cursor = null;

        try {
            cursor = query.run(db());
            out = cursor.moveToFirst() && cursor.getInt(0) == 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return out;
    }

}
