package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.StationOnRoutes;

/**
 * DAO для таблицы НСИ <i>StationsOnRoutes</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class StationsOnRouteDao extends BaseEntityDao<StationOnRoutes, Integer> {

    public static final String TABLE_NAME = "StationsOnRoutes";

    public static class Properties {
        public static final String RouteCode = "RouteCode";
        public static final String StationNumber = "StationNumber";
        public static final String StationCode = "StationCode";
        public static final String DistanceFromBegining = "DistanceFromBegining";
        public static final String ZoneNumber = "ZoneNumber";
        public static final String IsTurnstileExist = "IsTurnstileExist";
        public static final String IsRailway = "IsRailway";
        public static final String IsHasNext = "IsHasNext";
        public static final String IsNoFracture = "IsNoFracture";
        public static final String IsTariffBorder = "IsTariffBorder";
        public static final String IsDepartureStation = "IsDepartureStation";
        public static final String IsDestinationStation = "IsDestinationStation";
    }

    public StationsOnRouteDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public StationOnRoutes fromCursor(Cursor cursor) {
        StationOnRoutes stationOnRoutes = new StationOnRoutes();

        int index = cursor.getColumnIndex(StationsOnRouteDao.Properties.DistanceFromBegining);
        if (index != -1)
            stationOnRoutes.setDistanceFromBegining(cursor.getInt(index));

        index = cursor.getColumnIndex(StationsOnRouteDao.Properties.IsDepartureStation);
        if (index != -1)
            stationOnRoutes.setDepartureStation(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(StationsOnRouteDao.Properties.IsDestinationStation);
        if (index != -1)
            stationOnRoutes.setDestinationStation(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(StationsOnRouteDao.Properties.IsHasNext);
        if (index != -1)
            stationOnRoutes.setHasNext(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(StationsOnRouteDao.Properties.IsNoFracture);
        if (index != -1)
            stationOnRoutes.setNoFracture(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(StationsOnRouteDao.Properties.IsRailway);
        if (index != -1)
            stationOnRoutes.setRailway(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(StationsOnRouteDao.Properties.IsTariffBorder);
        if (index != -1)
            stationOnRoutes.setTariffBorder(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(StationsOnRouteDao.Properties.IsTurnstileExist);
        if (index != -1)
            stationOnRoutes.setTurnstileExist(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(StationsOnRouteDao.Properties.RouteCode);
        if (index != -1)
            stationOnRoutes.setRouteCode(cursor.getInt(index));

        index = cursor.getColumnIndex(StationsOnRouteDao.Properties.StationCode);
        if (index != -1)
            stationOnRoutes.setStationCode(cursor.getInt(index));

        index = cursor.getColumnIndex(StationsOnRouteDao.Properties.StationNumber);
        if (index != -1)
            stationOnRoutes.setStationNumber(cursor.getInt(index));

        index = cursor.getColumnIndex(StationsOnRouteDao.Properties.ZoneNumber);
        if (index != -1)
            stationOnRoutes.setZoneNumber(cursor.getInt(index));

        return stationOnRoutes;
    }
}
