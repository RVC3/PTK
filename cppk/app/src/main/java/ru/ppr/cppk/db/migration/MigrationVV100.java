package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV100 extends MigrationBase {

    public MigrationVV100(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 100;
    }

    @Override
    public String getVersionDescription() {
        return "Добавляем новые поля (Trips7000Spend, TripsCount, Trips7000Count) в таблицу CPPKTicketControl";
    }

}