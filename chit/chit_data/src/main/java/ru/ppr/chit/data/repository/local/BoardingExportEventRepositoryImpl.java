package ru.ppr.chit.data.repository.local;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.BoardingExportEventMapper;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.BoardingExportEvent;
import ru.ppr.chit.domain.repository.local.BoardingExportEventRepository;
import ru.ppr.chit.localdb.entity.BoardingExportEventEntity;

/**
 * @author Dmitry Nevolin
 */
public class BoardingExportEventRepositoryImpl extends BaseCrudLocalDbRepository<BoardingExportEvent, BoardingExportEventEntity, Long> implements BoardingExportEventRepository {

    @Inject
    BoardingExportEventRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<BoardingExportEventEntity, Long> dao() {
        return daoSession().getBoardingExportEventEntityDao();
    }

    @Override
    protected LocalDbMapper<BoardingExportEvent, BoardingExportEventEntity> mapper() {
        return BoardingExportEventMapper.INSTANCE;
    }

}
