package ru.ppr.nsi.query;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.StationsOnRouteDao;

/**
 * Запрос возвращает список кодов станций,
 * которые расположены на том же маршруте что и входящий список станций.
 *
 * @author Grigoriy Kashka
 */
public class StationsOnRouteQuery extends BaseNsiQuery {

    @Nullable
    private final Iterable<Long> stationCodes;
    /**
     * Версия НСИ
     */
    private final int versionId;

    public StationsOnRouteQuery(NsiDaoSession nsiDaoSession,
                                @Nullable Iterable<Long> stationCodes,
                                int versionId) {
        super(nsiDaoSession);
        this.stationCodes = stationCodes;
        this.versionId = versionId;
    }

    /**
     * Выполняет запрос.
     *
     * @return Список кодов станций на маршруте
     */
    public List<Long> query() {

        QueryBuilder qb = new QueryBuilder();

        qb.selectDistinct().field(StationsOnRouteDao.TABLE_NAME, StationsOnRouteDao.Properties.StationCode).from(StationsOnRouteDao.TABLE_NAME);
        qb.where();
        qb.appendRaw(checkVersion(StationsOnRouteDao.TABLE_NAME, versionId));
        qb.and().field(StationsOnRouteDao.Properties.RouteCode).in().appendInBrackets(() -> {
            qb.selectDistinct().field(StationsOnRouteDao.TABLE_NAME, StationsOnRouteDao.Properties.RouteCode).from(StationsOnRouteDao.TABLE_NAME);
            qb.where();
            qb.field(StationsOnRouteDao.TABLE_NAME, StationsOnRouteDao.Properties.StationCode).in(stationCodes);
            qb.and().appendRaw(checkVersion(StationsOnRouteDao.TABLE_NAME, versionId));
        });

        Query query = qb.build();
        ArrayList<Long> stationCodesOut = new ArrayList<Long>();
        Cursor cursor = null;
        try {
            cursor = query.run(db());
            while (cursor.moveToNext()) {
                stationCodesOut.add(cursor.getLong(0));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        //LOGD("StationsOnRouteQuery", "End");
        return stationCodesOut;
    }
}
