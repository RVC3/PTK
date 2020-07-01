package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;
import android.support.v4.util.Pair;

import java.math.BigDecimal;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.utils.cache.Cache;
import ru.ppr.utils.cache.LruCache;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.kpp.model.local.TicketType;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.TicketTypeDao;

/**
 * @author Grigoriy Kashka
 */
public class TicketTypeLoader extends BaseLoader {

    private final Cache<Pair<Long, Integer>, Pair<TicketType, Integer>> cache = new LruCache<>(100);
    private int putToCacheCount = 0;
    private int getFromCacheCount = 0;
    private final String loadTicketTypeQuery;

    public TicketTypeLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadTicketTypeQuery = buildLoadTicketTypeQuery();
    }

    public Pair<TicketType, Integer> loadTicketType(long code, int versionId) {

        Pair<Long, Integer> cacheKey = new Pair<>(code, versionId);
        Pair<TicketType, Integer> ticketTypeInCache = cache.get(cacheKey);
        if (ticketTypeInCache != null) {
            getFromCacheCount++;
            return ticketTypeInCache;
        }

        String[] selectionArgs = new String[]{
                String.valueOf(code),
                String.valueOf(versionId),
                String.valueOf(versionId)
        };

        Pair<TicketType, Integer> TicketTypePair = null;
        Cursor cursor = null;
        try {
            cursor = nsiDaoSession.getNsiDb().rawQuery(loadTicketTypeQuery, selectionArgs);
            if (cursor.moveToFirst()) {

                int realVersionId = cursor.getInt(0);

                TicketType TicketType = new TicketType();
                TicketType.code = cursor.getInt(1);
                TicketType.tax = cursor.isNull(2) ? null : new BigDecimal(cursor.getString(2));

                TicketTypePair = new Pair<>(TicketType, realVersionId);
                cache.put(cacheKey, TicketTypePair);
                putToCacheCount++;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return TicketTypePair;
    }

    public int getGetFromCacheCount() {
        return getFromCacheCount;
    }

    public int getPutToCacheCount() {
        return putToCacheCount;
    }

    private String buildLoadTicketTypeQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(BaseEntityDao.Properties.VersionId).append(", ");
        sb.append(BaseEntityDao.Properties.Code).append(", ");
        sb.append(TicketTypeDao.Properties.Tax);
        sb.append(" FROM ");
        sb.append(TicketTypeDao.TABLE_NAME);
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