package ru.ppr.chit.data.repository.local;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.PassengerMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.Passenger;
import ru.ppr.chit.domain.repository.local.PassengerRepository;
import ru.ppr.chit.localdb.entity.PassengerEntity;

/**
 * @author Dmitry Nevolin
 */
public class PassengerRepositoryImpl extends BaseCrudLocalDbRepository<Passenger, PassengerEntity, Long> implements PassengerRepository {

    @Inject
    PassengerRepositoryImpl(LocalDbManager localDbManagerr) {
        super(localDbManagerr);
    }

    @Override
    protected AbstractDao<PassengerEntity, Long> dao() {
        return daoSession().getPassengerEntityDao();
    }

    @Override
    protected LocalDbMapper<Passenger, PassengerEntity> mapper() {
        return PassengerMapper.INSTANCE;
    }

}
