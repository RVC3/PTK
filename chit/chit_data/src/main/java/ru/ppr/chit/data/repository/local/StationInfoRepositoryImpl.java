package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.StationInfoMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.StationInfo;
import ru.ppr.chit.domain.repository.local.StationInfoRepository;
import ru.ppr.chit.localdb.entity.StationInfoEntity;
import ru.ppr.chit.localdb.greendao.StationInfoEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class StationInfoRepositoryImpl extends BaseCrudLocalDbRepository<StationInfo, StationInfoEntity, Long> implements StationInfoRepository {

    @Inject
    StationInfoRepositoryImpl(LocalDbManager localDbManagerr) {
        super(localDbManagerr);
    }

    @Override
    protected AbstractDao<StationInfoEntity, Long> dao() {
        return daoSession().getStationInfoEntityDao();
    }

    @Override
    protected LocalDbMapper<StationInfo, StationInfoEntity> mapper() {
        return StationInfoMapper.INSTANCE;
    }

    @NonNull
    @Override
    public List<StationInfo> loadAllByTrainInfo(@NonNull Long trainInfoId) {
        List<StationInfoEntity> entityList = dao().queryBuilder()
                .where(StationInfoEntityDao.Properties.TrainInfoId.eq(trainInfoId))
                .orderAsc(StationInfoEntityDao.Properties.Number)
                .list();
        return mapper().entityListToModelList(entityList);
    }

    @Override
    public void insertAll(@NonNull List<StationInfo> modelList, @NonNull Long trainInfoId) {
        List<StationInfoEntity> entityList = mapper().modelListToEntityList(modelList);
        for (StationInfoEntity entity : entityList) {
            entity.setTrainInfoId(trainInfoId);
        }
        dao().insertInTx(entityList);
    }

}
