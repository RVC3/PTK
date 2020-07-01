package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * https://aj.srvdev.ru/browse/CPPKPP-29029
 * @author Aleksandr Brazhkin
 */
public class MigrationVV68 extends MigrationBase {

    public MigrationVV68(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 68;
    }

    @Override
    public String getVersionDescription() {
        return "Изменен механизм учета билетной ленты";
    }

}
