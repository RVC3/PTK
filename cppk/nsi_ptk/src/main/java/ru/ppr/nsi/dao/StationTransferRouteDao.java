package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.StationTransferRoute;

/**
 * @author Dmitry Nevolin
 */
public class StationTransferRouteDao extends BaseEntityDao<StationTransferRoute, Long> {

    public static final String TABLE_NAME = "StationTransferRoutes";

    public static class Properties {
        public static final String StationCode = "StationCode";
        public static final String RouteCode = "RouteCode";
    }

    public StationTransferRouteDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public StationTransferRoute fromCursor(Cursor cursor) {
        StationTransferRoute stationTransferRoute = new StationTransferRoute();

        int index = cursor.getColumnIndex(StationTransferRouteDao.Properties.StationCode);
        if (index != -1)
            stationTransferRoute.setStationCode(cursor.getLong(index));

        index = cursor.getColumnIndex(StationTransferRouteDao.Properties.RouteCode);
        if (index != -1)
            stationTransferRoute.setRouteCode(cursor.getLong(index));

        return stationTransferRoute;
    }

}
