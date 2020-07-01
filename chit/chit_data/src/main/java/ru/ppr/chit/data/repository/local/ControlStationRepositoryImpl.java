package ru.ppr.chit.data.repository.local;

import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.ControlStationMapper;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.domain.repository.local.ControlStationRepository;
import ru.ppr.chit.localdb.entity.ControlStationEntity;

/**
 * @author Dmitry Nevolin
 */
public class ControlStationRepositoryImpl extends BaseCrudLocalDbRepository<ControlStation, ControlStationEntity, Long> implements ControlStationRepository {

    @Inject
    ControlStationRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<ControlStationEntity, Long> dao() {
        return daoSession().getControlStationEntityDao();
    }

    @Override
    protected LocalDbMapper<ControlStation, ControlStationEntity> mapper() {
        return ControlStationMapper.INSTANCE;
    }

    @Nullable
    @Override
    public ControlStation loadFirst() {
        ControlStationEntity entity = dao().queryBuilder()
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

}
