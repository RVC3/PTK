package ru.ppr.cppk.sync.loader.model.local;

import android.database.Cursor;
import android.support.v4.util.Pair;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.utils.cache.Cache;
import ru.ppr.utils.cache.LruCache;
import ru.ppr.cppk.sync.kpp.model.local.Fine;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.FineDao;

/**
 * @author Grigoriy Kashka
 */
public class FineLoader extends BaseLoader {

    private final Cache<Pair<Long, Integer>, Pair<Fine, Integer>> cache = new LruCache<>(20);
    private int putToCacheCount = 0;
    private int getFromCacheCount = 0;
    private final String loadFineQuery;

    public static final String TABLE_NAME = FineDao.TABLE_NAME;

    public static class Columns {
        static final Column CODE = new Column(0, FineDao.Properties.Code);
        static final Column NDS_PERCENT = new Column(1, FineDao.Properties.NdsPercent);

        public static Column[] all = new Column[]{
                CODE,
                NDS_PERCENT
        };
    }

    public FineLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadFineQuery = buildLoadQuery();
    }

    public Pair<Fine, Integer> loadFine(long code, int versionId) {

        Pair<Long, Integer> cacheKey = new Pair<>(code, versionId);
        Pair<Fine, Integer> fineInCache = cache.get(cacheKey);
        if (fineInCache != null) {
            getFromCacheCount++;
            return fineInCache;
        }

        String[] selectionArgs = new String[]{
                String.valueOf(code),
                String.valueOf(versionId),
                String.valueOf(versionId)
        };

        Pair<Fine, Integer> finePair = null;
        Cursor cursor = null;
        try {
            cursor = nsiDaoSession.getNsiDb().rawQuery(loadFineQuery, selectionArgs);
            if (cursor.moveToFirst()) {

                int realVersionId = cursor.getInt(0);

                Fine fine = load(cursor, new Offset());

                finePair = new Pair<>(fine, realVersionId);
                cache.put(cacheKey, finePair);
                putToCacheCount++;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return finePair;
    }

    public Fine load(Cursor cursor, Offset offset) {
        Fine fine = new Fine();
        fine.code = cursor.getInt(offset.value + Columns.CODE.index);
        fine.ndsPercent = cursor.getInt(offset.value + Columns.NDS_PERCENT.index);
        offset.value += Columns.all.length;
        return fine;
    }

    public int getGetFromCacheCount() {
        return getFromCacheCount;
    }

    public int getPutToCacheCount() {
        return putToCacheCount;
    }

    private String buildLoadQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(createColumnsForSelect(TABLE_NAME, Columns.all));
        sb.append(" FROM ");
        sb.append(TABLE_NAME);
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
