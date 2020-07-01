package ru.ppr.chit.data.repository.local;

import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.TrainInfoMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.repository.local.TrainInfoRepository;
import ru.ppr.chit.localdb.entity.TrainInfoEntity;
import ru.ppr.chit.localdb.greendao.TrainInfoEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class TrainInfoRepositoryImpl extends BaseCrudLocalDbRepository<TrainInfo, TrainInfoEntity, Long> implements TrainInfoRepository {

    @Inject
    TrainInfoRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<TrainInfoEntity, Long> dao() {
        return daoSession().getTrainInfoEntityDao();
    }

    @Override
    protected LocalDbMapper<TrainInfo, TrainInfoEntity> mapper() {
        return TrainInfoMapper.INSTANCE;
    }

    @Override
    public TrainInfo loadLastNotLegacy() {
        TrainInfoEntity entity = dao().queryBuilder()
                .where(TrainInfoEntityDao.Properties.Legacy.eq(false))
                .orderDesc(TrainInfoEntityDao.Properties.Id)
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

}
