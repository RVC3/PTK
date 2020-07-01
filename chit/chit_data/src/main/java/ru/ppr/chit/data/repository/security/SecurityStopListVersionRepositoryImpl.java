package ru.ppr.chit.data.repository.security;

import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.SecurityDbManager;
import ru.ppr.chit.data.mapper.security.SecurityDbMapper;
import ru.ppr.chit.data.mapper.security.SecurityStopListVersionMapper;
import ru.ppr.chit.data.repository.security.base.BaseSecurityDbRepository;
import ru.ppr.chit.domain.model.security.SecurityStopListVersion;
import ru.ppr.chit.domain.repository.security.SecurityStopListVersionRepository;
import ru.ppr.chit.securitydb.entity.SecurityStopListVersionEntity;

/**
 * Created by m.sidorov.
 */
public class SecurityStopListVersionRepositoryImpl extends BaseSecurityDbRepository<SecurityStopListVersion, SecurityStopListVersionEntity, Void> implements SecurityStopListVersionRepository {

    @Inject
    SecurityStopListVersionRepositoryImpl(SecurityDbManager securityDbManager) {
        super(securityDbManager);
    }

    @Override
    protected AbstractDao<SecurityStopListVersionEntity, Void> dao() {
        return daoSession().getSecurityStopListVersionEntityDao();
    }

    @Override
    protected SecurityDbMapper<SecurityStopListVersion, SecurityStopListVersionEntity> mapper() {
        return SecurityStopListVersionMapper.INSTANCE;
    }

    @Nullable
    @Override
    public SecurityStopListVersion loadLast() {
        SecurityStopListVersionEntity entity = dao().queryBuilder()
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

}
