package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.SecurityCard;

/**
 * DAO для таблицы базы безопасности <i>SecurityCard</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class SecurityCardDao extends BaseEntityDao<SecurityCard, Long> {

    public static final String TABLE_NAME = "SecurityCard";

    public static class Properties {
        public static final String ID = "Id";
        public static final String USER_ID = "UserId";
        public static final String UID = "UID";
        public static final String SERIAL_NUMBER = "SerialNumber";
        public static final String VALID_FROM = "ValidFrom";
        public static final String VALID_TO = "ValidTo";
        public static final String PASSWORD_HASH = "PasswordHash";
        public static final String SALT = "Salt";
        public static final String ACTIVE = "Active";
    }

    public SecurityCardDao(SecurityDaoSession securityDaoSession) {
        super(securityDaoSession);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public SecurityCard fromCursor(Cursor cursor) {
        SecurityCard securityCard = new SecurityCard();

        int index = cursor.getColumnIndex(SecurityCardDao.Properties.ID);
        if (index != -1) securityCard.setId(cursor.getInt(index));

        index = cursor.getColumnIndex(SecurityCardDao.Properties.USER_ID);
        if (index != -1) securityCard.setUserId(cursor.getInt(index));

        index = cursor.getColumnIndex(SecurityCardDao.Properties.UID);
        if (index != -1) securityCard.setUidCard(cursor.getString(index));

        index = cursor.getColumnIndex(SecurityCardDao.Properties.VALID_FROM);
        if (index != -1) securityCard.setValidFrom(cursor.getLong(index));

        index = cursor.getColumnIndex(SecurityCardDao.Properties.VALID_TO);
        if (index != -1) securityCard.setValidTo(cursor.getLong(index));

        index = cursor.getColumnIndex(SecurityCardDao.Properties.PASSWORD_HASH);
        if (index != -1) securityCard.setPasswordHash(cursor.getString(index));

        index = cursor.getColumnIndex(SecurityCardDao.Properties.SALT);
        if (index != -1) securityCard.setSalt(cursor.getString(index));

        index = cursor.getColumnIndex(SecurityCardDao.Properties.SERIAL_NUMBER);
        if (index != -1) securityCard.setSerialNumber(cursor.getString(index));

        index = cursor.getColumnIndex(SecurityCardDao.Properties.ACTIVE);
        if (index != -1) securityCard.setActive(cursor.getInt(index) == 1);

        return securityCard;
    }

    public SecurityCard getSecurityCard(String uid, String userId) {

        if (uid == null) {
            throw new IllegalArgumentException("Login is null");
        }

        SecurityCard securityCard = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Select * from ").append(SecurityCardDao.TABLE_NAME)
                .append(" where ").append(SecurityCardDao.Properties.UID)
                .append(" = '").append(uid).append("'").append(" AND ")
                .append(SecurityCardDao.Properties.USER_ID).append(" ='").append(userId).append("'");

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                securityCard = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return securityCard;
    }
}
