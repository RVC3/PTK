package ru.ppr.nsi.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.entity.ExemptionOrganization;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class ExemptionOrganizationRepository extends BaseRepository<ExemptionOrganization, String> {

    @Inject
    ExemptionOrganizationRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<ExemptionOrganization, String> selfDao() {
        return daoSession().getExemptionOrganizationDao();
    }

}
