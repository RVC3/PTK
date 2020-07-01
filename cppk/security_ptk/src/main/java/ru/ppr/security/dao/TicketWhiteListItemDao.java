package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.TicketId;

/**
 * DAO для таблицы базы безопасности <i>TicketWhitelistItem</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketWhiteListItemDao extends BaseEntityDao<String, Long> {

    public static final String TABLE_NAME = "TicketWhitelistItem";

    public static class Properties {
        public static final String TICKET_ID = "TicketId";
    }

    public TicketWhiteListItemDao(SecurityDaoSession securityDaoSession) {
        super(securityDaoSession);
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
     * Проверяет присутствует ли билет с таким id в белом листе
     */
    public boolean isTicketInWhiteList(TicketId ticketId) {
        int out = 0;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select count(*) from " + TicketWhiteListItemDao.TABLE_NAME + " where ")
                .append(TicketWhiteListItemDao.Properties.TICKET_ID + "='")
                .append(ticketId.getString()).append("'");

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                out = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return out > 0;
    }
}