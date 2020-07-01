package ru.ppr.chit.data.repository.local;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.EventMapper;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.localdb.entity.EventEntity;

/**
 * @author Dmitry Nevolin
 */
public class EventRepositoryImpl extends BaseCrudLocalDbRepository<Event, EventEntity, Long> implements EventRepository {

    @Inject
    EventRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<EventEntity, Long> dao() {
        return daoSession().getEventEntityDao();
    }

    @Override
    protected LocalDbMapper<Event, EventEntity> mapper() {
        return EventMapper.INSTANCE;
    }

}
