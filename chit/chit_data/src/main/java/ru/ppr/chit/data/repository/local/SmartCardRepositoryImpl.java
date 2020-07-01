package ru.ppr.chit.data.repository.local;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.SmartCardMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.SmartCard;
import ru.ppr.chit.domain.repository.local.SmartCardRepository;
import ru.ppr.chit.localdb.entity.SmartCardEntity;

/**
 * @author Dmitry Nevolin
 */
public class SmartCardRepositoryImpl extends BaseCrudLocalDbRepository<SmartCard, SmartCardEntity, Long> implements SmartCardRepository {

    @Inject
    SmartCardRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<SmartCardEntity, Long> dao() {
        return daoSession().getSmartCardEntityDao();
    }

    @Override
    protected LocalDbMapper<SmartCard, SmartCardEntity> mapper() {
        return SmartCardMapper.INSTANCE;
    }

}
