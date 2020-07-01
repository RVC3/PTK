package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

public class MigrationVV63 extends MigrationBase {

    public MigrationVV63(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 63;
    }

    @Override
    public String getVersionDescription() {
        return "Тип данных для deviceId, userId, ecpKeyNumber изменен на long";
    }

}