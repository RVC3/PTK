package ru.ppr.chit.data.db;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import ru.ppr.chit.data.assets.AssetsStore;
import ru.ppr.database.Database;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.greendao.GreenDaoDatabase;
import ru.ppr.database.greendao.GreenDaoLoggableDatabase;
import ru.ppr.database.greendao.GreenDaoNewSQLiteDbOpenHelper;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Дефолтный класс для работы с localDb, rdsDb, securityDb.
 *
 * @author Aleksandr Brazhkin
 */
public class AppDbOpenHelper extends GreenDaoNewSQLiteDbOpenHelper {

    private static String TAG = Logger.makeLogTag(AppDbOpenHelper.class);

    private final Context context;
    private final AssetsStore assetsStore;
    private final AssetsStore.Entry assetsDbEntry;
    private GreenDaoLoggableDatabase loggableDatabase = null;
    private Database prevDelegateDatabase = null;

    AppDbOpenHelper(Context context, AssetsStore assetsStore, String name, AssetsStore.Entry assetsDbEntry) {
        super(context, name, 1);
        this.context = context;
        this.assetsStore = assetsStore;
        this.assetsDbEntry = assetsDbEntry;
    }

    @Override
    public void onCreate(Database db) {
        Logger.trace(TAG, "onCreate: " + getDatabaseName());
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        Logger.trace(TAG, "onUpgrade: " + getDatabaseName() + " from " + oldVersion + " to " + newVersion);
    }

    @Override
    public void onOpen(Database db) {
        Logger.trace(TAG, "onOpen: " + getDatabaseName());
    }

    File getDatabasePath() {
        return context.getDatabasePath(getDatabaseName());
    }

    @Override
    public GreenDaoLoggableDatabase getWritableDatabase() {
        Logger.trace(TAG, "getWritableDatabase: " + getDatabaseName());
        // Копируем здесь БД из Assets, если нужно
        prepareDatabase();
        GreenDaoLoggableDatabase loggableDatabase = wrap(super.getWritableDatabase());
        SqLiteUtils.logTablesInDatabase(loggableDatabase);
        return loggableDatabase;
    }


    @Override
    public GreenDaoLoggableDatabase getReadableDatabase() {
        Logger.trace(TAG, "getReadableDatabase: " + getDatabaseName());
        // Копируем здесь БД из Assets, если нужно
        prepareDatabase();
        GreenDaoLoggableDatabase loggableDatabase = wrap(super.getReadableDatabase());
        SqLiteUtils.logTablesInDatabase(loggableDatabase);
        return loggableDatabase;
    }

    private GreenDaoLoggableDatabase wrap(GreenDaoDatabase database) {
        if (prevDelegateDatabase != database) {
            if (database == null) {
                loggableDatabase = null;
                prevDelegateDatabase = null;
            } else {
                loggableDatabase = new GreenDaoLoggableDatabase(database, getDatabaseName());
                prevDelegateDatabase = database;
            }
        }

        return loggableDatabase;
    }

    private synchronized void prepareDatabase() {
        File file = getDatabasePath();
        if (file.exists()) {
            Logger.info(TAG, "Database already exists: " + file.getName());
        } else {
            try {
                File databasesDir = file.getParentFile();
                if ((databasesDir.exists() || databasesDir.mkdirs()) && FileUtils2.copyFileFromStream(assetsStore.getInputStream(assetsDbEntry), file)) {
                    Logger.info(TAG, "Database copied from assets: " + file.getName());
                } else {
                    throw new RuntimeException("Could not copy database from assets: " + file.getName());
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not copy database from assets: " + file.getName(), e);
            }
        }
    }
}