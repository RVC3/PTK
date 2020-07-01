package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV98 extends MigrationBase {

    public MigrationVV98(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 98;
    }

    @Override
    public String getVersionDescription() {
        return "Удаляем лишние записи из таблицы Fee. Чистим ссылки на них в TicketSaleReturnEventBase";
    }

}