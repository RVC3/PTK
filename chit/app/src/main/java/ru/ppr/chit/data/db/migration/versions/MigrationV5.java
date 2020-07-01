package ru.ppr.chit.data.db.migration.versions;

import android.content.Context;

import ru.ppr.chit.data.db.migration.versions.base.BaseMigration;
import ru.ppr.database.Database;

/**
 * Прописывает в события посадок станцию контроля из билета, там где она не была указана
 *
 * @author Max Sidorov
 */
public class MigrationV5 extends BaseMigration {

    public MigrationV5(Context context) {
        super(context);
    }

    @Override
    protected void migrateInternal(Database db) throws Exception {
        execFromAssets(db);
    }

    @Override
    public int getVersionNumber() {
        return 5;
    }

    @Override
    public String getVersionDescription() {
        return "Прописывает в события посадок станцию контроля из билета, там где она не была указана";
    }
}
