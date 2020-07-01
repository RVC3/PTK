package ru.ppr.nsi.repository;

import android.database.Cursor;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.VersionDao;
import ru.ppr.nsi.entity.Version;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * Репозиторий версий НСИ
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class VersionRepository extends BaseRepository<Version, Integer> {

    private static final String TAG = Logger.makeLogTag(VersionRepository.class);

    @Inject
    VersionRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<Version, Integer> selfDao() {
        return daoSession().getVersionDao();
    }

    /**
     * Возвращает версию НСИ по Id
     * Не использовать для логики!
     */
    public Version getVersionById(int versionId) {
        QueryBuilder qb = new QueryBuilder();
        qb.selectAll().from(VersionDao.TABLE_NAME);
        qb.where().field(VersionDao.Properties.VersionId).eq(versionId);

        qb.limit(1);

        Query query = qb.build();
        Version version = null;
        Cursor cursor = null;

        try {
            cursor = query.run(daoSession().getNsiDb());

            if (cursor.moveToFirst()) {
                version = daoSession().getVersionDao().readEntity(cursor, 0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return version;
    }

    /**
     * Возвращает версию НСИ в указанный момент времени.
     * Не использовать для логики!
     */
    public Version getVersionForDate(Date date, @Version.Status int[] nsiStatuses) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("*");
        sb.append(" FROM ");
        sb.append(VersionDao.TABLE_NAME);
        sb.append(" WHERE 1=1 ");

        if (nsiStatuses != null && nsiStatuses.length > 0) {
            sb.append(" AND ");
            sb.append(VersionDao.Properties.Status).append(" in (");
            for (int i = 0; i < nsiStatuses.length; i++) {
                if (i != 0) sb.append(", ");
                sb.append(nsiStatuses[i]);
            }
            sb.append(") ");
        }

        if (date != null) {
            sb.append(" AND ");
            sb.append(VersionDao.Properties.StartingDateTime);
            sb.append(" < ");
            // http://www.sqlite.org/lang_datefunc.html
            // https://aj.srvdev.ru/browse/CPPKPP-27999
            sb.append("(SELECT datetime(").append(date.getTime() / 1000).append(", 'unixepoch'))");
        }
        sb.append(" ORDER BY ").append(VersionDao.Properties.VersionId).append(" DESC ");
        sb.append(" LIMIT ").append(1);

        Version version = null;
        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(sb.toString(), null);
            if (cursor.moveToFirst()) {
                version = daoSession().getVersionDao().readEntity(cursor, 0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return version;
    }

    /**
     * Возвращает масксимальный Id версии НСИ
     */
    public int getMaxVersionId() {

        StringBuilder stringBuilder = new StringBuilder();
        int versionId = -1;

        stringBuilder.append("SELECT MAX(");
        stringBuilder.append(VersionDao.Properties.VersionId);
        stringBuilder.append(") FROM ");
        stringBuilder.append(VersionDao.TABLE_NAME);

        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    versionId = cursor.getInt(0);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (versionId == -1) {
            Logger.trace(TAG, "Incorrect result from database while select NSI version");
        }

        return versionId;
    }

    /**
     * Возвращает Id версии НСИ в указанный момент времени.
     * Не использовать для логики!
     */
    public int getVersionIdForDate(Date date, @Version.Status int[] nsiStatuses) {

        StringBuilder stringBuilder = new StringBuilder();
        int versionId = -1;

        stringBuilder.append("SELECT ");
        stringBuilder.append("MAX(VersionId)");
        stringBuilder.append(" FROM ");
        stringBuilder.append(VersionDao.TABLE_NAME);
        stringBuilder.append(" WHERE 1=1 ");

        if (nsiStatuses != null && nsiStatuses.length > 0) {
            stringBuilder.append(" AND ");
            stringBuilder.append(VersionDao.Properties.Status).append(" in (");
            for (int i = 0; i < nsiStatuses.length; i++) {
                if (i != 0) stringBuilder.append(", ");
                stringBuilder.append(nsiStatuses[i]);
            }
            stringBuilder.append(") ");
        }

        if (date != null) {
            stringBuilder.append(" AND ");
            stringBuilder.append(VersionDao.Properties.StartingDateTime);
            stringBuilder.append(" < ");
            stringBuilder.append("(SELECT datetime(").append(date.getTime() / 1000).append(", 'unixepoch'))");
        }
        // http://www.sqlite.org/lang_datefunc.html
        // https://aj.srvdev.ru/browse/CPPKPP-27999

        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    versionId = cursor.getInt(0);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (versionId == -1) {
            Logger.trace(TAG, "Incorrect result from database while select NSI version");
        }

        return versionId;
    }

}
