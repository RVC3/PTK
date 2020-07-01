package ru.ppr.chit.data.db;

import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.chit.data.assets.AssetsEntry;
import ru.ppr.chit.data.assets.AssetsStore;
import ru.ppr.chit.securitydb.daosession.SecurityDaoMaster;
import ru.ppr.chit.securitydb.daosession.SecurityDaoSession;
import ru.ppr.database.greendao.GreenDaoLoggableDatabase;
import ru.ppr.logger.Logger;

/**
 * Менеджер для работы с БД безопасности.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class SecurityDbManager extends AbstractDbManager<SecurityDaoSession> {

    private static final String TAG = Logger.makeLogTag(SecurityDbManager.class);

    private static final String DATABASE_NAME = "securityDb.db";

    @Inject
    public SecurityDbManager(Context context, AssetsStore assetsStore) {
        super(context, assetsStore, DATABASE_NAME, AssetsEntry.SECURITY_DB);
    }

    @Override
    public SecurityDaoSession createDaoSession(GreenDaoLoggableDatabase database) {
        SecurityDaoMaster daoMaster = new SecurityDaoMaster(database);
        return daoMaster.newSession();
    }
}
