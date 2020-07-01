package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Aleksandr Brazhkin
 */
public class MigrationVV103 extends MigrationBase {

    public MigrationVV103(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 103;
    }

    @Override
    public String getVersionDescription() {
        return "Добавлена колонка StartDayOffset в таблицу TicketEventBase";
    }
}
