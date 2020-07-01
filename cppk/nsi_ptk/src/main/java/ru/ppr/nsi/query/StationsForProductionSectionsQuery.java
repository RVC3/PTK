package ru.ppr.nsi.query;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.StationDao;
import ru.ppr.nsi.dao.StationForProductionSectionDao;
import ru.ppr.nsi.entity.Station;

/**
 * Запрос на список станций, принадлежщих участкам.
 *
 * @author Aleksandr Brazhkin
 */
public class StationsForProductionSectionsQuery extends BaseNsiQuery {

    private static String TAG = Logger.makeLogTag(StationsForProductionSectionsQuery.class);

    /**
     * Ограничивающий список кодов производственных участков для фильтра.
     */
    @NonNull
    private final List<Long> allowedProductionSectionCodes;
    /**
     * Версия НСИ
     */
    private final int versionId;

    public StationsForProductionSectionsQuery(NsiDaoSession nsiDaoSession,
                                              @NonNull List<Long> allowedProductionSectionCodes,
                                              int versionId) {
        super(nsiDaoSession);
        this.allowedProductionSectionCodes = allowedProductionSectionCodes;
        this.versionId = versionId;
    }

    @NonNull
    public List<Station> query() {
        QueryBuilder qb = new QueryBuilder();
        qb.select()
                .field(StationDao.TABLE_NAME, StationDao.Properties.EsrCode).comma()
                .field(StationDao.TABLE_NAME, StationDao.Properties.Name).comma()
                .field(StationDao.TABLE_NAME, StationDao.Properties.ShortName).comma()
                .field(StationDao.TABLE_NAME, StationDao.Properties.RegionCode).comma()
                .field(StationDao.TABLE_NAME, StationDao.Properties.IsTransitStation).comma()
                .field(StationDao.TABLE_NAME, StationDao.Properties.CanSaleTickets).comma()
                .field(StationDao.TABLE_NAME, StationDao.Properties.Code).comma()
                .field(StationDao.TABLE_NAME, StationDao.Properties.IsForTransfer)
                .from(StationDao.TABLE_NAME);
        qb.innerJoin(StationForProductionSectionDao.TABLE_NAME);
        qb.on().field(StationDao.TABLE_NAME, BaseEntityDao.Properties.Code).
                eq().field(StationForProductionSectionDao.Properties.StationCode);
        qb.where();
        qb.field(StationForProductionSectionDao.Properties.ProductionSectionCode).in(allowedProductionSectionCodes);

        List<Station> stations = new ArrayList<>();

        Query query = qb.build();
        Logger.trace(TAG, "StationsForProductionSectionsQuery - Start");
        Cursor cursor = null;
        try {
            cursor = query.run(db());
            while (cursor.moveToNext()) {
                Station station = cursorToStation(cursor);
                stations.add(station);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Logger.trace(TAG, "StationsForProductionSectionsQuery - End");
        return stations;
    }

    /**
     * StationsDao.fromCursor съедает слишком много времени из-за cursor.getColumnIndex,
     * как временное решение достаём станцию с захардкоженными индексами, VersionId и DeleteInVersionId
     * отсутствуют, по аналогии с StationsDao.fromCursor
     */
    private Station cursorToStation(@NonNull Cursor cursor) {
        Station station = new Station();

        station.setErcCode(cursor.getInt(0));
        station.setName(cursor.getString(1).trim());
        station.setShortName(cursor.getString(2).trim());
        station.setRegionCode(cursor.getInt(3));
        station.setTransitStation(cursor.getInt(4) > 0);
        station.setCanSaleTickets(cursor.getInt(5) > 0);
        station.setCode(cursor.getInt(6));
        station.setForTransfer(cursor.getInt(7) > 0);
        station.setVersionId(versionId);

        return station;
    }

}
