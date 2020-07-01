package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * https://aj.srvdev.ru/browse/CPPKPP-32283
 *
 * @author Aleksandr Brazhkin
 */
public class MigrationVV73 extends MigrationBase {

    public MigrationVV73(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 73;
    }

    @Override
    public String getVersionDescription() {
        return "Чистка строкового представления 'null' в значениях PtkSettingsPrivate";
    }

}