package ru.ppr.chit.data.db;

import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.chit.data.assets.AssetsEntry;
import ru.ppr.chit.data.assets.AssetsStore;
import ru.ppr.chit.localdb.daosession.LocalDaoMaster;
import ru.ppr.chit.localdb.daosession.LocalDaoSession;
import ru.ppr.database.greendao.GreenDaoLoggableDatabase;
import ru.ppr.logger.Logger;

/**
 * Менеджер для работы с локальной БД.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class LocalDbManager extends AbstractDbManager<LocalDaoSession> {

    private static final String TAG = Logger.makeLogTag(LocalDbManager.class);

    private static final String DATABASE_NAME = "localDb.db";

    private final DbMigrationManager dbMigrationManager;

    @Inject
    public LocalDbManager(Context context, AssetsStore assetsStore, DbMigrationManager dbMigrationManager) {
        super(context, assetsStore, DATABASE_NAME, AssetsEntry.LOCAL_DB);
        this.dbMigrationManager = dbMigrationManager;
    }

    @Override
    protected void prepareDatabase(GreenDaoLoggableDatabase database) {
        Logger.trace(TAG, "prepareDatabase");
        try {
            // Производим обновление структуры локальной БД, если требуется
            dbMigrationManager.upgradeIfNeed(database);
        } catch (Exception e) {
            Logger.error(TAG, "LocalDb migration failed", e);
        }
    }

    @Override
    public LocalDaoSession createDaoSession(GreenDaoLoggableDatabase database) {
        LocalDaoMaster daoMaster = new LocalDaoMaster(database);
        return daoMaster.newSession();
    }
}
