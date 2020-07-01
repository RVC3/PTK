package ru.ppr.nsi.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.entity.Carrier;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class CarrierRepository extends BaseRepository<Carrier, String> {

    @Inject
    CarrierRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<Carrier, String> selfDao() {
        return daoSession().getCarrierDao();
    }

}
