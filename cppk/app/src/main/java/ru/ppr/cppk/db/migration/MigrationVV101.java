package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV101 extends MigrationBase {

    public MigrationVV101 (Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 101;
    }

    @Override
    public String getVersionDescription() {
        return "Убираем свойство NOT NULL для поля Type в таблице TicketEventBase";
    }

}