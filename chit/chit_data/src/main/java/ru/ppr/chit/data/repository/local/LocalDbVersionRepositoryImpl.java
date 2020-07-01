package ru.ppr.chit.data.repository.local;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.LocalDbVersionMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.LocalDbVersion;
import ru.ppr.chit.domain.repository.local.base.LocalDbRepository;
import ru.ppr.chit.localdb.entity.LocalDbVersionEntity;

/**
 * @author Aleksandr Brazhkin
 */
public class LocalDbVersionRepositoryImpl extends BaseCrudLocalDbRepository<LocalDbVersion, LocalDbVersionEntity, Long> implements LocalDbRepository {

    @Inject
    LocalDbVersionRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<LocalDbVersionEntity, Long> dao() {
        return daoSession().getLocalDbVersionEntityDao();
    }

    @Override
    protected LocalDbMapper<LocalDbVersion, LocalDbVersionEntity> mapper() {
        return LocalDbVersionMapper.INSTANCE;
    }
}
