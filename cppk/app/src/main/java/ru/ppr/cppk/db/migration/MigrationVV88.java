package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV88 extends MigrationBase {

    public MigrationVV88(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 88;
    }

    @Override
    public String getVersionDescription() {
        return "Добавляем колонку DataContractVersion в таблицу UpdateEvent";
    }

}