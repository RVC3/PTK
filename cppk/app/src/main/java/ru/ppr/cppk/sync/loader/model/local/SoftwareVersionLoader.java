package ru.ppr.cppk.sync.loader.model.local;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.UpdateEventDao;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.utils.cache.Cache;
import ru.ppr.utils.cache.LruCache;

/**
 * @author Aleksandr Brazhkin
 */
public class SoftwareVersionLoader extends BaseLoader {

    private final Cache<Long, String> cache = new LruCache<>(20);
    private int putToCacheCount = 0;
    private int getFromCacheCount = 0;
    private final String loadSoftwareVersionQuery;

    public SoftwareVersionLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadSoftwareVersionQuery = buildLoadSoftwareVersionQuery();
    }

    public String load(long updateEventId) {
        String softwareVersionInCache = cache.get(updateEventId);
        if (softwareVersionInCache != null) {
            getFromCacheCount++;
            return softwareVersionInCache;
        }

        String[] selectionArgs = new String[]{String.valueOf(updateEventId)};

        String softwareVersion = null;
        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadSoftwareVersionQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                String loadedSoftwareVersionInCash = cursor.getString(0);
                cache.put(updateEventId, loadedSoftwareVersionInCash);
                putToCacheCount++;
                softwareVersion = loadedSoftwareVersionInCash;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return softwareVersion;
    }

    public int getGetFromCacheCount() {
        return getFromCacheCount;
    }

    public int getPutToCacheCount() {
        return putToCacheCount;
    }

    private String buildLoadSoftwareVersionQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(UpdateEventDao.Properties.Version);
        sb.append(" FROM ");
        sb.append(UpdateEventDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}
