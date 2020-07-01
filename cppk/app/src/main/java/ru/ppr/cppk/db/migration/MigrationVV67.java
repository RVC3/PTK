package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Created by григорий on 28.10.2016.
 * https://aj.srvdev.ru/browse/CPPKPP-29864
 */

public class MigrationVV67 extends MigrationBase {

    public MigrationVV67(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 67;
    }

    @Override
    public String getVersionDescription() {
        return "Добавляем в CommonSettings поле timeZoneOffset";
    }

}
