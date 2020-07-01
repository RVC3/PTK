package ru.ppr.chit.data.repository.security;

import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.SecurityDbManager;
import ru.ppr.chit.data.mapper.security.PtkDataContractsVersionMapper;
import ru.ppr.chit.data.mapper.security.SecurityDbMapper;
import ru.ppr.chit.data.repository.security.base.BaseSecurityDbRepository;
import ru.ppr.chit.domain.model.security.PtkDataContractsVersion;
import ru.ppr.chit.domain.repository.security.PtkDataContractsVersionRepository;
import ru.ppr.chit.securitydb.entity.PtkDataContractsVersionEntity;
import ru.ppr.chit.securitydb.greendao.PtkDataContractsVersionEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class PtkDataContractsVersionRepositoryImpl extends BaseSecurityDbRepository<PtkDataContractsVersion, PtkDataContractsVersionEntity, Integer> implements PtkDataContractsVersionRepository {

    @Inject
    PtkDataContractsVersionRepositoryImpl(SecurityDbManager securityDbManager) {
        super(securityDbManager);
    }

    @Override
    protected AbstractDao<PtkDataContractsVersionEntity, Integer> dao() {
        return daoSession().getPtkDataContractsVersionEntityDao();
    }

    @Override
    protected SecurityDbMapper<PtkDataContractsVersion, PtkDataContractsVersionEntity> mapper() {
        return PtkDataContractsVersionMapper.INSTANCE;
    }

    @Nullable
    @Override
    public PtkDataContractsVersion loadLast() {
        PtkDataContractsVersionEntity entity = dao().queryBuilder()
                .orderDesc(PtkDataContractsVersionEntityDao.Properties.Version)
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

}
