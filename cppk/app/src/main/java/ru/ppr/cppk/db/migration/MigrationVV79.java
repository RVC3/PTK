package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV79 extends MigrationBase {

    public MigrationVV79(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 79;
    }

    @Override
    public String getVersionDescription() {
        return "Исправлены TypeCode в сущности SmartCard с -1 на верные";
    }

}