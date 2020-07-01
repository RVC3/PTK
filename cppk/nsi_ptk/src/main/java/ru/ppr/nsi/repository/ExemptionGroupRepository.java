package ru.ppr.nsi.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.entity.ExemptionGroup;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class ExemptionGroupRepository extends BaseRepository<ExemptionGroup, Integer> {

    @Inject
    ExemptionGroupRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<ExemptionGroup, Integer> selfDao() {
        return daoSession().getExemptionGroupDao();
    }

}
