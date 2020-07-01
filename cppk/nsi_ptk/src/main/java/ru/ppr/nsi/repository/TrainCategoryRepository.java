package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.TrainCategoryDao;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.entity.TrainCategoryPrefix;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class TrainCategoryRepository extends BaseRepository<TrainCategory, Integer> {

    @Inject
    TrainCategoryRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<TrainCategory, Integer> selfDao() {
        return daoSession().getTrainCategoryDao();
    }

    @Override
    public TrainCategory load(Integer code, int versionId) {
        TrainCategory loaded = super.load(code, versionId);

        if (loaded != null) {
            loaded.setVersionId(versionId);
        }

        return loaded;
    }

    @Override
    public List<TrainCategory> loadAll(Iterable<Long> codes, int versionId) {
        List<TrainCategory> loaded = super.loadAll(codes, versionId);

        for (TrainCategory trainCategory : loaded) {
            trainCategory.setVersionId(versionId);
        }

        return loaded;
    }

    /**
     * Возвращает первую категорию из БД с заданным префиксом, определенной версии НСИ.
     *
     * @param prefix
     * @return
     */
    public TrainCategory getTrainCategoryToPrefix(@NonNull TrainCategoryPrefix prefix, int nsiVersion) {
        Cursor cursor = null;
        TrainCategory object = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select * from ").append(TrainCategoryDao.TABLE_NAME).append(" where ").append(TrainCategoryDao.Properties.Prefix)
                .append(" = ").append(prefix.getCode()).append(" AND ").append(BaseEntityDao.Properties.VersionId).append(" <= ").append(nsiVersion)
                .append(" AND ").append("(").append(BaseEntityDao.Properties.DeleteInVersionId).append(" > ").append(nsiVersion).append(" OR ")
                .append(BaseEntityDao.Properties.DeleteInVersionId).append(" is NULL)");
        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                object = daoSession().getTrainCategoryDao().fromCursor(cursor);
                object.setVersionId(nsiVersion);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return object;
    }

}
