package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

public class MigrationVV59 extends MigrationBase {

    public MigrationVV59(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 59;
    }

    @Override
    public String getVersionDescription() {
        return "В таблицу PtkSettingsCommon добавлена колонка logFullSQL default = 0";
    }

}