package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

public class MigrationVV104 extends MigrationBase {

    public MigrationVV104(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 104;
    }

    @Override
    public String getVersionDescription() {
        return "Добавление поля markedAsDeleted с пометкой удаления записи";
    }

}