package ru.ppr.chit.data.db.migration.versions;

import android.content.Context;

import ru.ppr.chit.data.db.migration.versions.base.BaseMigration;
import ru.ppr.database.Database;

/**
 * Добавляет в таблицы поле deletedMark
 *
 * @author Max Sidorov
 */
public class MigrationV6 extends BaseMigration {

    public MigrationV6(Context context) {
        super(context);
    }

    @Override
    protected void migrateInternal(Database db) throws Exception {
        execFromAssets(db);
    }

    @Override
    public int getVersionNumber() {
        return 6;
    }

    @Override
    public String getVersionDescription() {
        return "Добавление в основные таблицы поля deletedMark";
    }
}
