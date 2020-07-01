package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.CarInfoMapper;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.CarInfo;
import ru.ppr.chit.domain.repository.local.CarInfoRepository;
import ru.ppr.chit.localdb.entity.CarInfoEntity;
import ru.ppr.chit.localdb.greendao.CarInfoEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class CarInfoRepositoryImpl extends BaseCrudLocalDbRepository<CarInfo, CarInfoEntity, Long> implements CarInfoRepository {

    @Inject
    CarInfoRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<CarInfoEntity, Long> dao() {
        return daoSession().getCarInfoEntityDao();
    }

    @Override
    protected LocalDbMapper<CarInfo, CarInfoEntity> mapper() {
        return CarInfoMapper.INSTANCE;
    }

    @NonNull
    @Override
    public List<CarInfo> loadAllByTrainInfo(@NonNull Long trainInfoId) {
        List<CarInfoEntity> entityList = dao().queryBuilder()
                .where(CarInfoEntityDao.Properties.TrainInfoId.eq(trainInfoId))
                .list();
        return mapper().entityListToModelList(entityList);
    }

    @Override
    public long insert(@NonNull CarInfo carInfo, @NonNull Long trainInfoId) {
        CarInfoEntity entity = mapper().modelToEntity(carInfo);
        entity.setTrainInfoId(trainInfoId);
        return dao().insert(entity);
    }

    @Override
    public void insertAll(@NonNull List<CarInfo> modelList, @NonNull Long trainInfoId) {
        List<CarInfoEntity> entityList = mapper().modelListToEntityList(modelList);
        for (CarInfoEntity entity : entityList) {
            entity.setTrainInfoId(trainInfoId);
        }
        dao().insertInTx(entityList);
    }

}
