package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;

/**
 * DAO для таблицы базы безопасности <i>PtkDataContractsVersion</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class PtkDataContractsVersionDao extends BaseEntityDao<String, Long> {

    public static final String TABLE_NAME = "PtkDataContractsVersion";

    public static class Properties {
        public static final String VERSION = "Version";
    }

    public PtkDataContractsVersionDao(SecurityDaoSession securityDaoSession) {
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
     * Возвращает версию датаконтрактов из текущей security базы
     */
    public int getPtkDataContractsVersion() {
        int out = -1;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(Properties.VERSION);
        stringBuilder.append(" FROM ");
        stringBuilder.append(TABLE_NAME);

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

        return out;
    }
}
