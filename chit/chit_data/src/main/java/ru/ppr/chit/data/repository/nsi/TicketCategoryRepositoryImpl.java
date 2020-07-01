package ru.ppr.chit.data.repository.nsi;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.mapper.nsi.NsiDbMapper;
import ru.ppr.chit.data.mapper.nsi.TicketCategoryMapper;
import ru.ppr.chit.data.repository.nsi.base.BaseCvdNsiDbRepository;
import ru.ppr.chit.domain.model.nsi.TicketCategory;
import ru.ppr.chit.domain.repository.nsi.TicketCategoryRepository;
import ru.ppr.chit.nsidb.entity.TicketCategoryEntity;

/**
 * @author Aleksandr Brazhkin
 */
public class TicketCategoryRepositoryImpl extends BaseCvdNsiDbRepository<TicketCategory, TicketCategoryEntity, Long> implements TicketCategoryRepository {

    @Inject
    TicketCategoryRepositoryImpl(NsiDbManager nsiDbManager) {
        super(nsiDbManager);
    }

    @Override
    protected AbstractDao<TicketCategoryEntity, Void> dao() {
        return daoSession().getTicketCategoryEntityDao();
    }

    @Override
    protected NsiDbMapper<TicketCategory, TicketCategoryEntity> mapper() {
        return TicketCategoryMapper.INSTANCE;
    }

}
