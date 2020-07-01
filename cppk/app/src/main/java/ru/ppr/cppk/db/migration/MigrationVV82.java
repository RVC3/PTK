package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV82 extends MigrationBase {

    public MigrationVV82(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 82;
    }

    @Override
    public String getVersionDescription() {
        return "Заменяем TicketEventBase.WayType=1 на WayType=0 для проданных доплат";
    }

}