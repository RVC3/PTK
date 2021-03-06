package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Обновление локальной БД до версии 47
 */
public class MigrationVV47 extends MigrationBase {

    public MigrationVV47(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 47;
    }

    @Override
    public String getVersionDescription() {
        return "В таблицу PtkSettingsPrivate добавлены колонки isPosEnabled, isSaleEnabled";
    }

}
