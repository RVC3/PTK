package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV105 extends MigrationBase {

    public MigrationVV105(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 105;
    }

    @Override
    public String getVersionDescription() {
        return "Удаляем лишнее значение из CommonSettings (DURATION_OF_PD_NEXT_DAY)";
    }

}