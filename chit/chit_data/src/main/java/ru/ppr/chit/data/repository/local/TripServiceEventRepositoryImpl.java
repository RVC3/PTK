package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.TripServiceEventMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.chit.localdb.entity.TripServiceEventEntity;
import ru.ppr.chit.localdb.greendao.TripServiceEventEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class TripServiceEventRepositoryImpl extends BaseCrudLocalDbRepository<TripServiceEvent, TripServiceEventEntity, Long> implements TripServiceEventRepository {

    @Inject
    TripServiceEventRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<TripServiceEventEntity, Long> dao() {
        return daoSession().getTripServiceEventEntityDao();
    }

    @Override
    protected LocalDbMapper<TripServiceEvent, TripServiceEventEntity> mapper() {
        return TripServiceEventMapper.INSTANCE;
    }

    @Override
    public TripServiceEvent loadLast() {
        TripServiceEventEntity entity = dao().queryBuilder()
                .orderDesc(TripServiceEventEntityDao.Properties.Id)
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

    @Override
    public TripServiceEvent loadFirstByTripUuid(@NonNull String tripUuid) {
        TripServiceEventEntity entity = dao().queryBuilder()
                .where(TripServiceEventEntityDao.Properties.TripUuid.eq(tripUuid))
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

}
