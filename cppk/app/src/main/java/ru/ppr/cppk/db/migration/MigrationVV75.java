package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV75 extends MigrationBase {

    public MigrationVV75(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 75;
    }

    @Override
    public String getVersionDescription() {
        return "Поле AdditionalInfoForEtt.IssueDataTime теперь может быть null, заменены PassengerCategory r->Р";
    }
}