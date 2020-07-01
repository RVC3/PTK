package ru.ppr.nsi.query.stationswithtariffs;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.StationDao;
import ru.ppr.nsi.dao.TariffDao;
import ru.ppr.nsi.model.TariffDescription;

/**
 * Оптимизированная версия запроса {@link TariffsQuery} для случая,
 * когда требуются только лишь коды станций отправления или назначения.
 *
 * @author Aleksandr Brazhkin
 */
public class StationsWithTariffsQuery extends BaseTariffsQuery {
    /**
     * {@code true} - искать станции отправления
     * {@code false} - искать станции назначения
     */
    private final boolean searchDeparture;

    public StationsWithTariffsQuery(NsiDaoSession nsiDaoSession,
                                    @NonNull Params params,
                                    boolean searchDeparture) {
        super(nsiDaoSession, params);
        this.searchDeparture = searchDeparture;
    }

    @Override
    @NonNull
    List<TariffDescription> queryImpl(@Nullable Collection<Long> allowedDepStationCodes,
                                      @Nullable Collection<Long> allowedDestStationCodes) {
        String mainStationColumnName;
        String secondaryStationColumnName;
        String mainStationsTableName;
        String secondaryStationsTableName;
        Collection<Long> mainAllowedStationCodes;
        Collection<Long> secondaryAllowedStationCodes;

        if (searchDeparture) {
            mainStationColumnName = TariffDao.Properties.StationDepartureCode;
            secondaryStationColumnName = TariffDao.Properties.StationDestinationCode;
            mainStationsTableName = ALLOWED_DEP_STATION_CODES;
            secondaryStationsTableName = ALLOWED_DEST_STATION_CODES;
            mainAllowedStationCodes = allowedDepStationCodes;
            secondaryAllowedStationCodes = allowedDestStationCodes;
        } else {
            mainStationColumnName = TariffDao.Properties.StationDestinationCode;
            secondaryStationColumnName = TariffDao.Properties.StationDepartureCode;
            mainStationsTableName = ALLOWED_DEST_STATION_CODES;
            secondaryStationsTableName = ALLOWED_DEP_STATION_CODES;
            mainAllowedStationCodes = allowedDestStationCodes;
            secondaryAllowedStationCodes = allowedDepStationCodes;
        }

        QueryBuilder qb = new QueryBuilder();
        // SELECT Code FROM Stations WHERE
        qb.selectDistinct().field(StationDao.Properties.Code).from(StationDao.TABLE_NAME).where();
        // Stations.VersionId <= @versionId AND (Stations.DeleteInVersionId > @versionId OR Stations.DeleteInVersionId IS NULL)
        qb.appendRaw(checkVersion(StationDao.TABLE_NAME, params.getVersionId()));
        if (mainAllowedStationCodes != null) {
            // AND Stations.Code IN (SELECT Code FROM ALLOWED_DEP_STATION_CODES)
            qb.and().field(StationDao.Properties.Code).in().appendInBrackets(() -> {
                qb.select().field(CODE).from(mainStationsTableName);
            });
        }
        qb.and().exists(() -> {
            // SELECT 1 FROM Tariffs
            qb.select().appendRaw(1).from(TariffDao.TABLE_NAME);
            if (secondaryAllowedStationCodes != null) {
                // INNER JOIN ALLOWED_DEST_STATION_CODES
                qb.innerJoin().table(secondaryStationsTableName)
                        // ON DestinationStationCode = ALLOWED_DEST_STATION_CODES.Code
                        .on().field(secondaryStationColumnName).eq().field(secondaryStationsTableName, CODE);
            }
            //  WHERE
            qb.where();
            // Tariffs.VersionId <= @versionId AND (Tariffs.DeleteInVersionId > @versionId OR Tariffs.DeleteInVersionId IS NULL)
            qb.appendRaw(checkVersion(TariffDao.TABLE_NAME, params.getVersionId()));
            // AND Stations.Code = DepartureStationCode
            qb.and().field(StationDao.TABLE_NAME, StationDao.Properties.Code).eq().field(mainStationColumnName);
            if (params.getAllowedTicketTypeCodes() != null) {
                // AND TicketTypeCode IN (@allowedTicketTypeCodes)
                qb.and().field(TariffDao.Properties.TicketTypeCode).in(params.getAllowedTicketTypeCodes());
            }
            if (params.getAllowedTariffPlanCodes() != null) {
                // AND TariffPlanCode IN (@allowedTariffPlanCodes)
                qb.and().field(TariffDao.Properties.TariffPlanCode).in(params.getAllowedTariffPlanCodes());
            }
        });

        List<TariffDescription> tariffs = new ArrayList<>();

        Query query = qb.build();
        query.logQuery();

        Cursor cursor = query.run(db());
        try {
            while (cursor.moveToNext()) {
                TariffDescription tariff = new TariffDescription();
                Long stationCode = cursor.getLong(0);
                if (searchDeparture) {
                    tariff.setDepStationCode(stationCode);
                } else {
                    tariff.setDestStationCode(stationCode);
                }
                tariffs.add(tariff);
            }
        } finally {
            cursor.close();
        }
        return tariffs;
    }
}
