package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

public class MigrationVV61 extends MigrationBase {

    public MigrationVV61(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 61;
    }

    @Override
    public String getVersionDescription() {
        return "В таблице BankTransactionCashRegisterEvent сняты CONSTRAINT NOT NULL с полей: MerchantId, Rrn, AuthorizationCode, SmartCardApplicationName, CardPan, CardEmitentName, BankCheckNumber.";
    }

}