package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.PermissionDvc;

/**
 * DAO для таблицы базы безопасности <i>PermissionDvc</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class PermissionDvcDao extends BaseEntityDao<String, Long> {

    public static final String TABLE_NAME = "PermissionDvc";

    public static class Properties {
        public static final String ID = "Id";
        public static final String CODE = "Code";
    }

    public PermissionDvcDao(SecurityDaoSession securityDaoSession) {
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
     * Вернет Id Пермишшена (-1 - в случае неудачи)
     */
    public int getPermissionDvcId(PermissionDvc permission) {
        int out = -1;

        String[] selectionArgs = new String[1];

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(Properties.ID);
        stringBuilder.append(" FROM ");
        stringBuilder.append(TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(Properties.CODE).append(" = ").append("?");
        selectionArgs[0] = permission.getCode();

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgs);
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
