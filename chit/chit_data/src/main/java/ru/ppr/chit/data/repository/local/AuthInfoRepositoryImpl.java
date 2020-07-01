package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.AuthInfoMapper;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.AuthInfo;
import ru.ppr.chit.domain.repository.local.AuthInfoRepository;
import ru.ppr.chit.localdb.entity.AuthInfoEntity;
import ru.ppr.chit.localdb.greendao.AuthInfoEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class AuthInfoRepositoryImpl extends BaseCrudLocalDbRepository<AuthInfo, AuthInfoEntity, Long> implements AuthInfoRepository {

    @Inject
    AuthInfoRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<AuthInfoEntity, Long> dao() {
        return daoSession().getAuthInfoEntityDao();
    }

    @Override
    protected LocalDbMapper<AuthInfo, AuthInfoEntity> mapper() {
        return AuthInfoMapper.INSTANCE;
    }

    @Override
    public long insert(@NonNull AuthInfo model) {
        model.setAuthorizationDate(new Date());
        return super.insert(model);
    }

    @Nullable
    @Override
    public AuthInfo loadLast() {
        AuthInfoEntity entity = dao().queryBuilder()
                .orderDesc(AuthInfoEntityDao.Properties.Id)
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

}
