package ru.ppr.nsi.dao;

import android.database.Cursor;
import android.support.annotation.NonNull;

import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * DAO для таблицы НСИ <i>TicketStorageTypes</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketStorageTypeDao extends BaseEntityDao<String, Integer> {

    public static final String TABLE_NAME = "TicketStorageTypes";

    public static class Properties {
        public static final String Name = "Name";
        public static final String Description = "Description";
        public static final String Abbreviation = "Abbreviation";
    }

    public TicketStorageTypeDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
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
     * Получает Имя по типу из базы НСИ
     *
     * @param ticketStorageType
     * @param versionId
     * @return
     */
    public String getName(@NonNull TicketStorageType ticketStorageType, int versionId) {
        String name = null;
        QueryBuilder qb = new QueryBuilder();
        qb.select().field(Properties.Name).from(TABLE_NAME).where().field(BaseEntityDao.Properties.Code).eq(ticketStorageType.getDBCode());
        qb.and();
        qb.appendRaw(checkVersion(TABLE_NAME, versionId));
        Query query = qb.build();
        Cursor cursor = query.run(db());

        try {
            if (cursor.moveToFirst())
                name = cursor.getString(0);
        } finally {
            cursor.close();
        }

        return name;
    }

}
