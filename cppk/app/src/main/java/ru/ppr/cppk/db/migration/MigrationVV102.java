package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Dmitry Vinogradov
 */
public class MigrationVV102 extends MigrationBase {

    public MigrationVV102(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 102;
    }

    @Override
    public String getVersionDescription() {
        return "Добавляем новое поле transferDepartureDateTime в таблицу CPPKTicketControl";
    }
}
