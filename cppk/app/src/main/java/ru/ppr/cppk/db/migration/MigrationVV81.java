package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV81 extends MigrationBase {

    public MigrationVV81(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 81;
    }

    @Override
    public String getVersionDescription() {
        return "Таблица SentEvents переделана на ключ/значение + добавлена запись для SentFinePaidEvents";
    }

}