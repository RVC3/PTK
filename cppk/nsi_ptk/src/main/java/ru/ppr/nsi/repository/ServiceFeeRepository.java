package ru.ppr.nsi.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.entity.ServiceFee;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class ServiceFeeRepository extends BaseRepository<ServiceFee, Long> {

    @Inject
    ServiceFeeRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<ServiceFee, Long> selfDao() {
        return daoSession().getServiceFeeDao();
    }

}
