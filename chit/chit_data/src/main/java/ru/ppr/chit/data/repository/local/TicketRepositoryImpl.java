package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.TicketMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.Ticket;
import ru.ppr.chit.domain.repository.local.TicketRepository;
import ru.ppr.chit.localdb.entity.TicketEntity;
import ru.ppr.chit.localdb.greendao.TicketEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class TicketRepositoryImpl extends BaseCrudLocalDbRepository<Ticket, TicketEntity, Long> implements TicketRepository {

    @Inject
    TicketRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<TicketEntity, Long> dao() {
        return daoSession().getTicketEntityDao();
    }

    @Override
    protected LocalDbMapper<Ticket, TicketEntity> mapper() {
        return TicketMapper.INSTANCE;
    }

    @Nullable
    @Override
    public Ticket loadByTicket(@NonNull Long ticketIdId) {
        TicketEntity entity = dao().queryBuilder()
                .where(TicketEntityDao.Properties.TicketIdId.eq(ticketIdId))
                .unique();
        return mapper().entityToModel(entity);
    }

}
