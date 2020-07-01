package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV74 extends MigrationBase {

    public MigrationVV74(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 74;
    }

    @Override
    public String getVersionDescription() {
        return "Удаление ссылок на CashRegisterWorkingShift из событий печати отчетов и событий учета БЛ в случае уже закрытой смены";
    }
}