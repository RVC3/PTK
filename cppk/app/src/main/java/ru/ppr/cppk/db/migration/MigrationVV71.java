package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

public class MigrationVV71 extends MigrationBase {

    public MigrationVV71(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 71;
    }

    @Override
    public String getVersionDescription() {
        return "В таблице BankTransactionCashRegisterEvent снят constraint NOT NULL с CurrencyCode, TerminalNumber.";
    }

}
