package ru.ppr.cppk.db.migration;

import android.content.Context;
import android.content.SharedPreferences;

import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.cppk.di.Di;
import ru.ppr.database.Database;

/**
 * @author Grigoriy Kashka
 */
public class MigrationVV89 extends MigrationBase {

    public MigrationVV89(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        //это лишнее, т.к. изменения в локальной базе не требуются
        //migrateWithScriptFromAssets(localDB);

        SharedPreferences preferences = Di.INSTANCE.getApp().getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        //возьмем модель принтера
        String model = preferences.getString(GlobalConstants.CashRegisterModel, null);
        if (model!=null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(GlobalConstants.FISCAL_MODEL, model);
            editor.apply();
        }
    }

    @Override
    public int getVersionNumber() {
        return 89;
    }

    @Override
    public String getVersionDescription() {
        return "Заполняем поле FiscalModel в SharedPreferences. Локальную базу не трогаем";
    }

}