package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Created by григорий on 28.10.2016.
 * https://aj.srvdev.ru/browse/CPPKPP-29838
 */

public class MigrationVV66  extends MigrationBase {

    public MigrationVV66(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 66;
    }

    @Override
    public String getVersionDescription() {
        return "Меняем дефолтное значение параметра PrivateSettings.isUseMobileData в true";
    }

}
