package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * https://aj.srvdev.ru/browse/CPPKPP-29847
 */
public class MigrationVV69 extends MigrationBase {

    public MigrationVV69(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 69;
    }

    @Override
    public String getVersionDescription() {
        return "Изменена структура таблицы PtkSettingsCommon на схему хранения настроек key-value в DB Local.";
    }

}
