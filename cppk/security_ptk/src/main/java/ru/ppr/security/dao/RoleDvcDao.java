package ru.ppr.security.dao;

import android.database.Cursor;

import java.util.ArrayList;

import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.RoleDvc;

/**
 * DAO для таблицы базы безопасности <i>RoleDvc</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class RoleDvcDao extends BaseEntityDao<RoleDvc, Long> {

    public static final String TABLE_NAME = "RoleDvc";

    public static class Properties {
        public static final String ID = "Id";
        public static final String NAME = "Name";
    }

    public RoleDvcDao(SecurityDaoSession securityDaoSession) {
        super(securityDaoSession);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public RoleDvc fromCursor(Cursor cursor) {
        RoleDvc roleDvc = new RoleDvc();
        roleDvc.setId(cursor.getInt(cursor.getColumnIndex(RoleDvcDao.Properties.ID)));
        roleDvc.setName(cursor.getString(cursor.getColumnIndex(RoleDvcDao.Properties.NAME)));
        return roleDvc;
    }

    /**
     * Вернет первую из доступных на текущем участке ролей пользователя
     */
    public RoleDvc getUserRoleForProductionSection(int cProductionSectionCode, int userId) {
        RoleDvc role = null;
        StringBuilder sql = new StringBuilder()
                .append("select * from ").append(RoleDvcDao.TABLE_NAME).append(" WHERE ")
                .append(RoleDvcDao.Properties.ID).append(" = ")
                .append(" ( ")
                .append(" select ").append(UserRoleDvcDao.Properties.ROLE_ID).append(" FROM ").append(UserRoleDvcDao.TABLE_NAME).append(" WHERE ")
                .append(UserRoleDvcDao.Properties.ID).append(" in ")
                .append(" ( ")
                .append(" select ").append(UserRoleProductionSectionDao.Properties.USER_ROLE_ID).append(" FROM ").append(UserRoleProductionSectionDao.TABLE_NAME).append(" WHERE ")
                .append(UserRoleProductionSectionDao.Properties.PRODUCTION_SECTION_CODE).append(" = ").append(cProductionSectionCode)
                .append(" ) ")
                .append(" AND ")
                .append(UserRoleDvcDao.Properties.USER_ID).append(" = ").append(userId)
                .append(" LIMIT 1) ");

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(sql.toString(), null);
            if (cursor.moveToFirst()) {
                role = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return role;
    }

    /**
     * Получение списка возможных ролей на ПТК
     */
    public ArrayList<RoleDvc> getRoleDvcList() {
        ArrayList<RoleDvc> out = new ArrayList<RoleDvc>();
        StringBuilder sql = new StringBuilder()
                .append("select * from ").append(RoleDvcDao.TABLE_NAME);

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(sql.toString(), null);
            while (cursor.moveToNext()) {
                RoleDvc role = fromCursor(cursor);
                out.add(role);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return out;
    }
}
