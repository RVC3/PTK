package ru.ppr.cppk.sync.loader.model.local;

import android.database.Cursor;
import android.support.v4.util.Pair;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.utils.cache.Cache;
import ru.ppr.utils.cache.LruCache;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.kpp.model.local.TariffPlan;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.TariffPlanDao;

/**
 * @author Grigoriy Kashka
 */
public class TariffPlanLoader extends BaseLoader {

    private final Cache<Pair<Long, Integer>, Pair<TariffPlan, Integer>> cache = new LruCache<>(100);
    private int putToCacheCount = 0;
    private int getFromCacheCount = 0;
    private final String loadTariffPlanQuery;

    public TariffPlanLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadTariffPlanQuery = buildLoadTariffPlanQuery();
    }

    public Pair<TariffPlan, Integer> loadTariffPlan(long code, int versionId) {

        Pair<Long, Integer> cacheKey = new Pair<>(code, versionId);
        Pair<TariffPlan, Integer> tariffPlanInCache = cache.get(cacheKey);
        if (tariffPlanInCache != null) {
            getFromCacheCount++;
            return tariffPlanInCache;
        }

        String[] selectionArgs = new String[]{
                String.valueOf(code),
                String.valueOf(versionId),
                String.valueOf(versionId)
        };

        Pair<TariffPlan, Integer> tariffPlanPair = null;
        Cursor cursor = null;
        try {
            cursor = nsiDaoSession.getNsiDb().rawQuery(loadTariffPlanQuery, selectionArgs);
            if (cursor.moveToFirst()) {

                int realVersionId = cursor.getInt(0);

                TariffPlan tariffPlan = new TariffPlan();
                tariffPlan.code = cursor.getInt(1);
                tariffPlan.isSurcharge = cursor.getInt(2) == 1;

                tariffPlanPair = new Pair<>(tariffPlan, realVersionId);
                cache.put(cacheKey, tariffPlanPair);
                putToCacheCount++;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tariffPlanPair;
    }

    public int getGetFromCacheCount() {
        return getFromCacheCount;
    }

    public int getPutToCacheCount() {
        return putToCacheCount;
    }

    private String buildLoadTariffPlanQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(BaseEntityDao.Properties.VersionId).append(", ");
        sb.append(BaseEntityDao.Properties.Code).append(", ");
        sb.append(TariffPlanDao.Properties.IsSurcharge);
        sb.append(" FROM ");
        sb.append(TariffPlanDao.TABLE_NAME);
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
