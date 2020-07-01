package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV76 extends MigrationBase {

    public MigrationVV76(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 76;
    }

    @Override
    public String getVersionDescription() {
        return "Исправляем время транзакции событий BankTransactionCashRegisterEvent с TransactionId=0 на время из Event.CreationTimestamp";
    }
}