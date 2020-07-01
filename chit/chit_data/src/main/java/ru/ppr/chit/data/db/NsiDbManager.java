package ru.ppr.chit.data.db;

import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.chit.data.assets.AssetsEntry;
import ru.ppr.chit.data.assets.AssetsStore;
import ru.ppr.chit.nsidb.daosession.NsiDaoMaster;
import ru.ppr.chit.nsidb.daosession.NsiDaoSession;
import ru.ppr.database.greendao.GreenDaoLoggableDatabase;
import ru.ppr.logger.Logger;

/**
 * Менеджер для работы с БД НСИ.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class NsiDbManager extends AbstractDbManager<NsiDaoSession> {

    private static final String TAG = Logger.makeLogTag(NsiDbManager.class);

    private static final String DATABASE_NAME = "nsiDb.db";

    @Inject
    NsiDbManager(Context context, AssetsStore assetsStore) {
        super(context, assetsStore, DATABASE_NAME, AssetsEntry.NSI_DB);
    }

    @Override
    public NsiDaoSession createDaoSession(GreenDaoLoggableDatabase database) {
        NsiDaoMaster daoMaster = new NsiDaoMaster(database);
        return daoMaster.newSession();
    }
}
