package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.StationTransferRouteDao;
import ru.ppr.nsi.entity.StationTransferRoute;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class StationTransferRouteRepository extends BaseRepository<StationTransferRoute, Long> {

    @Inject
    StationTransferRouteRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<StationTransferRoute, Long> selfDao() {
        return daoSession().getStationTransferRouteDao();
    }

    /**
     * Загружает список кодов автобусных маршрутов для списка станций
     *
     * @param stationCodes список кодов станций
     * @param versionId    версия НСИ
     * @return список кодов автобусных маршрутов для списка станций
     */
    public List<Long> loadStationsTransferRouteCodesForStationCodes(@NonNull List<Long> stationCodes, int versionId) {
        List<Long> stationTransferRoutes = new ArrayList<>();
        QueryBuilder qb = new QueryBuilder();

        qb.selectAll().from(StationTransferRouteDao.TABLE_NAME);
        qb.where().field(StationTransferRouteDao.Properties.StationCode).in(stationCodes);
        qb.and().appendRaw(NsiUtils.checkVersion(StationTransferRouteDao.TABLE_NAME, versionId));

        Cursor cursor = qb.build().run(daoSession().getNsiDb());

        try {
            while (cursor.moveToNext()) {
                stationTransferRoutes.add(selfDao().readEntity(cursor, 0).getRouteCode());
            }
        } finally {
            cursor.close();
        }

        return stationTransferRoutes;
    }

}
