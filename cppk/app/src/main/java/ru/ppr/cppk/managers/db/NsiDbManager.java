package ru.ppr.cppk.managers.db;

import android.content.Context;

import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.NsiDbSessionManager;

/**
 * @author Aleksandr Brazhkin
 */
public class NsiDbManager extends AbstractDbManager<NsiDaoSession> implements NsiDbSessionManager {

    private static final String TAG = Logger.makeLogTag(NsiDbManager.class);

    public static final String DB_NAME = "_ReferenceDatabase.db";

    public NsiDbManager(Context context) {
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
        NsiDaoSession nsiDaoSession = new NsiDaoSession(getAppDbOpenHelper().getWritableDatabase());
        setDaoSession(nsiDaoSession);
    }
}
