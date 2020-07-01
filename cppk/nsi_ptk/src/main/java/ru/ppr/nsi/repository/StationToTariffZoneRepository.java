package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.StationToTariffZoneDao;
import ru.ppr.nsi.entity.StationToTariffZone;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * Репозиторий для {@link StationToTariffZone}.
 */
public class StationToTariffZoneRepository extends BaseRepository {
    @Inject
    StationToTariffZoneRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<StationToTariffZone, Long> selfDao() {
        return daoSession().getStationToTariffZoneDao();
    }

    @NonNull
    public List<Long> getStationCodesForTariffZone(long tariffZoneCode, int versionId) {

        QueryBuilder qb = new QueryBuilder();

        qb.select().field(StationToTariffZoneDao.Properties.StationCode);
        qb.from(StationToTariffZoneDao.TABLE_NAME);
        qb.where().field(StationToTariffZoneDao.Properties.TariffZoneCode).eq(tariffZoneCode);
        qb.and().appendRaw(NsiUtils.checkVersion(StationToTariffZoneDao.TABLE_NAME, versionId));

        List<Long> stationCodes = new ArrayList<>();
        Query query = qb.build();
        Cursor cursor = query.run(daoSession().getNsiDb());
        try {
            while (cursor.moveToNext()) {
                Long stationId = cursor.getLong(0);
                stationCodes.add(stationId);
            }
        } finally {
            cursor.close();
        }

        return stationCodes;
    }
}
