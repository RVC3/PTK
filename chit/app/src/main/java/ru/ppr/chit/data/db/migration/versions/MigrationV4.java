package ru.ppr.chit.data.db.migration.versions;

import android.content.Context;

import ru.ppr.chit.data.db.migration.versions.base.BaseMigration;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationV4 extends BaseMigration {

    public MigrationV4(Context context) {
        super(context);
    }

    @Override
    protected void migrateInternal(Database db) throws Exception {
        execFromAssets(db);
    }

    @Override
    public int getVersionNumber() {
        return 4;
    }

    @Override
    public String getVersionDescription() {
        return "Добавил поле BoardingByList в таблицу TicketBoarding";
    }
}
