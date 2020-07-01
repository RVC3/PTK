package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Обновление локальной БД до версии 49
 */
public class MigrationVV49 extends MigrationBase {

    public MigrationVV49(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 49;
    }

    @Override
    public String getVersionDescription() {
        return "В таблицу PtkSettingsPrivate добавлена колонка isUseMobileData";
    }

}
