package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;

/**
 * DAO для таблицы базы безопасности <i>RolePermissionDvc</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class RolePermissionDvcDao extends BaseEntityDao<String, Long> {

    public static final String TABLE_NAME = "RolePermissionDvc";

    public static class Properties {
        public static final String ID = "Id";
        public static final String ROLE_ID = "RoleId";
        public static final String PERMISSION_ID = "PermissionId";
    }

    public RolePermissionDvcDao(SecurityDaoSession securityDaoSession) {
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
     * Проверяет разрешение у роли (доступно ли такое разрешение текущей роли)
     */
    public boolean isPermissionEnabled(RoleDvc role, PermissionDvc permission) {
        boolean out = false;
        if (role == null || permission == null)
            return out;
        else if (role.isRoot()) {
            return true;
        }
        int permissionDvcId = getSecurityDaoSession().getPermissionDvcDao().getPermissionDvcId(permission);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select count(*) from RolePermissionDvc where RoleId=").append(role.getId())
                .append(" and PermissionId=").append(permissionDvcId);

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                out = cursor.getInt(0) > 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return out;
    }
}
