package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;

/**
 * DAO для таблицы базы безопасности <i>UserRoleDvc</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class UserRoleDvcDao extends BaseEntityDao<String, Long> {

    public static final String TABLE_NAME = "UserRoleDvc";

    public static class Properties {
        public static final String ID = "Id";
        public static final String USER_ID = "UserId";
        public static final String ROLE_ID = "RoleId";
    }

    public UserRoleDvcDao(SecurityDaoSession securityDaoSession) {
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
}
