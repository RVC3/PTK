package ru.ppr.chit.data.db.migration.versions;

import android.content.Context;

import ru.ppr.chit.data.db.migration.versions.base.BaseMigration;
import ru.ppr.database.Database;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class MigrationV2 extends BaseMigration {

    public MigrationV2(Context context) {
        super(context);
    }

    @Override
    protected void migrateInternal(Database db) {
        Logger.trace(getTag(), "migrate");
    }

    @Override
    public int getVersionNumber() {
        return 2;
    }

    @Override
    public String getVersionDescription() {
        return "Пробная миграция без изменений структуры";
    }
}
