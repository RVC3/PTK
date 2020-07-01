package ru.ppr.cppk.managers.db;

import android.content.Context;

import ru.ppr.cppk.db.DefaultLocalDaoSession;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.LocalDbSessionManager;
import ru.ppr.cppk.db.migration.base.MigrationException;
import ru.ppr.cppk.db.migration.base.MigrationManager;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class LocalDbManager extends AbstractDbManager<LocalDaoSession> implements LocalDbSessionManager {

    private static final String TAG = Logger.makeLogTag(LocalDbManager.class);

    public static final String DB_NAME = "cppkdb.sqlite";

    public LocalDbManager(Context context) {
        super(context);
    }

    @Override
    protected String getDbName() {
        return DB_NAME;
    }

    @Override
    protected boolean isExternalDatabase() {
        return false;
    }

    /**
     * Переинициализирует обертку для доступа к БД.
     */
    @Override
    public void resetDaoSession() {
        Logger.trace(TAG, "resetLocalDaoSession");

        //производим обновление структуры локальной БД
        try {
            MigrationManager.checkVersionAndMakeUpdateLocalDb(getAppDbOpenHelper().getWritableDatabase());
        } catch (MigrationException e) {
            Logger.error(TAG, "Database version from " + e.getFromVersionNumber() + " to " + e.getToVersionNumber() + " migration failed", e);
        }

        LocalDaoSession localDaoSession = new DefaultLocalDaoSession(getAppDbOpenHelper().getWritableDatabase());
        setDaoSession(localDaoSession);
    }
}
