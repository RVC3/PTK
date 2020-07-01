package ru.ppr.chit.data.repository.local;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.PlaceLocationMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.PlaceLocation;
import ru.ppr.chit.domain.repository.local.PlaceLocationRepository;
import ru.ppr.chit.localdb.entity.PlaceLocationEntity;

/**
 * @author Dmitry Nevolin
 */
public class PlaceLocationRepositoryImpl extends BaseCrudLocalDbRepository<PlaceLocation, PlaceLocationEntity, Long> implements PlaceLocationRepository {

    @Inject
    PlaceLocationRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<PlaceLocationEntity, Long> dao() {
        return daoSession().getPlaceLocationEntityDao();
    }

    @Override
    protected LocalDbMapper<PlaceLocation, PlaceLocationEntity> mapper() {
        return PlaceLocationMapper.INSTANCE;
    }

}
