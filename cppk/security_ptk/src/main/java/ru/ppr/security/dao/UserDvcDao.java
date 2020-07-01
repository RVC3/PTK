package ru.ppr.security.dao;

import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ppr.logger.Logger;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.UserDvc;

/**
 * DAO для таблицы базы безопасности <i>UserDvc</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class UserDvcDao extends BaseEntityDao<UserDvc, Long> {

    private static final String TAG = Logger.makeLogTag(UserDvcDao.class);

    public static final String TABLE_NAME = "UserDvc";

    public static class Properties {
        public static final String ID = "Id";
        public static final String FIRST_NAME = "FirstName";
        public static final String LAST_NAME = "LastName";
        public static final String MIDDLE_NAME = "MiddleName";
        public static final String LOGIN = "Login";
        public static final String VALID_FROM = "ValidFrom";
        public static final String VALID_TO = "ValidTo";
    }

    public UserDvcDao(SecurityDaoSession securityDaoSession) {
        super(securityDaoSession);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public UserDvc fromCursor(Cursor cursor) {
        UserDvc out = new UserDvc();

        int index = cursor.getColumnIndex(UserDvcDao.Properties.ID);
        if (index != -1) out.setId(cursor.getInt(index));

        index = cursor.getColumnIndex(UserDvcDao.Properties.FIRST_NAME);
        if (index != -1) out.setFirstName(cursor.getString(index));

        index = cursor.getColumnIndex(UserDvcDao.Properties.LAST_NAME);
        if (index != -1) out.setLastName(cursor.getString(index));

        index = cursor.getColumnIndex(UserDvcDao.Properties.MIDDLE_NAME);
        if (index != -1) out.setMiddleName(cursor.getString(index));

        index = cursor.getColumnIndex(UserDvcDao.Properties.LOGIN);
        if (index != -1) out.setLogin(cursor.getString(index));

        index = cursor.getColumnIndex(UserDvcDao.Properties.VALID_FROM);
        if (index != -1)
            out.setValidFrom(getDateFrom1970(cursor.getString(index)));

        index = cursor.getColumnIndex(UserDvcDao.Properties.VALID_TO);
        if (index != -1)
            out.setValidTo(getDateFrom1970(cursor.getString(index)));

        return out;
    }

    /**
     * Возвращает информацию о пользователе по логину
     *
     * @param login логин пользователя по которому происходит выбор информации
     * @return информация о пользователе, если такой пользователь найден, либо
     * null
     */
    public UserDvc getUserFromUserDvc(String login) {

        if (login == null) {
            throw new IllegalArgumentException("Login is null");
        }

        UserDvc userDvc = null;

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Select * from ").append(UserDvcDao.TABLE_NAME).append(" where ").append(UserDvcDao.Properties.LOGIN).append(" = '").append(login.trim()).append("'");
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                userDvc = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return userDvc;
    }

    public Date getDateFrom1970(String datetime) {
        if (datetime == null)
            return null;
        try {
            return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datetime));
        } catch (ParseException e) {
            Logger.info(TAG, "Can not convert date " + datetime + " to timestamp with pattern yyyy-MM-dd HH:mm:ss - " + e.getMessage());
        }
        return null;
    }
}
