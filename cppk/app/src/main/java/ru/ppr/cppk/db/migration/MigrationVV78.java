package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * @autor Grigoriy Kashka
 */
public class MigrationVV78 extends MigrationBase {

    public MigrationVV78(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 78;
    }

    @Override
    public String getVersionDescription() {
        return "Добавлена таблица SentEvents";
    }

}