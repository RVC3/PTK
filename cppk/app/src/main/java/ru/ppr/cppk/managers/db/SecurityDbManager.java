package ru.ppr.cppk.managers.db;

import android.content.Context;

import ru.ppr.logger.Logger;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.SecurityDbSessionManager;

/**
 * @author Aleksandr Brazhkin
 */
public class SecurityDbManager extends AbstractDbManager<SecurityDaoSession> implements SecurityDbSessionManager {

    private static final String TAG = Logger.makeLogTag(SecurityDbManager.class);

    public static final String DB_NAME = "_SecurityDatabase.db";

    public SecurityDbManager(Context context) {
        super(context);
    }

    @Override
    protected String getDbName() {
        return DB_NAME;
    }

    @Override
    protected boolean isExternalDatabase() {
        // База данных является внешней и мы не управляемм ее структурой и версией
        return true;
    }

    /**
     * Переинициализирует обертку для доступа к БД.
     */
    @Override
    public void resetDaoSession() {
        Logger.trace(TAG, "resetDaoSession");
        setLogEnabled(isSqlLogsEnabled());
        SecurityDaoSession securityDaoSession = new SecurityDaoSession(getAppDbOpenHelper().getWritableDatabase());
        setDaoSession(securityDaoSession);
    }
}
