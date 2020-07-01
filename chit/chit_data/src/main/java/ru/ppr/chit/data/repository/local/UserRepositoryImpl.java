package ru.ppr.chit.data.repository.local;

import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.UserMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.User;
import ru.ppr.chit.domain.repository.local.UserRepository;
import ru.ppr.chit.localdb.entity.UserEntity;
import ru.ppr.chit.localdb.greendao.UserEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class UserRepositoryImpl extends BaseCrudLocalDbRepository<User, UserEntity, Long> implements UserRepository {

    @Inject
    UserRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<UserEntity, Long> dao() {
        return daoSession().getUserEntityDao();
    }

    @Override
    protected LocalDbMapper<User, UserEntity> mapper() {
        return UserMapper.INSTANCE;
    }

    @Nullable
    @Override
    public User loadLast() {
        UserEntity entity = dao().queryBuilder()
                .orderDesc(UserEntityDao.Properties.Id)
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

}
