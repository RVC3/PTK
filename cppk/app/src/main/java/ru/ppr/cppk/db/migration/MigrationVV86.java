package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.database.Database;

/**
 * @author Aleksandr Brazhkin
 */
public class MigrationVV86 extends MigrationBase {

    public MigrationVV86(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 86;
    }

    @Override
    public String getVersionDescription() {
        //см. http://agile.srvdev.ru/browse/CPPKPP-34653
        return "Удаляем все AuditTrailEvent которые ссылаются на FineSaleEvent со статусом 0 или 1, потому что у них нет чека";
    }

}