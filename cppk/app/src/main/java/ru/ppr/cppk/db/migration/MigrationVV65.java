package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Created by Александр on 14.10.2016.
 */
public class MigrationVV65 extends MigrationBase {

    public MigrationVV65(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 65;
    }

    @Override
    public String getVersionDescription() {
        return "В таблицу PtkSettingsCommon добавлены колонки autoBlockingTimeout и autoBlockingEnabled";
    }

}