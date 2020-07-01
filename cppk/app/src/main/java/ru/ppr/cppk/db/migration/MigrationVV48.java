package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Обновление локальной БД до версии 48
 */
public class MigrationVV48 extends MigrationBase {

    public MigrationVV48(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);

        
    }

    @Override
    public int getVersionNumber() {
        return 48;
    }

    @Override
    public String getVersionDescription() {
        return "В таблицу TicketTapeEvent добавлена колонка ExpectedFirstDocNumber";
    }

}
