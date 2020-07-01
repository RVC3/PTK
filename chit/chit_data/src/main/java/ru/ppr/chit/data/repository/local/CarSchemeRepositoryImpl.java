package ru.ppr.chit.data.repository.local;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.CarSchemeMapper;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.CarScheme;
import ru.ppr.chit.domain.repository.local.CarSchemeRepository;
import ru.ppr.chit.localdb.entity.CarSchemeEntity;

/**
 * @author Dmitry Nevolin
 */
public class CarSchemeRepositoryImpl extends BaseCrudLocalDbRepository<CarScheme, CarSchemeEntity, Long> implements CarSchemeRepository {

    @Inject
    CarSchemeRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<CarSchemeEntity, Long> dao() {
        return daoSession().getCarSchemeEntityDao();
    }

    @Override
    protected LocalDbMapper<CarScheme, CarSchemeEntity> mapper() {
        return CarSchemeMapper.INSTANCE;
    }

}
