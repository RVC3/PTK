package ru.ppr.cppk.db.migration.base;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.db.local.LocalDbVersionDao;
import ru.ppr.database.Database;
import ru.ppr.database.SqliteScripts;
import ru.ppr.logger.Logger;

public abstract class MigrationBase {

    protected static String TAG = Logger.makeLogTag(MigrationBase.class);

    protected final Context context;

    public MigrationBase(Context context) {
        this.context = context;
    }

    public abstract void migrate(Database localDB) throws Exception;

    public void migrateInTransaction(Database localDB) throws MigrationException {
        int prevDbVersion = -1;
        try {
            prevDbVersion = MigrationManager.getVersionLocalDb(localDB);
            localDB.beginTransaction();
            try {
                migrate(localDB);
                addVersionToLocalDb(localDB, getVersionNumber(), getVersionDescription());
                localDB.setTransactionSuccessful();
                Logger.info(TAG, "Migration step from " + prevDbVersion + " to " + getVersionNumber() + " successful");
            } catch (Exception e) {
                Logger.error(TAG, "Migration step from " + prevDbVersion + " to " + getVersionNumber() + " failed");
                Logger.error(TAG, e);
                throw e;
            } finally {
                localDB.endTransaction();
            }
        } catch (Exception e) {
            throw new MigrationException(prevDbVersion, getVersionNumber(), e);
        }
    }

    /**
     * Добавляет запись о новой версии в локальную БД
     */
    private static void addVersionToLocalDb(Database localDB, int version, String description) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LocalDbVersionDao.Properties.VersionId, version);
        contentValues.put(LocalDbVersionDao.Properties.CreatedDateTime, DateFormatOperations.getDateddMMyyyyHHmmss((new Date())));
        contentValues.put(LocalDbVersionDao.Properties.Description, description);
        long id = localDB.insert(LocalDbVersionDao.TABLE_NAME, null, contentValues);
        if (id < 0)
            throw new Exception("could not add new version number to database");
    }

    protected void migrateWithScriptFromAssets(Database localDB) throws Exception {
        String sql = SqliteScripts.readAssetsSqlScript(Globals.getInstance(), "migration/migrateTo" + getVersionNumber() + ".sql");
        migrate(localDB, sql);
    }

    protected void migrate(Database localDB, String sqlStatements) throws Exception {
        SqliteScripts.exec(localDB, sqlStatements);
    }

    protected void migrate(Database localDB, List<String> sqlStatements) throws Exception {
        SqliteScripts.exec(localDB, sqlStatements);
    }

    public abstract int getVersionNumber();

    public abstract String getVersionDescription();

}
