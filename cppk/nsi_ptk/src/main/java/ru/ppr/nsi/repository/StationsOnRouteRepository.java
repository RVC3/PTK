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
import ru.ppr.nsi.dao.StationsOnRouteDao;
import ru.ppr.nsi.entity.StationOnRoutes;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class StationsOnRouteRepository extends BaseRepository<StationOnRoutes, Integer> {

    @Inject
    StationsOnRouteRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<StationOnRoutes, Integer> selfDao() {
        return daoSession().getStationsOnRouteDao();
    }

    public List<StationOnRoutes> loadStationsOnRoutesByRouteCode(@NonNull String routeCode, int versionNsi) {
        QueryBuilder qb = new QueryBuilder();

        qb.selectAll().from(StationsOnRouteDao.TABLE_NAME).where().field(StationsOnRouteDao.Properties.RouteCode).eq(routeCode);
        qb.and().appendRaw(NsiUtils.checkVersion(StationsOnRouteDao.TABLE_NAME, versionNsi));

        List<StationOnRoutes> stationsOnRoutes = new ArrayList<>();
        Cursor cursor = qb.build().run(daoSession().getNsiDb());

        try {
            while (cursor.moveToNext()) {
                stationsOnRoutes.add(daoSession().getStationsOnRouteDao().readEntity(cursor, 0));
            }
        } finally {
            cursor.close();
        }

        return stationsOnRoutes;
    }

}
