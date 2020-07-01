package ru.ppr.cppk.db.migration;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;

import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.db.local.PrivateSettingsDao;
import ru.ppr.cppk.db.migration.base.MigrationBase;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.database.Database;

/**
 * Created by Григорий on 17.03.2017.
 */

public class MigrationVV72 extends MigrationBase {

    public MigrationVV72(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
        //достанем код станции из SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        int stationCode = preferences.getInt(GlobalConstants.currentBindingStation, -1);
        //добавим значение в базу
        addSaleStationCode(localDB, stationCode);
        //удалим эту пару из SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(GlobalConstants.currentBindingStation);
        editor.apply();
    }

    @Override
    public int getVersionNumber() {
        return 72;
    }

    @Override
    public String getVersionDescription() {
        return "Переделка таблицы PtkPrivateSettings на ключ-значение";
    }

    /**
     * Добавляет в таблицу с приватными настройками станцию привязки ПТК
     */
    private static void addSaleStationCode(Database localDB, int stationCode) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PrivateSettingsDao.Properties.Name, PrivateSettings.Entities.SALE_STATION_CODE);
        contentValues.put(PrivateSettingsDao.Properties.Value, stationCode);
        long id = localDB.insert(PrivateSettingsDao.TABLE_NAME, null, contentValues);
        if (id < 0)
            throw new Exception("could not add SALE_STATION_CODE to PtkSettingsPrivate table");
    }


}