package ru.ppr.nsi.repository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class TicketCategoryRepository extends BaseRepository<TicketCategory, Integer> {

    @Inject
    TicketCategoryRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<TicketCategory, Integer> selfDao() {
        return daoSession().getTicketCategoryDao();
    }

    @Override
    public TicketCategory load(Integer code, int versionId) {
        TicketCategory loaded = super.load(code, versionId);

        if (loaded != null) {
            loaded.setVersionId(versionId);
        }

        return loaded;
    }

    @Override
    public List<TicketCategory> loadAll(Iterable<Long> codes, int versionId) {
        List<TicketCategory> loaded = super.loadAll(codes, versionId);

        for (TicketCategory ticketCategory : loaded) {
            ticketCategory.setVersionId(versionId);
        }

        return loaded;
    }

}
