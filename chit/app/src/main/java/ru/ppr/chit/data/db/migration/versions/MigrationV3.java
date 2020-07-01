package ru.ppr.chit.data.db.migration.versions;

import android.content.Context;

import ru.ppr.chit.data.db.migration.versions.base.BaseMigration;
import ru.ppr.database.Database;

/**
 * @author Max Sidorov
 */
public class MigrationV3 extends BaseMigration {

    public MigrationV3(Context context) {
        super(context);
    }

    @Override
    protected void migrateInternal(Database db) throws Exception {
        execFromAssets(db);
    }

    @Override
    public int getVersionNumber() {
        return 3;
    }

    @Override
    public String getVersionDescription() {
        return "Переименование поля exemptionCode -> exemptionExpressCode в сущностях [Ticket, TicketData]";
    }
}
