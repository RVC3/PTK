package ru.ppr.cppk.db.migration;

import android.content.Context;
import android.content.SharedPreferences;

import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV87 extends MigrationBase {

    public MigrationVV87(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);

        SharedPreferences preferences = Di.INSTANCE.getApp().getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        int printerMode = preferences.getInt(GlobalConstants.PRINTER_MODE, PrinterManager.PRINTER_MODE_MOEBIUS_REAL);
        //если был привязан штрих и у него был задан номер ЭКЛЗ вместо ФП, тогда заменим данные в SharedPreferences
        if (printerMode == PrinterManager.PRINTER_MODE_SHTRIH) {
            SharedPreferences.Editor editor = preferences.edit();
            //тоже сотрем, поскольку ничего хорошего там быть не может, только захардкоженное 1234567890
            editor.putString(GlobalConstants.CashRegisterEklsNumber, null);
            editor.putString(GlobalConstants.CashRegisterFnSerial, null);
            editor.apply();
        }

    }

    @Override
    public int getVersionNumber() {
        return 87;
    }

    @Override
    public String getVersionDescription() {
        return "Добавляем колонку FNSerial в таблицу CashRegister";
    }

}