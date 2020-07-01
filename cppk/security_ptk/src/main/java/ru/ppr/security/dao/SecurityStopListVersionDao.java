package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;

/**
 * DAO для таблицы базы безопасности <i>SecurityStopListVersion</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class SecurityStopListVersionDao extends BaseEntityDao<String, Long> {

    public static final String TABLE_NAME = "SecurityStopListVersion";

    public static class Properties {
        public static final String SMART_CARD_STOP_LIST_ITEM_VERSION = "SmartCardStopListItemVersion";
        public static final String TICKET_STOP_LIST_ITEM_VERSION = "TicketStopListItemVersion";
        public static final String TICKET_WHITE_LIST_ITEM_VERSION = "TicketWhitelistItemVersion";
    }

    public SecurityStopListVersionDao(SecurityDaoSession securityDaoSession) {
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
     * Возвращает версию стоплистов карт
     *
     * @return
     */
    public String getSmartCardStoplistItemVersion() {
        String out = null;

        StringBuilder query = new StringBuilder();
        query.append("select ")
                .append(SecurityStopListVersionDao.Properties.SMART_CARD_STOP_LIST_ITEM_VERSION).append(" from ")
                .append(SecurityStopListVersionDao.TABLE_NAME);

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(query.toString(), null);
            if (cursor.moveToFirst()) {
                out = cursor.getString(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return out;
    }

    /**
     * Возвращает версию стоплистов билетов
     * <p>
     * Пример: 2015-09-02 11:57:35.443
     */
    public String getTicketStopListItemVersion() {
        String out = null;
        StringBuilder query = new StringBuilder();

        query.append("select ")
                .append(SecurityStopListVersionDao.Properties.TICKET_STOP_LIST_ITEM_VERSION).append(" from ")
                .append(SecurityStopListVersionDao.TABLE_NAME);

        Cursor cursor = null;

        try {
            cursor = db().rawQuery(query.toString(), null);
            if (cursor.moveToFirst()) {
                out = cursor.getString(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return out;
    }

    /**
     * Возвращает версию белого списка билетов
     * пример: 2015-08-28 11:56:17.857
     */
    public String getTicketWhiteListItemVersion() {
        String out = null;
        StringBuilder query = new StringBuilder();

        query.append("select ")
                .append(SecurityStopListVersionDao.Properties.TICKET_WHITE_LIST_ITEM_VERSION).append(" from ")
                .append(SecurityStopListVersionDao.TABLE_NAME);

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(query.toString(), null);
            if (cursor.moveToFirst()) {
                out = cursor.getString(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return out;
    }
}
