package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.CarSchemeElementMapper;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.CarSchemeElement;
import ru.ppr.chit.domain.repository.local.CarSchemeElementRepository;
import ru.ppr.chit.localdb.entity.CarSchemeElementEntity;
import ru.ppr.chit.localdb.greendao.CarSchemeElementEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class CarSchemeElementRepositoryImpl extends BaseCrudLocalDbRepository<CarSchemeElement, CarSchemeElementEntity, Long> implements CarSchemeElementRepository {

    @Inject
    CarSchemeElementRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<CarSchemeElementEntity, Long> dao() {
        return daoSession().getCarSchemeElementEntityDao();
    }

    @Override
    protected LocalDbMapper<CarSchemeElement, CarSchemeElementEntity> mapper() {
        return CarSchemeElementMapper.INSTANCE;
    }

    @NonNull
    @Override
    public List<CarSchemeElement> loadAllByCarScheme(@NonNull Long carSchemeId) {
        List<CarSchemeElementEntity> entityList = dao().queryBuilder()
                .where(CarSchemeElementEntityDao.Properties.CarSchemeId.eq(carSchemeId))
                .list();
        return mapper().entityListToModelList(entityList);
    }

    @Override
    public long insert(@NonNull CarSchemeElement carSchemeElement, @NonNull Long carSchemeId) {
        CarSchemeElementEntity entity = mapper().modelToEntity(carSchemeElement);
        entity.setCarSchemeId(carSchemeId);
        return dao().insert(entity);
    }

    @Override
    public void insertAll(@NonNull List<CarSchemeElement> modelList, @NonNull Long carSchemeId) {
        List<CarSchemeElementEntity> entityList = mapper().modelListToEntityList(modelList);
        for (CarSchemeElementEntity entity : entityList) {
            entity.setCarSchemeId(carSchemeId);
        }
        dao().insertInTx(entityList);
    }

}
