package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV99  extends MigrationBase {

    public MigrationVV99(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 99;
    }

    @Override
    public String getVersionDescription() {
        return "Меняем тип поля для TicketTapeEvent.Number на INTEGER и делаем его и поле TicketTapeEvent.Series NOT NULL";
    }

}