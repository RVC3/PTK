package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Created by Александр on 01.08.2016.
 */
public class MigrationVV58 extends MigrationBase {

    public MigrationVV58(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 58;
    }

    @Override
    public String getVersionDescription() {
        return "Добавлена таблица CPPKServiceSale";
    }

}