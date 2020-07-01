package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Created by григорий on 29.07.2016.
 */
public class MigrationVV57 extends MigrationBase {

    public MigrationVV57(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 57;
    }

    @Override
    public String getVersionDescription() {
        return "В таблицу TerminalDay добавлена колонка TerminalNumber default=0";
    }

}