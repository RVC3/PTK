package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.query.Join;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.TicketControlExportEventMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.TicketControlExportEvent;
import ru.ppr.chit.domain.repository.local.TicketControlExportEventRepository;
import ru.ppr.chit.localdb.entity.BoardingEventEntity;
import ru.ppr.chit.localdb.entity.TicketControlEventEntity;
import ru.ppr.chit.localdb.entity.TicketControlExportEventEntity;
import ru.ppr.chit.localdb.greendao.BoardingEventEntityDao;
import ru.ppr.chit.localdb.greendao.TicketControlEventEntityDao;
import ru.ppr.chit.localdb.greendao.TicketControlExportEventEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class TicketControlExportEventRepositoryImpl extends BaseCrudLocalDbRepository<TicketControlExportEvent, TicketControlExportEventEntity, Long> implements TicketControlExportEventRepository {

    @Inject
    TicketControlExportEventRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<TicketControlExportEventEntity, Long> dao() {
        return daoSession().getTicketControlExportEventEntityDao();
    }

    @Override
    protected LocalDbMapper<TicketControlExportEvent, TicketControlExportEventEntity> mapper() {
        return TicketControlExportEventMapper.INSTANCE;
    }

    @NonNull
    @Override
    public List<TicketControlExportEvent> loadAllByBoardingEvent(@NonNull Long boardingEventId) {
        QueryBuilder<TicketControlExportEventEntity> qb = dao().queryBuilder();
        Join ticketControlEvent = qb.join(
                TicketControlExportEventEntityDao.Properties.TicketControlEventId,
                TicketControlEventEntity.class);
        Join boardingEvent = qb.join(
                ticketControlEvent,
                TicketControlEventEntityDao.Properties.BoardingEventId,
                BoardingEventEntity.class,
                BoardingEventEntityDao.Properties.Id);
        boardingEvent.where(BoardingEventEntityDao.Properties.Id.eq(boardingEventId));
        return mapper().entityListToModelList(qb.list());
    }

}
