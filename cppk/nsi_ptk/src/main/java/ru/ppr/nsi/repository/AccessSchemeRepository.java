package ru.ppr.nsi.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.entity.AccessScheme;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class AccessSchemeRepository extends BaseRepository<AccessScheme, Integer> {

    @Inject
    AccessSchemeRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<AccessScheme, Integer> selfDao() {
        return daoSession().getAccessSchemeDao();
    }

}
