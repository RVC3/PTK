package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Обновление локальной БД до версии 50
 */
public class MigrationVV50 extends MigrationBase {

    public MigrationVV50(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 50;
    }

    @Override
    public String getVersionDescription() {
        return "В таблицу PtkSettingsCommon добавлена колонка carrierName";
    }

}
