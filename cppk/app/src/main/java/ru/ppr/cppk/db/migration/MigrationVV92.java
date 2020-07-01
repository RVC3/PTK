package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Dmitry Nevolin
 */
public class MigrationVV92 extends MigrationBase {

    public MigrationVV92(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 92;
    }

    @Override
    public String getVersionDescription() {
        return "Исправляем LossSum в таблице Exemption, в рамках CPPKPP-37169";
    }

}