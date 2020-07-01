package ru.ppr.chit.data.db.migration.versions.base;

import android.content.ContentValues;
import android.content.Context;

import java.util.List;

import ru.ppr.chit.localdb.greendao.LocalDbVersionEntityDao;
import ru.ppr.database.Database;
import ru.ppr.database.SqliteScripts;
import ru.ppr.logger.Logger;

/**
 * Базовый класс для миграции на локальной БД.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseMigration implements Migration {

    protected final Context context;

    protected BaseMigration(Context context) {
        this.context = context;
    }

    protected abstract void migrateInternal(Database db) throws Exception;

    @Override
    public abstract int getVersionNumber();

    @Override
    public abstract String getVersionDescription();

    /**
     * Выполняет обновление структуры БД в транзакции.
     */
    @Override
    public final void migrate(Database db) throws Exception {
        db.beginTransaction();
        try {
            migrateInternal(db);
            addNewVersionToDb(db, getVersionNumber(), getVersionDescription());

            db.setTransactionSuccessful();
            Logger.info(getTag(), "Migration to " + getVersionNumber() + " version successful");
        } catch (Exception e) {
            Logger.error(getTag(), "Migration to " + getVersionNumber() + " version failed", e);
            throw e;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Добавляет запись о новой версии структуры БД.
     */
    private void addNewVersionToDb(Database db, int version, String description) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LocalDbVersionEntityDao.Properties.Id.columnName, version);
        contentValues.put(LocalDbVersionEntityDao.Properties.UpgradeDateTime.columnName, System.currentTimeMillis());
        contentValues.put(LocalDbVersionEntityDao.Properties.Description.columnName, description);
        db.insert(LocalDbVersionEntityDao.TABLENAME, null, contentValues);
    }

    /**
     * Выполняет скрипт sqlStatements, команды разделяются ;.
     */
    protected void exec(Database db, String sqlStatements) throws Exception {
        SqliteScripts.exec(db, sqlStatements);
    }

    /**
     * Выполняет последовательность sql команд.
     */
    protected void exec(Database db, List<String> sqlStatements) throws Exception {
        SqliteScripts.exec(db, sqlStatements);
    }

    /**
     * Выполняет sql скрипт из assets
     * Имя скрипта должно быть migration/migrateTo[Version].sql
     */
    protected void execFromAssets(Database db) throws Exception {
        String sql = SqliteScripts.readAssetsSqlScript(context, "migration/migrateTo" + getVersionNumber() + ".sql");
        SqliteScripts.exec(db, sql);
    }

    public String getTag(){
        return Logger.makeLogTag(getClass());
    }

}
