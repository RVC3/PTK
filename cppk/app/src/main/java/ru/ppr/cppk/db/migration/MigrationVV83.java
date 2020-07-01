package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Aleksandr Brazhkin
 */
public class MigrationVV83 extends MigrationBase {

    public MigrationVV83(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 83;
    }

    @Override
    public String getVersionDescription() {
        return "Удаление кривой банковской транзакции (id = 0, approved = 1)";
    }

}