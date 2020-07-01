package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.SecuritySettings;

/**
 * DAO для таблицы базы безопасности <i>Setting</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class SettingDao extends BaseEntityDao<String, Long> {

    public static final String TABLE_NAME = "Setting";

    public static class Properties {
        public static final String PARAMETER = "Parameter";
        public static final String VALUE = "Value";
    }

    public SettingDao(SecurityDaoSession securityDaoSession) {
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

    public SecuritySettings getSecuritySettings() {
        SecuritySettings.Builder builder = new SecuritySettings.Builder();

        Cursor cursor = null;
        try {
            cursor = db().query(SettingDao.TABLE_NAME, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                int parameterIndex = cursor.getColumnIndex(Properties.PARAMETER);
                int valueIndex = cursor.getColumnIndex(Properties.VALUE);
                do {
                    String params = cursor.getString(parameterIndex);
                    switch (params) {
                        case SecuritySettings.Parameters.SecuritySettings_AccessCardLoginLimitationPeriod:
                            builder.setAccessCardLoginLimitationPeriod(cursor.getInt(valueIndex));
                            break;

                        case SecuritySettings.Parameters.SecuritySettings_AccessCodeValidityPeriod:
                            builder.setAccessCodeValidityPeriod(cursor.getInt(valueIndex));
                            break;

                        case SecuritySettings.Parameters.SecuritySettings_LimitLoginAttempts:
                            builder.setLimitLoginAttempts(cursor.getInt(valueIndex));
                            break;

                        case SecuritySettings.Parameters.SecuritySettings_TimeLockAccess:
                            builder.setTimeLockAccess(cursor.getInt(valueIndex));
                            break;

                        case SecuritySettings.Parameters.SecuritySettings_DevicePincodeLength:
                            builder.setDevicePincodeLength(cursor.getInt(valueIndex));
                            break;

                        default:
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return builder.create();
    }
}
