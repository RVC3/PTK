package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV97   extends MigrationBase {

    public MigrationVV97(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 97;
    }

    @Override
    public String getVersionDescription() {
        return "Добавляем поля CheckId и ProgressStatus в CashRegisterWorkingShiftEvent";
    }

}