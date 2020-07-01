package ru.ppr.nsi.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.entity.ExemptionsTo;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class ExemptionsToRepository extends BaseRepository<ExemptionsTo, Integer> {

    @Inject
    ExemptionsToRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<ExemptionsTo, Integer> selfDao() {
        return daoSession().getExemptionToDao();
    }

}
