package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Created by григорий on 13.10.2016.
 */

public class MigrationVV64 extends MigrationBase {

    public MigrationVV64(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 64;
    }

    @Override
    public String getVersionDescription() {
        return "В PrivateSettings удален столбец trainCategoryCode и добавлен trainCategoryPrefix CPPKPP-29454";
    }

}