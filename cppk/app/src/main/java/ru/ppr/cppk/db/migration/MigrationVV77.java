package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV77 extends MigrationBase {

    public MigrationVV77(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {

        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 77;
    }

    @Override
    public String getVersionDescription() {
        return "Добавляем поле FeeType в таблицу Fee";
    }
}