package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;
import android.util.Pair;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.utils.cache.Cache;
import ru.ppr.utils.cache.LruCache;
import ru.ppr.cppk.sync.kpp.model.Station;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.StationDao;

/**
 * @author Aleksandr Brazhkin
 */
public class StationLoader extends BaseLoader {

    private final Cache<Pair<Long, Integer>, Station> cache = new LruCache<>(100);
    private int putToCacheCount = 0;
    private int getFromCacheCount = 0;
    private final String loadStationQuery;

    public StationLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadStationQuery = buildLoadStationQuery();
    }

    public Station loadStation(long code, int versionId) {
        Pair<Long, Integer> cacheKey = new Pair<>(code, versionId);
        Station stationInCache = cache.get(cacheKey);
        if (stationInCache != null) {
            getFromCacheCount++;
            return stationInCache;
        }

        String[] selectionArgs = new String[]{
                String.valueOf(code),
                String.valueOf(versionId),
                String.valueOf(versionId)
        };

        Station station = null;
        Cursor cursor = null;
        try {
            cursor = nsiDaoSession.getNsiDb().rawQuery(loadStationQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                station = new Station();
                station.Code = cursor.getString(0);
                station.Name = cursor.getString(1);
                station.ShortName = cursor.getString(2);
                cache.put(cacheKey, station);
                putToCacheCount++;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return station;
    }

    public int getGetFromCacheCount() {
        return getFromCacheCount;
    }

    public int getPutToCacheCount() {
        return putToCacheCount;
    }

    private String buildLoadStationQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(BaseEntityDao.Properties.Code).append(", ");
        sb.append(StationDao.Properties.Name).append(", ");
        sb.append(StationDao.Properties.ShortName);
        sb.append(" FROM ");
        sb.append(StationDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Code).append(" = ").append("?");
        sb.append(" AND ");
        sb.append(BaseEntityDao.Properties.VersionId).append(" <= ").append("?");
        sb.append(" AND ");
        sb.append(" ( ");
        {
            sb.append(BaseEntityDao.Properties.DeleteInVersionId).append(" > ").append("?");
            sb.append(" OR ");
            sb.append(BaseEntityDao.Properties.DeleteInVersionId).append(" IS NULL");
        }
        sb.append(" ) ");
        return sb.toString();
    }
}
