package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.TicketIdMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.repository.local.TicketIdRepository;
import ru.ppr.chit.localdb.entity.TicketIdEntity;
import ru.ppr.chit.localdb.greendao.TicketIdEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class TicketIdRepositoryImpl extends BaseCrudLocalDbRepository<TicketId, TicketIdEntity, Long> implements TicketIdRepository {

    @Inject
    TicketIdRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<TicketIdEntity, Long> dao() {
        return daoSession().getTicketIdEntityDao();
    }

    @Override
    protected LocalDbMapper<TicketId, TicketIdEntity> mapper() {
        return TicketIdMapper.INSTANCE;
    }

    @Nullable
    @Override
    public TicketId loadByIdentity(long ticketNumber, @NonNull Date saleDate, @NonNull String deviceId) {
        TicketIdEntity entity = dao().queryBuilder()
                .where(TicketIdEntityDao.Properties.TicketNumber.eq(ticketNumber),
                        TicketIdEntityDao.Properties.SaleDate.eq(saleDate),
                        TicketIdEntityDao.Properties.DeviceId.eq(deviceId))
                .unique();
        return mapper().entityToModel(entity);
    }

}
