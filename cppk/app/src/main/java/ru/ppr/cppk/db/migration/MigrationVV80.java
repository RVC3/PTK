package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * http://agile.srvdev.ru/browse/CPPKPP-33981
 *
 * @author Aleksandr Brazhkin
 */
public class MigrationVV80 extends MigrationBase {

    public MigrationVV80(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 80;
    }

    @Override
    public String getVersionDescription() {
        return "Добавлена таблица FineSaleEvent";
    }

}