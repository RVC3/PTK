package ru.ppr.chit.data.repository.local;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.TicketDataMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.TicketData;
import ru.ppr.chit.domain.repository.local.TicketDataRepository;
import ru.ppr.chit.localdb.entity.TicketDataEntity;

/**
 * @author Dmitry Nevolin
 */
public class TicketDataRepositoryImpl extends BaseCrudLocalDbRepository<TicketData, TicketDataEntity, Long> implements TicketDataRepository {

    @Inject
    TicketDataRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<TicketDataEntity, Long> dao() {
        return daoSession().getTicketDataEntityDao();
    }

    @Override
    protected LocalDbMapper<TicketData, TicketDataEntity> mapper() {
        return TicketDataMapper.INSTANCE;
    }

}
