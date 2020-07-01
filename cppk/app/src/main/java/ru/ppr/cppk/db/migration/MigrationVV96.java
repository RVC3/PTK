package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV96  extends MigrationBase {

    public MigrationVV96(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 96;
    }

    @Override
    public String getVersionDescription() {
        return "Добавляем поле Status в TestTicketEvent и убираем флаг NOT NULL для поля CheckId";
    }

}