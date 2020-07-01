package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Aleksandr Brazhkin
 */
public class MigrationVV85 extends MigrationBase {

    public MigrationVV85(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 85;
    }

    @Override
    public String getVersionDescription() {
        return "Добавление колонки IsRestoredTicket (билет восстановлен) в таблицу CPPKTicketControl ";
    }

}