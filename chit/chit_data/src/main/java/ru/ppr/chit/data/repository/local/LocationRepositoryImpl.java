package ru.ppr.chit.data.repository.local;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.LocationMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.Location;
import ru.ppr.chit.domain.repository.local.LocationRepository;
import ru.ppr.chit.localdb.entity.LocationEntity;

/**
 * @author Dmitry Nevolin
 */
public class LocationRepositoryImpl extends BaseCrudLocalDbRepository<Location, LocationEntity, Long> implements LocationRepository {

    @Inject
    LocationRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<LocationEntity, Long> dao() {
        return daoSession().getLocationEntityDao();
    }

    @Override
    protected LocalDbMapper<Location, LocationEntity> mapper() {
        return LocationMapper.INSTANCE;
    }

}
