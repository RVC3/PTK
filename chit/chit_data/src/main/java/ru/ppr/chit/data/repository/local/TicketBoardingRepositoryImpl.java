package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.TicketBoardingMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.TicketBoarding;
import ru.ppr.chit.domain.repository.local.TicketBoardingRepository;
import ru.ppr.chit.localdb.entity.TicketBoardingEntity;
import ru.ppr.chit.localdb.greendao.TicketBoardingEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class TicketBoardingRepositoryImpl extends BaseCrudLocalDbRepository<TicketBoarding, TicketBoardingEntity, Long> implements TicketBoardingRepository {

    @Inject
    TicketBoardingRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<TicketBoardingEntity, Long> dao() {
        return daoSession().getTicketBoardingEntityDao();
    }

    @Override
    protected LocalDbMapper<TicketBoarding, TicketBoardingEntity> mapper() {
        return TicketBoardingMapper.INSTANCE;
    }

    @Nullable
    @Override
    public TicketBoarding loadByTicket(@NonNull Long ticketId) {
        TicketBoardingEntity entity = dao().queryBuilder()
                .where(TicketBoardingEntityDao.Properties.TicketIdId.eq(ticketId))
                .unique();
        return mapper().entityToModel(entity);
    }

}
