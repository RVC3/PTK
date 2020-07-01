package ru.ppr.cppk.db.migration;

import android.content.Context;
import android.content.SharedPreferences;

import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.cppk.di.Di;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV91 extends MigrationBase {

    public MigrationVV91(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 91;
    }

    @Override
    public String getVersionDescription() {
        return "Добавляем новые колонки для сущности CPPKTicketControl (трансфер): TransferDeparturePoint и TransferDestinationPoint";
    }

}