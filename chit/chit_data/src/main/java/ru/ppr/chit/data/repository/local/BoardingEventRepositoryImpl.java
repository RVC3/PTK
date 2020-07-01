package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.BoardingEventMapper;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.domain.repository.local.BoardingEventRepository;
import ru.ppr.chit.localdb.entity.BoardingEventEntity;
import ru.ppr.chit.localdb.entity.BoardingExportEventEntity;
import ru.ppr.chit.localdb.entity.TripServiceEventEntity;
import ru.ppr.chit.localdb.greendao.BoardingEventEntityDao;
import ru.ppr.chit.localdb.greendao.BoardingExportEventEntityDao;
import ru.ppr.chit.localdb.greendao.TripServiceEventEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class BoardingEventRepositoryImpl extends BaseCrudLocalDbRepository<BoardingEvent, BoardingEventEntity, Long> implements BoardingEventRepository {

    @Inject
    BoardingEventRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<BoardingEventEntity, Long> dao() {
        return daoSession().getBoardingEventEntityDao();
    }

    @Override
    protected LocalDbMapper<BoardingEvent, BoardingEventEntity> mapper() {
        return BoardingEventMapper.INSTANCE;
    }

    @Nullable
    @Override
    public BoardingEvent loadLast() {
        BoardingEventEntity entity = dao().queryBuilder()
                .orderDesc(BoardingEventEntityDao.Properties.Id)
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

    @NonNull
    @Override
    public List<BoardingEvent> loadAllForTripService(@NonNull String tripServiceUuid) {
        QueryBuilder<BoardingEventEntity> queryBuilder = dao().queryBuilder();
        queryBuilder
                .join(BoardingEventEntityDao.Properties.TripServiceEventId,
                        TripServiceEventEntity.class,
                        TripServiceEventEntityDao.Properties.Id)
                .where(TripServiceEventEntityDao.Properties.TripUuid.eq(tripServiceUuid));
        return mapper().entityListToModelList(queryBuilder.list());
    }

    @NonNull
    @Override
    public List<BoardingEvent> loadAllByNotInUuid(@NonNull List<String> boardingEventUuidList) {
        List<BoardingEventEntity> entityList = dao().queryBuilder()
                .where(BoardingEventEntityDao.Properties.BoardingUuid.notIn(boardingEventUuidList))
                .list();
        return mapper().entityListToModelList(entityList);
    }

    @NonNull
    @Override
    public List<BoardingEvent> loadAllExported() {
        QueryBuilder<BoardingEventEntity> qb = dao().queryBuilder();
        qb.join(BoardingExportEventEntity.class, BoardingExportEventEntityDao.Properties.BoardingEventId);
        return mapper().entityListToModelList(qb.list());
    }

    @NonNull
    @Override
    public List<BoardingEvent> loadAllNotExported() {
        List<BoardingEvent> exportedBoardingEventList = loadAllExported();
        Set<String> exportedBoardingEventUuidList = new HashSet<>();
        for (BoardingEvent boardingEvent : exportedBoardingEventList) {
            exportedBoardingEventUuidList.add(boardingEvent.getBoardingUuid());
        }
        return loadAllByNotInUuid(new ArrayList<>(exportedBoardingEventUuidList));
    }

}
