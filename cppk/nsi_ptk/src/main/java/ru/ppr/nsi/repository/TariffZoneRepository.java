package ru.ppr.nsi.repository;

import javax.inject.Inject;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.entity.TariffZone;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * Репозиторий для {@link TariffZone}.
 */
public class TariffZoneRepository extends BaseRepository {
    @Inject
    TariffZoneRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<TariffZone, Long> selfDao() {
        return daoSession().getTariffZoneDao();
    }
}
