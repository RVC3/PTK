package ru.ppr.chit.data.repository.nsi;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.mapper.nsi.NsiDbMapper;
import ru.ppr.chit.data.mapper.nsi.StationMapper;
import ru.ppr.chit.data.repository.nsi.base.BaseCvdNsiDbRepository;
import ru.ppr.chit.domain.model.nsi.Station;
import ru.ppr.chit.domain.repository.nsi.StationRepository;
import ru.ppr.chit.nsidb.entity.StationEntity;

/**
 * @author Aleksandr Brazhkin
 */
public class StationRepositoryImpl extends BaseCvdNsiDbRepository<Station, StationEntity, Long> implements StationRepository {

    @Inject
    StationRepositoryImpl(NsiDbManager nsiDbManager) {
        super(nsiDbManager);
    }

    @Override
    protected AbstractDao<StationEntity, Void> dao() {
        return daoSession().getStationEntityDao();
    }

    @Override
    protected NsiDbMapper<Station, StationEntity> mapper() {
        return StationMapper.INSTANCE;
    }

}
