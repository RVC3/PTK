package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;
import android.support.v4.util.Pair;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.sync.kpp.model.Tariff;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.TariffDao;
import ru.ppr.utils.cache.Cache;
import ru.ppr.utils.cache.LruCache;

/**
 * @author Aleksandr Brazhkin
 */
public class TariffLoader extends BaseLoader {

    private final Cache<Pair<Long, Integer>, Pair<Tariff, Integer>> cache = new LruCache<>(100);
    private int putToCacheCount = 0;
    private int getFromCacheCount = 0;
    private final String loadTariffQuery;

    public TariffLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadTariffQuery = buildLoadTariffQuery();
    }

    public Pair<Tariff, Integer> loadTariff(long code, int versionId) {

        Pair<Long, Integer> cacheKey = new Pair<>(code, versionId);
        Pair<Tariff, Integer> tariffInCache = cache.get(cacheKey);
        if (tariffInCache != null) {
            getFromCacheCount++;
            return tariffInCache;
        }

        String[] selectionArgs = new String[]{
                String.valueOf(code),
                String.valueOf(versionId)
        };

        Pair<Tariff, Integer> tariffPair = null;
        Cursor cursor = null;
        try {
            cursor = nsiDaoSession.getNsiDb().rawQuery(loadTariffQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                int realVersionId = cursor.getInt(0);

                Tariff tariff = new Tariff();
                tariff.TariffCode = cursor.getInt(1);
                tariff.TariffPlanCode = cursor.getInt(2);
                tariff.RouteCode = cursor.getString(3);

                // костыль, если тариф уже удален, то подменим ему версию на последнюю неудаленную
                if (!cursor.isNull(4)) {
                    int deleteInVersionId = cursor.getInt(4);
                    if (versionId >= deleteInVersionId) {
                        realVersionId = deleteInVersionId - 1;
                    }
                }

                tariffPair = new Pair<>(tariff, realVersionId);
                cache.put(cacheKey, tariffPair);
                putToCacheCount++;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tariffPair;
    }

    public int getGetFromCacheCount() {
        return getFromCacheCount;
    }

    public int getPutToCacheCount() {
        return putToCacheCount;
    }

    private String buildLoadTariffQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(BaseEntityDao.Properties.VersionId).append(", ");
        sb.append(BaseEntityDao.Properties.Code).append(", ");
        sb.append(TariffDao.Properties.TariffPlanCode).append(", ");
        sb.append(TariffDao.Properties.RouteCode).append(", ");
        sb.append(BaseEntityDao.Properties.DeleteInVersionId);
        sb.append(" FROM ");
        sb.append(TariffDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Code).append(" = ").append("?");
        sb.append(" AND ");
        sb.append(BaseEntityDao.Properties.VersionId).append(" <= ").append("?");
        sb.append(" ORDER BY ").append(BaseEntityDao.Properties.VersionId).append(" DESC LIMIT 1");
        return sb.toString();
    }
}
