package ru.ppr.chit.data.repository.local;

import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.OAuth2TokenMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.OAuth2Token;
import ru.ppr.chit.domain.repository.local.OAuth2TokenRepository;
import ru.ppr.chit.localdb.entity.OAuth2TokenEntity;

/**
 * @author Dmitry Nevolin
 */
public class OAuth2TokenRepositoryImpl extends BaseCrudLocalDbRepository<OAuth2Token, OAuth2TokenEntity, Long> implements OAuth2TokenRepository {

    @Inject
    OAuth2TokenRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<OAuth2TokenEntity, Long> dao() {
        return daoSession().getOAuth2TokenEntityDao();
    }

    @Override
    protected LocalDbMapper<OAuth2Token, OAuth2TokenEntity> mapper() {
        return OAuth2TokenMapper.INSTANCE;
    }

    @Nullable
    @Override
    public OAuth2Token loadFirst() {
        OAuth2TokenEntity entity = dao().queryBuilder()
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

}
