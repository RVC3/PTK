package ru.ppr.chit.data.db.migration;

import android.database.Cursor;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.data.db.DbMigrationManager;
import ru.ppr.chit.data.db.migration.versions.base.Migration;
import ru.ppr.chit.localdb.greendao.LocalDbVersionEntityDao;
import ru.ppr.database.Database;
import ru.ppr.database.QueryBuilder;
import ru.ppr.logger.Logger;

/**
 * Менеджер, осуществляющий миграцию на новую версию структуры локальной БД.
 *
 * @author Aleksandr Brazhkin
 */
public class DbMigrationManagerImpl implements DbMigrationManager {

    private static final String TAG = Logger.makeLogTag(DbMigrationManagerImpl.class);

    /**
     * Версия структуры локальной БД, необходимая для работы приложения
     */
    private static final int REQUIRED_LOCAL_DB_VERSION = 6;

    private final MigrationProvider migrationProvider;

    @Inject
    DbMigrationManagerImpl(MigrationProvider migrationProvider) {
        this.migrationProvider = migrationProvider;
    }

    @Override
    public void upgradeIfNeed(Database db) throws Exception {
        // Текущая версия структуры БД
        int oldVersion = getCurrentLocalDbVersion(db);
        // Актуальная версия структуры БД
        int newVersion = REQUIRED_LOCAL_DB_VERSION;
        if (oldVersion < 1) {
            Logger.error(TAG, "Illegal state, oldVersion shouldn't be smaller 1");
        }
        if (oldVersion < newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private void onUpgrade(Database db, int oldVersion, int newVersion) throws Exception {
        Logger.trace(TAG, String.format("start upgrade database structure, oldVersion = %d, newVersion = %d", oldVersion, newVersion));

        // Формируем список необходимых миграций и последовательно выполняем их
        List<Migration> migrations = migrationProvider.getMigrations(oldVersion, newVersion);
        for (Migration migration : migrations) {
            migration.migrate(db);
        }

        Logger.trace(TAG, String.format("finish upgrade database structure, newVersion = %d", newVersion));
    }

    /**
     * Возвращает текущую версию структуры локальной БД.
     */
    private int getCurrentLocalDbVersion(Database db) {
        QueryBuilder qb = new QueryBuilder();
        qb.select()
                .max(LocalDbVersionEntityDao.Properties.Id.columnName)
                .from(LocalDbVersionEntityDao.TABLENAME);

        Cursor cursor = qb.build().run(db);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }
}
