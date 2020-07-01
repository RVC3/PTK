package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV84 extends MigrationBase {

    public MigrationVV84(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 84;
    }

    @Override
    public String getVersionDescription() {
        return "добавляем таблицу CouponReadEvent и добавляем в CPPKTicketSales поле CouponReadEventId";
    }

}