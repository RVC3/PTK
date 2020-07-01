package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;

/**
 * DAO для таблицы базы безопасности <i>SecurityDataVersion</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class SecurityDataVersionDao extends BaseEntityDao<String, Long> {

    public static final String TABLE_NAME = "SecurityDataVersion";

    public static class Properties {
        public static final String DATA_CONTRACT_VERSION = "DataContractVersion";
    }

    public SecurityDataVersionDao(SecurityDaoSession securityDaoSession) {
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
     * Возвращает версию базы security (последняя дата из таблицы SecurityDataVersion[DataContractVersion])
     * пример: 2015-09-11 09:05:00
     */
    public String getSecurityVersion() {
        String out = null;
        StringBuilder query = new StringBuilder();
        query.append("select max(" + SecurityDataVersionDao.Properties.DATA_CONTRACT_VERSION + ") from "
                + SecurityDataVersionDao.TABLE_NAME);

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
