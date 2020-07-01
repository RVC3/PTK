package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Created by nevolin on 06.07.2016.
 */
public class MigrationVV54 extends MigrationBase {

    public MigrationVV54(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 54;
    }

    @Override
    public String getVersionDescription() {
        return "В таблицу PtkSettingsCommon добавлена колонка posTerminalCheckPeriod";
    }

}
