package ru.ppr.nsi.dao;

import android.database.Cursor;
import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.StationToTariffZone;

/**
 * DAO для таблицы НСИ <i>StationsToTariffZones</i>.
 */
public class StationToTariffZoneDao extends BaseEntityDao<StationToTariffZone, Long> {

    public static final String TABLE_NAME = "StationsToTariffZones";

    public static class Properties {
        public static final String StationCode = "StationCode";
        public static final String TariffZoneCode = "TariffZoneCode";
        public static final String IsPrimaryStation = "IsPrimaryStation";
    }

    public StationToTariffZoneDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public StationToTariffZone fromCursor(Cursor cursor) {

        StationToTariffZone stationToTariffZone = new StationToTariffZone();

        stationToTariffZone.setStationCode(cursor.getLong(cursor.getColumnIndex(Properties.StationCode)));
        stationToTariffZone.setTariffZoneCode(cursor.getLong(cursor.getColumnIndex(Properties.TariffZoneCode)));
        stationToTariffZone.setPrimaryStation(cursor.getInt(cursor.getColumnIndex(Properties.IsPrimaryStation))!= 0);

        //добавим базовые поля
        addBaseNSIData(stationToTariffZone, Long.class, cursor);

        return stationToTariffZone;
    }
}
