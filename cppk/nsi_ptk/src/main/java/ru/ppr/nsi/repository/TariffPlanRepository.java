package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.TariffPlanDao;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class TariffPlanRepository extends BaseRepository<TariffPlan, Integer> {

    @Inject
    TariffPlanRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<TariffPlan, Integer> selfDao() {
        return daoSession().getTariffPlanDao();
    }

    @Override
    public TariffPlan load(Integer code, int versionId) {
        TariffPlan loaded = super.load(code, versionId);

        if (loaded != null) {
            loaded.setVersionId(versionId);
        }

        return loaded;
    }

    @Override
    public List<TariffPlan> loadAll(Iterable<Long> codes, int versionId) {
        List<TariffPlan> loaded = super.loadAll(codes, versionId);

        for (TariffPlan tariffPlan : loaded) {
            tariffPlan.setVersionId(versionId);
        }

        return loaded;
    }

    /**
     * Выполняет поиск тарифных планов, удовлетворяющих данным условиям.
     *
     * @param trainCategoryCode Код категории поезда
     * @param isSurcharge       {@code true}, если ищет тарифный план доплаты, {@code false} - иначе
     * @param versionId         Версия НСИ
     * @return Список тарифных планоа
     */
    @NonNull
    public List<TariffPlan> getTariffPlans(@Nullable Long trainCategoryCode, boolean isSurcharge, int versionId) {

        QueryBuilder qb = new QueryBuilder();
        qb.selectAll().from(TariffPlanDao.TABLE_NAME);
        qb.where().appendRaw(NsiUtils.checkVersion(TariffPlanDao.TABLE_NAME, versionId));
        qb.and().field(TariffPlanDao.Properties.IsSurcharge).eq(isSurcharge ? 1 : 0);
        if (trainCategoryCode != null) {
            qb.and().field(TariffPlanDao.Properties.TrainCategoryCode).eq(trainCategoryCode);
        }

        List<TariffPlan> tariffPlans = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = qb.build().run(daoSession().getNsiDb());
            while (cursor.moveToNext()) {
                TariffPlan tariffPlan = daoSession().getTariffPlanDao().readEntity(cursor, 0);
                tariffPlan.setVersionId(versionId);
                tariffPlans.add(tariffPlan);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return tariffPlans;
    }

    /**
     * Выполняет поиск тарифных планов, удовлетворяющих данным условиям.
     *
     * @param trainCategoryCode Код категории поезда
     * @param isSurcharge       {@code true}, если ищет тарифный план доплаты, {@code false} - иначе
     * @return Список тарифных планоа
     */
    @NonNull
    public List<TariffPlan> getTariffPlans(long trainCategoryCode, boolean isSurcharge, int nsiVersion) {
        return getTariffPlans(trainCategoryCode == -1 ? null : trainCategoryCode, isSurcharge, nsiVersion);
    }

}
