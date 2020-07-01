package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Aleksandr Brazhkin
 */
public class MigrationVV106 extends MigrationBase {

    public MigrationVV106(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 106;
    }

    @Override
    public String getVersionDescription() {
        return "Коррекция Foreign Keys";
    }

}