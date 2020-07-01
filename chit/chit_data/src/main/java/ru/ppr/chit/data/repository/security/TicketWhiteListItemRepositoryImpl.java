package ru.ppr.chit.data.repository.security;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.SecurityDbManager;
import ru.ppr.chit.data.mapper.security.SecurityDbMapper;
import ru.ppr.chit.data.mapper.security.TicketIdMapper;
import ru.ppr.chit.data.mapper.security.TicketWhiteListItemMapper;
import ru.ppr.chit.data.repository.security.base.BaseSecurityDbRepository;
import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.model.security.TicketWhiteListItem;
import ru.ppr.chit.domain.repository.security.TicketWhiteListItemRepository;
import ru.ppr.chit.securitydb.entity.TicketWhiteListItemEntity;
import ru.ppr.chit.securitydb.greendao.TicketWhiteListItemEntityDao;

/**
 * @author Aleksandr Brazhkin
 */
public class TicketWhiteListItemRepositoryImpl extends BaseSecurityDbRepository<TicketWhiteListItem, TicketWhiteListItemEntity, Void> implements TicketWhiteListItemRepository {

    private final TicketIdMapper ticketIdMapper = TicketIdMapper.INSTANCE;

    @Inject
    TicketWhiteListItemRepositoryImpl(SecurityDbManager securityDbManager) {
        super(securityDbManager);
    }

    @Override
    protected AbstractDao<TicketWhiteListItemEntity, Void> dao() {
        return daoSession().getTicketWhiteListItemEntityDao();
    }

    @Override
    protected SecurityDbMapper<TicketWhiteListItem, TicketWhiteListItemEntity> mapper() {
        return TicketWhiteListItemMapper.INSTANCE;
    }

    @Override
    public TicketWhiteListItem loadByTicketId(TicketId ticketId) {
        String ticketIdEntity = ticketIdMapper.mapToEntity(ticketId);
        TicketWhiteListItemEntity entity = dao().queryBuilder()
                .where(TicketWhiteListItemEntityDao.Properties.TicketId.eq(ticketIdEntity))
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }
}
