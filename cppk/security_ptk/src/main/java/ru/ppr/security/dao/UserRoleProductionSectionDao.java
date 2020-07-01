package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;

/**
 * DAO для таблицы базы безопасности <i>UserRoleProductionSection</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class UserRoleProductionSectionDao extends BaseEntityDao<String, Long> {

    public static final String TABLE_NAME = "UserRoleProductionSection";

    public static class Properties {
        public static final String ID = "Id";
        public static final String USER_ROLE_ID = "UserRoleId";
        public static final String PRODUCTION_SECTION_CODE = "ProductionSectionCode";
    }

    public UserRoleProductionSectionDao(SecurityDaoSession securityDaoSession) {
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
