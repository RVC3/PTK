package ru.ppr.nsi.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.entity.Direction;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Aleksandr Brazhkin
 */
@Singleton
public class DirectionRepository extends BaseRepository<Direction, Long> {

    @Inject
    DirectionRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<Direction, Long> selfDao() {
        return daoSession().getDirectionDao();
    }

}
