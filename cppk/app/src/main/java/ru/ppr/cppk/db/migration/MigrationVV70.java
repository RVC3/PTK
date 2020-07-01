package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * https://aj.srvdev.ru/browse/CPPKPP-30379
 */
public class MigrationVV70 extends MigrationBase {

    public MigrationVV70(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 70;
    }

    @Override
    public String getVersionDescription() {
        return "В таблицу CPPKServiceSale добавлена колонка TicketTapeEventId, check NOT NULL";
    }

}
