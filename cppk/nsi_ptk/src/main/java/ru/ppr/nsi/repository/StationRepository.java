package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.database.QueryBuilder;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.ProductionSectionForUkkDao;
import ru.ppr.nsi.dao.RouteToTariffPlanDao;
import ru.ppr.nsi.dao.StationDao;
import ru.ppr.nsi.dao.StationForProductionSectionDao;
import ru.ppr.nsi.dao.StationTransferRouteDao;
import ru.ppr.nsi.dao.StationsOnRouteDao;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.model.TransferStationRoute;
import ru.ppr.nsi.query.StationsForProductionSectionsQuery;
import ru.ppr.nsi.query.StationsOnRouteQuery;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class StationRepository extends BaseRepository<Station, Long> {

    @Inject
    StationRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<Station, Long> selfDao() {
        return daoSession().getStationDao();
    }

    @Override
    public Station load(Long code, int versionId) {
        Station loaded = super.load(code, versionId);

        if (loaded != null) {
            loaded.setVersionId(versionId);
        }

        return loaded;
    }

    @Override
    public List<Station> loadAll(Iterable<Long> codes, int versionId) {
        List<Station> loaded = super.loadAll(codes, versionId);

        for (Station station : loaded) {
            station.setVersionId(versionId);
        }

        return loaded;
    }

    /**
     * Возвращает список станций для текущего маршрута
     *
     * @param productionSectionCode
     * @return
     */
    public List<Station> getStationsForProductionSection(int productionSectionCode, int nsiVersion) {
        StringBuilder stringBuilder = new StringBuilder();

        List<String> selectionArgsList = new ArrayList<>();

        stringBuilder.append("SELECT DISTINCT ");
        stringBuilder.append(StationDao.TABLE_NAME).append(".").append("*");
        stringBuilder.append(" FROM ");
        stringBuilder.append(StationForProductionSectionDao.TABLE_NAME);
        stringBuilder.append(" JOIN ");
        stringBuilder.append(StationDao.TABLE_NAME);
        stringBuilder.append(" ON ");
        stringBuilder.append(StationForProductionSectionDao.TABLE_NAME).append(".").append(StationForProductionSectionDao.Properties.StationCode);
        stringBuilder.append(" = ");
        stringBuilder.append(StationDao.TABLE_NAME).append(".").append(StationDao.Properties.Code);
        stringBuilder.append(" WHERE ");
        {
            stringBuilder.append(" ( ");
            stringBuilder.append(StationForProductionSectionDao.Properties.ProductionSectionCode).append(" = ").append("?");
            selectionArgsList.add(String.valueOf(productionSectionCode));
            stringBuilder.append(" OR ");
            stringBuilder.append(StationForProductionSectionDao.Properties.ProductionSectionCode).append(" IN ");
            {
                stringBuilder.append(" ( ");
                stringBuilder.append("SELECT ");
                stringBuilder.append(ProductionSectionForUkkDao.Properties.ProductSectionCode);
                stringBuilder.append(" FROM ");
                stringBuilder.append(ProductionSectionForUkkDao.TABLE_NAME);
                stringBuilder.append(" WHERE ");
                stringBuilder.append(ProductionSectionForUkkDao.Properties.UkkCode).append(" = ").append("?");
                selectionArgsList.add(String.valueOf(productionSectionCode));
                stringBuilder.append(" AND ");
                stringBuilder.append(NsiUtils.checkVersion(ProductionSectionForUkkDao.TABLE_NAME, nsiVersion));
                stringBuilder.append(" ) ");
            }
            stringBuilder.append(" ) ");
        }
        stringBuilder.append(" AND ");
        stringBuilder.append(NsiUtils.checkVersion(StationDao.TABLE_NAME, nsiVersion));
        stringBuilder.append(" AND ");
        stringBuilder.append(NsiUtils.checkVersion(StationForProductionSectionDao.TABLE_NAME, nsiVersion));

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        List<Station> stations = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                Station station = daoSession().getStationDao().readEntity(cursor, 0);
                station.setVersionId(nsiVersion);
                stations.add(station);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return stations;
    }

    /**
     * Выполняет поиск станций между указанными для конкретных тарифных планов.
     *
     * @param fromStationCodes Коды возможных станций отправления
     * @param toStationCodes   Коды возможных станций назначения
     * @param tariffPlanCodes  Коды возможных тарифных планов
     * @param versionId        Версия НСИ
     * @return Список кодов станций, находящихся в требуемых границах
     */
    @NonNull
    public List<Long> getStationCodesBetweenStations(
            @Nullable List<Long> fromStationCodes,
            @Nullable List<Long> toStationCodes,
            @Nullable List<Long> tariffPlanCodes,
            int versionId) {

        List<String> selectionArgsList = new ArrayList<>();

        String fromStationsTable = "FromStations";
        String toStationsTable = "ToStations";
        String pairsTable = "Pairs";

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT DISTINCT ");
        sb.append(StationsOnRouteDao.Properties.StationCode);
        sb.append(" FROM ");
        sb.append(StationsOnRouteDao.TABLE_NAME);
        sb.append(" JOIN ");
        sb.append(" ( ");
        {
            sb.append("SELECT ");
            sb.append(fromStationsTable).append(".").append(StationsOnRouteDao.Properties.StationCode).append(" AS ").append("FromCode").append(", ");
            sb.append(fromStationsTable).append(".").append(StationsOnRouteDao.Properties.StationNumber).append(" AS ").append("FromNumber").append(", ");
            sb.append(toStationsTable).append(".").append(StationsOnRouteDao.Properties.StationCode).append(" AS ").append("ToCode").append(", ");
            sb.append(toStationsTable).append(".").append(StationsOnRouteDao.Properties.StationNumber).append(" AS ").append("ToNumber").append(", ");
            sb.append(RouteToTariffPlanDao.TABLE_NAME).append(".").append(RouteToTariffPlanDao.Properties.RouteCode);
            sb.append(" FROM ");
            sb.append(" ( ");
            {
                sb.append("SELECT ");
                sb.append(RouteToTariffPlanDao.Properties.RouteCode);
                sb.append(" FROM ");
                sb.append(RouteToTariffPlanDao.TABLE_NAME);
                sb.append(" WHERE ");
                sb.append(NsiUtils.checkVersion(RouteToTariffPlanDao.TABLE_NAME, versionId));
                if (tariffPlanCodes != null) {
                    sb.append(" AND ");
                    sb.append(RouteToTariffPlanDao.Properties.TariffPlanCode);
                    sb.append(" IN ");
                    sb.append(" ( ");
                    sb.append(SqLiteUtils.makePlaceholders(tariffPlanCodes.size()));
                    for (Long tariffPlanCode : tariffPlanCodes) {
                        selectionArgsList.add(String.valueOf(tariffPlanCode));
                    }
                    sb.append(" ) ");
                }
            }
            sb.append(" ) ").append(" AS ").append(RouteToTariffPlanDao.TABLE_NAME);
            ;
            sb.append(" JOIN ");
            sb.append(" ( ");
            {
                sb.append("SELECT ");
                sb.append(StationsOnRouteDao.Properties.StationCode).append(", ");
                sb.append(StationsOnRouteDao.Properties.StationNumber).append(", ");
                sb.append(StationsOnRouteDao.Properties.RouteCode);
                sb.append(" FROM ");
                sb.append(StationsOnRouteDao.TABLE_NAME);
                sb.append(" WHERE ");
                sb.append(NsiUtils.checkVersion(StationsOnRouteDao.TABLE_NAME, versionId));
                if (fromStationCodes != null) {
                    sb.append(" AND ");
                    sb.append(StationsOnRouteDao.Properties.StationCode);
                    sb.append(" IN ");
                    sb.append(" ( ");
                    sb.append(SqLiteUtils.makePlaceholders(fromStationCodes.size()));
                    for (Long stationCode : fromStationCodes) {
                        selectionArgsList.add(String.valueOf(stationCode));
                    }
                    sb.append(" ) ");
                }
            }
            sb.append(" ) ").append(" AS ").append(fromStationsTable);
            sb.append(" ON ");
            sb.append(RouteToTariffPlanDao.TABLE_NAME).append(".").append(RouteToTariffPlanDao.Properties.RouteCode);
            sb.append(" = ");
            sb.append(fromStationsTable).append(".").append(StationsOnRouteDao.Properties.RouteCode);
            sb.append(" JOIN ");
            sb.append(" ( ");
            {
                sb.append("SELECT ");
                sb.append(StationsOnRouteDao.Properties.StationCode).append(", ");
                sb.append(StationsOnRouteDao.Properties.StationNumber).append(", ");
                sb.append(StationsOnRouteDao.Properties.RouteCode);
                sb.append(" FROM ");
                sb.append(StationsOnRouteDao.TABLE_NAME);
                sb.append(" WHERE ");
                sb.append(NsiUtils.checkVersion(StationsOnRouteDao.TABLE_NAME, versionId));
                if (toStationCodes != null) {
                    sb.append(" AND ");
                    sb.append(StationsOnRouteDao.Properties.StationCode);
                    sb.append(" IN ");
                    sb.append(" ( ");
                    sb.append(SqLiteUtils.makePlaceholders(toStationCodes.size()));
                    for (Long stationCode : toStationCodes) {
                        selectionArgsList.add(String.valueOf(stationCode));
                    }
                    sb.append(" ) ");
                }
            }
            sb.append(" ) ").append(" AS ").append(toStationsTable);
            sb.append(" ON ");
            sb.append(fromStationsTable).append(".").append(StationsOnRouteDao.Properties.RouteCode);
            sb.append(" = ");
            sb.append(toStationsTable).append(".").append(StationsOnRouteDao.Properties.RouteCode);
            sb.append(" AND ");
            sb.append(fromStationsTable).append(".").append(StationsOnRouteDao.Properties.StationCode);
            sb.append(" <> ");
            sb.append(toStationsTable).append(".").append(StationsOnRouteDao.Properties.StationCode);
        }
        sb.append(" ) ").append(" AS ").append(pairsTable);
        sb.append(" ON ");
        sb.append(StationsOnRouteDao.TABLE_NAME).append(".").append(StationsOnRouteDao.Properties.RouteCode);
        sb.append(" = ");
        sb.append(pairsTable).append(".").append(RouteToTariffPlanDao.Properties.RouteCode);
        sb.append(" AND ");
        sb.append(" ( ");
        {
            sb.append(StationsOnRouteDao.TABLE_NAME).append(".").append(StationsOnRouteDao.Properties.StationNumber);
            sb.append(" >= ");
            sb.append(pairsTable).append(".").append("FromNumber");
            sb.append(" AND ");
            sb.append(StationsOnRouteDao.TABLE_NAME).append(".").append(StationsOnRouteDao.Properties.StationNumber);
            sb.append(" <= ");
            sb.append(pairsTable).append(".").append("ToNumber");
            sb.append(" OR ");
            sb.append(StationsOnRouteDao.TABLE_NAME).append(".").append(StationsOnRouteDao.Properties.StationNumber);
            sb.append(" >= ");
            sb.append(pairsTable).append(".").append("ToNumber");
            sb.append(" AND ");
            sb.append(StationsOnRouteDao.TABLE_NAME).append(".").append(StationsOnRouteDao.Properties.StationNumber);
            sb.append(" <= ");
            sb.append(pairsTable).append(".").append("FromNumber");
        }
        sb.append(" ) ");
        sb.append(" WHERE ");
        sb.append(NsiUtils.checkVersion(StationsOnRouteDao.TABLE_NAME, versionId));

        List<Long> stationCodes = new ArrayList<>();

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(sb.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                long stationCode = cursor.getLong(0);
                stationCodes.add(stationCode);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return stationCodes;
    }

    /**
     * Выполянет фильтрацию кодов станций по имени станции.
     *
     * @param codes     Список кодов станций
     * @param likeQuery Текст для поиска
     * @param versionId Версия НСИ
     * @return Список кодов станций
     */
    public Set<Long> filterStationCodesByName(@Nullable Iterable<Long> codes, @Nullable String likeQuery, int versionId) {
        QueryBuilder qb = new QueryBuilder();
        qb.select().field(StationDao.Properties.Code).from(StationDao.TABLE_NAME);
        qb.where().appendRaw(NsiUtils.checkVersion(StationDao.TABLE_NAME, versionId));
        if (codes != null) {
            qb.and().field(StationDao.Properties.Code).in(codes);
        }
        if (likeQuery != null) {
            qb.and().field(StationDao.Properties.Name).like("%" + likeQuery + "%");
        }

        Cursor cursor = null;
        Set<Long> outCodes = new HashSet<>();
        try {
            cursor = qb.build().run(daoSession().getNsiDb());
            while (cursor.moveToNext()) {
                outCodes.add(cursor.getLong(0));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return outCodes;
    }

    /**
     * Выполянет фильтрацию кодов станций, оставляя только транзитные.
     *
     * @param codes     Список кодов станций
     * @param versionId Версия НСИ
     * @return Список кодов транзитных станций
     */
    public List<Long> filterTransitStationCodes(@Nullable Iterable<Long> codes, int versionId) {
        QueryBuilder qb = new QueryBuilder();
        qb.select().field(StationDao.Properties.Code).from(StationDao.TABLE_NAME);
        qb.where().field(StationDao.Properties.IsTransitStation).eq(1);
        if (codes != null) {
            qb.and().field(StationDao.Properties.Code).in(codes);
        }
        qb.and().appendRaw(NsiUtils.checkVersion(StationDao.TABLE_NAME, versionId));

        Cursor cursor = null;
        List<Long> transitCodes = new ArrayList<>();
        try {
            cursor = qb.build().run(daoSession().getNsiDb());
            while (cursor.moveToNext()) {
                transitCodes.add(cursor.getLong(0));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return transitCodes;
    }

    public List<Long> loadStationsOnRoute(@Nullable Iterable<Long> stationCodes, int versionId) {
        return new StationsOnRouteQuery(daoSession(), stationCodes, versionId).query();
    }

    @NonNull
    public List<Station> loadStationsForProductionSections(@NonNull List<Long> allowedProductionSectionCodes, int versionId) {
        return new StationsForProductionSectionsQuery(daoSession(), allowedProductionSectionCodes, versionId).query();
    }

    /**
     * Ищет коды всех автобусных станций
     *
     * @param versionId версия НСИ
     * @return список кодов всех автобусных станций
     */
    @NonNull
    public List<Long> loadTransferStations(int versionId) {
        QueryBuilder qb = new QueryBuilder();
        qb.select().field(StationDao.Properties.Code).from(StationDao.TABLE_NAME);
        qb.where().field(StationDao.Properties.IsForTransfer).eq(1);
        qb.and().appendRaw(NsiUtils.checkVersion(StationDao.TABLE_NAME, versionId));

        List<Long> transferCodes = new ArrayList<>();
        Cursor cursor = qb.build().run(daoSession().getNsiDb());

        try {
            while (cursor.moveToNext()) {
                transferCodes.add(cursor.getLong(0));
            }
        } finally {
            cursor.close();
        }

        return transferCodes;
    }

    /**
     * Ищет все возможные станции назначения для конкретного маршрута и станции отправления
     *
     * @param routeCode            маршрут
     * @param departureStationCode код станции отправления
     * @param back                 false - туда, true - обратно
     * @param versionId            версия НСИ
     * @return все возможные станции назначения для конкретного маршрута и станции отправления
     */
    public List<Long> loadDestinationTransferStations(@NonNull String routeCode, long departureStationCode, boolean back, int versionId) {
        QueryBuilder qb = new QueryBuilder();
        // SELECT StationCode FROM StationsOnRoutes
        qb.select().field(StationsOnRouteDao.Properties.StationCode).from(StationsOnRouteDao.TABLE_NAME);
        // WHERE  RouteCode = ?
        qb.where().field(StationsOnRouteDao.Properties.RouteCode).eq(routeCode);
        // AND StationNumber
        qb.and().field(StationsOnRouteDao.Properties.StationNumber);
        // >
        if (!back) {
            qb.more();
        } else { // <
            qb.less();
        }
        // (
        qb.appendInBrackets(() -> {
            // SELECT StationNumber FROM StationsOnRoutes
            qb.select().field(StationsOnRouteDao.Properties.StationNumber).from(StationsOnRouteDao.TABLE_NAME);
            // WHERE StationCode = ?
            qb.where().field(StationsOnRouteDao.Properties.StationCode).eq(departureStationCode);
            // AND StationsOnRoutes.VersionId <= ? AND (StationsOnRoutes.DeleteInVersionId > ? OR StationsOnRoutes.DeleteInVersionId IS NULL)
            qb.and().appendRaw(NsiUtils.checkVersion(StationsOnRouteDao.TABLE_NAME, versionId));
        });
        // ) AND StationsOnRoutes.VersionId <= ? AND (StationsOnRoutes.DeleteInVersionId > ? OR StationsOnRoutes.DeleteInVersionId IS NULL)
        qb.and().appendRaw(NsiUtils.checkVersion(StationsOnRouteDao.TABLE_NAME, versionId));

        List<Long> stationCodes = new ArrayList<>();
        Cursor cursor = qb.build().run(daoSession().getNsiDb());

        try {
            while (cursor.moveToNext()) {
                stationCodes.add(cursor.getLong(0));
            }
        } finally {
            cursor.close();
        }

        return stationCodes;
    }

    /**
     * Ищет все возможные автобусные маршруты в направлении
     *
     * @param back      false - туда, true - обратно
     * @param versionId версия НСИ
     * @return все возможные автобусные маршруты в направлении
     */
    public List<TransferStationRoute> loadTransferRoutes(boolean back, int versionId) {
        QueryBuilder qb = new QueryBuilder();
        String sor = "sor";
        // SELECT (...), (...), sor.RouteCode
        qb.select().appendInBrackets(() -> selectFirstOrLastStationOfRoute(qb, sor, back, versionId)).comma();
        qb.appendInBrackets(() -> selectFirstOrLastStationOfRoute(qb, sor, !back, versionId)).comma();
        qb.field(sor, StationsOnRouteDao.Properties.RouteCode);
        // FROM  StationsOnRoutes AS sor
        qb.from(StationsOnRouteDao.TABLE_NAME).as(sor);
        // WHERE sor.RouteCode IN (
        qb.where().field(sor, StationsOnRouteDao.Properties.RouteCode).in().appendInBrackets(() -> {
            // SELECT RouteCode FROM StationTransferRoutes
            qb.select().field(StationTransferRouteDao.Properties.RouteCode).from(StationTransferRouteDao.TABLE_NAME);
            // WHERE StationTransferRouteDao.VersionId <= ? AND (StationTransferRouteDao.DeleteInVersionId > ? OR StationTransferRouteDao.DeleteInVersionId IS NULL)
            qb.where().appendRaw(NsiUtils.checkVersion(StationTransferRouteDao.TABLE_NAME, versionId));
        });
        // ) AND sor.VersionId <= ? AND (sor.DeleteInVersionId > ? OR sor.DeleteInVersionId IS NULL)
        qb.and().appendRaw(NsiUtils.checkVersion(sor, versionId));
        // GROUP BY sor.RouteCode;
        qb.groupBy(sor, StationsOnRouteDao.Properties.RouteCode);

        List<TransferStationRoute> transferStationRoutes = new ArrayList<>();
        Cursor cursor = qb.build().run(daoSession().getNsiDb());

        try {
            while (cursor.moveToNext()) {
                TransferStationRoute transferStationRoute = new TransferStationRoute();
                transferStationRoute.setDepStationCode(cursor.getLong(0));
                transferStationRoute.setDestStationCode(cursor.getLong(1));
                transferStationRoute.setRouteCode(cursor.getString(2));

                transferStationRoutes.add(transferStationRoute);
            }
        } finally {
            cursor.close();
        }

        return transferStationRoutes;
    }

    private void selectFirstOrLastStationOfRoute(QueryBuilder qb, String veryOuterStationsOnRoutesAlias, boolean last, int versionId) {
        String s = "s", sor3 = "sor3", sor4 = "sor4";
        // SELECT s.Code FROM Stations AS s WHERE s.Code =
        qb.select().field(s, StationDao.Properties.Code);
        qb.from(StationDao.TABLE_NAME).as(s);
        qb.where().field(s, StationDao.Properties.Code).eq();
        // (
        qb.appendInBrackets(() -> {
            // SELECT sor4.StationCode FROM StationsOnRoutes AS sor4 WHERE sor4.StationNumber =
            qb.select().field(sor4, StationsOnRouteDao.Properties.StationCode);
            qb.from(StationsOnRouteDao.TABLE_NAME).as(sor4);
            qb.where().field(sor4, StationsOnRouteDao.Properties.StationNumber).eq();
            // (
            qb.appendInBrackets(() -> {
                // SELECT
                qb.select();
                // MAX(sor3.StationNumber)
                if (last) {
                    qb.max(sor3, StationsOnRouteDao.Properties.StationNumber);
                } else { // MIN(sor3.StationNumber)
                    qb.min(sor3, StationsOnRouteDao.Properties.StationNumber);
                }
                // FROM StationsOnRoutes AS sor3 WHERE sor3.RouteCode = sor.RouteCode
                qb.from(StationsOnRouteDao.TABLE_NAME).as(sor3);
                qb.where().field(sor3, StationsOnRouteDao.Properties.RouteCode).eq().field(veryOuterStationsOnRoutesAlias, StationsOnRouteDao.Properties.RouteCode);
                //  AND sor3.VersionId <= ? AND (sor3.DeleteInVersionId > ? OR sor3.DeleteInVersionId IS NULL)
                qb.and().appendRaw(NsiUtils.checkVersion(sor3, versionId));
            });
            // ) AND sor4.RouteCode = sor.RouteCode
            qb.and().field(sor4, StationsOnRouteDao.Properties.RouteCode).eq().field(veryOuterStationsOnRoutesAlias, StationsOnRouteDao.Properties.RouteCode);
            // AND sor4.VersionId <= ? AND (sor4.DeleteInVersionId > ? OR sor4.DeleteInVersionId IS NULL)
            qb.and().appendRaw(NsiUtils.checkVersion(sor4, versionId));
        });
        // ) AND s.VersionId <= ? AND (s.DeleteInVersionId > ? OR s.DeleteInVersionId IS NULL)
        qb.and().appendRaw(NsiUtils.checkVersion(s, versionId));
    }

    /**
     * Вернет маршрут трансфера для станций
     *
     * @param stationCodeDeparture   - код станции отправления
     * @param stationCodeDestination - код станции назначения
     * @param versionId              - версия НСИ
     * @return
     */
    public TransferStationRoute loadTransferRouteForStations(long stationCodeDeparture, long stationCodeDestination, int versionId) {
        if (stationCodeDeparture <= 0 || stationCodeDestination <= 0) return null;

        List<TransferStationRoute> transferRoutes = loadTransferRoutes(false, versionId);
        for (TransferStationRoute transferRoute : transferRoutes) {
            if (transferRoute.getDepStationCode() == stationCodeDeparture && transferRoute.getDestStationCode() == stationCodeDestination) {
                return transferRoute;
            } else if (transferRoute.getDepStationCode() == stationCodeDestination && transferRoute.getDestStationCode() == stationCodeDeparture) {
                return transferRoute;
            }
        }

        return null;
    }

    /**
     * Возвращает список кодов станций для всех переданных маршрутов
     *
     * @param routeCodes список кодов маршрутов
     * @param versionId  версия НСИ
     * @return список кодов станций для всех переданных маршрутов
     */
    public List<Long> loadStationCodesForRouteCodes(@NonNull List<Long> routeCodes, int versionId) {
        // В таблице StationsOnRoutes у RouteCode спереди стоит 0,
        // поэтому если отдать просто число запрос ничего не вернет
        // приклеиваем 0 спереди чтобы запрос работал
        // В будущем убрать когда коды будут совпадать
        List<String> fixedRouteCodes = new ArrayList<>();

        for (Long routeCode : routeCodes) {
            fixedRouteCodes.add("0" + routeCode);
        }

        Set<Long> stationCodes = new HashSet<>();
        QueryBuilder qb = new QueryBuilder();

        qb.select().field(StationsOnRouteDao.Properties.StationCode).from(StationsOnRouteDao.TABLE_NAME);
        qb.where().field(StationsOnRouteDao.Properties.RouteCode).in(fixedRouteCodes);
        qb.and().appendRaw(NsiUtils.checkVersion(StationsOnRouteDao.TABLE_NAME, versionId));

        Cursor cursor = qb.build().run(daoSession().getNsiDb());

        try {
            while (cursor.moveToNext()) {
                stationCodes.add(cursor.getLong(0));
            }
        } finally {
            cursor.close();
        }

        return new ArrayList<>(stationCodes);
    }

}
