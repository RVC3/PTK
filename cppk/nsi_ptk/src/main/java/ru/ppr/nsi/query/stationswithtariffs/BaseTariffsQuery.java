package ru.ppr.nsi.query.stationswithtariffs;

import android.content.ContentValues;
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
import ru.ppr.nsi.model.TariffDescription;
import ru.ppr.nsi.query.BaseNsiQuery;

/**
 * Базовый клаасс для запроса на поиск тарифов.
 * Содержит в себе логику для управления временными таблицами с ограничивающим списком кодов станций.
 *
 * @author Aleksandr Brazhkin
 */
abstract class BaseTariffsQuery extends BaseNsiQuery {
    /**
     * Наименование временной таблицы для кодов станций отправления
     */
    static final String ALLOWED_DEP_STATION_CODES = "ALLOWED_DEP_STATION_CODES";
    /**
     * Наименование временной таблицы для кодов станций назначения
     */
    static final String ALLOWED_DEST_STATION_CODES = "ALLOWED_DEST_STATION_CODES";
    /**
     * Наименование колонки во временных таблицах
     */
    static final String CODE = "Code";

    @NonNull
    protected final Params params;

    BaseTariffsQuery(NsiDaoSession nsiDaoSession, @NonNull Params params) {
        super(nsiDaoSession);
        this.params = params;
    }

    /**
     * Выполняет запрос на информацию о тарифах.
     *
     * @return писок элементов с информацией о тарифах
     */
    public List<TariffDescription> query() {
        Collection<Long> allowedDepStationCodes = mapToAllowedStations(
                params.getAllowedDepStationCodes(),
                params.getDeniedDepStationCodes()
        );

        Collection<Long> allowedDestStationCodes = mapToAllowedStations(
                params.getAllowedDestStationCodes(),
                params.getDeniedDestStationCodes()
        );
        try {
            fillTable(ALLOWED_DEP_STATION_CODES, allowedDepStationCodes);
            fillTable(ALLOWED_DEST_STATION_CODES, allowedDestStationCodes);
            return queryImpl(allowedDepStationCodes, allowedDestStationCodes);
        } finally {
            clearTempTables();
        }
    }

    /**
     * Реализация запроса.
     *
     * @param allowedDepStationCodes  Список разрешенных кодов станций отправления
     * @param allowedDestStationCodes Список разрешенных кодов станций назначения
     * @return Список элементов с информацией о тарифах
     */
    @NonNull
    abstract List<TariffDescription> queryImpl(@Nullable Collection<Long> allowedDepStationCodes,
                                               @Nullable Collection<Long> allowedDestStationCodes);

    /**
     * Выполняет объединение двух списков {@code allowedStations} и {@code deniedStations} в один allowedStations.
     *
     * @param allowedStations Список разрешенных кодов станций
     * @param deniedStations  Список запрещенных кодов станций
     * @return Новый список разрешенных кодов станций
     */
    @Nullable
    private Collection<Long> mapToAllowedStations(@Nullable Collection<Long> allowedStations,
                                                  @Nullable Collection<Long> deniedStations) {

        if (deniedStations == null) {
            // Если списка запрещенных кодов станций нет,
            // можно просто использовать список разрешенных кодов без изменений
            return allowedStations;
        }

        if (allowedStations != null) {
            // Если есть список разрешенных кодов станций,
            // исключаем из него список запрещенных кодов
            allowedStations.removeAll(deniedStations);
            return allowedStations;
        }

        // Если список разрешенных кодов станий отсутствует,
        // но есть список запрещенных - самостоятельно
        // формируем список разрешенных кодов обходом таблицы Stations
        // с исключением из выборки запрещенных кодов

        QueryBuilder qb = new QueryBuilder();
        // SELECT DISTINCT Code FROM Stations WHERE
        qb.selectDistinct().field(StationDao.Properties.Code).from(StationDao.TABLE_NAME).where();
        // Stations.VersionId <= @versionId AND (Stations.DeleteInVersionId > @versionId OR Stations.DeleteInVersionId IS NULL)
        qb.appendRaw(checkVersion(StationDao.TABLE_NAME, params.getVersionId()));
        // AND Code NOT IN (@deniedStations)
        qb.and().field(StationDao.Properties.Code).notIn(deniedStations);

        List<Long> stationCodes = new ArrayList<>();

        Query query = qb.build();
        query.logQuery();

        Cursor cursor = query.run(db());
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
     * Выполняет запись кодов станций во временную таблицу.
     *
     * @param tableName    Имя таблицы
     * @param stationCodes Список кодов станций
     */
    private void fillTable(@NonNull String tableName, @Nullable Collection<Long> stationCodes) {
        if (stationCodes == null) {
            return;
        }
        createTableIfNeed(tableName);
        db().beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            for (Long code : stationCodes) {
                cv.put(CODE, code);
                db().insert(tableName, null, cv);
            }
            db().setTransactionSuccessful();
        } finally {
            db().endTransaction();
        }
    }

    /**
     * Выподняет создание временной таблицы.
     *
     * @param tableName Имя таблицы
     */
    private void createTableIfNeed(String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TEMP TABLE IF NOT EXISTS ");
        sb.append(tableName);
        sb.append(" (Code INTEGER NOT NULL PRIMARY KEY)");
        db().execSQL(sb.toString());
    }

    /**
     * Удаляяет временные таблицы
     */
    private void clearTempTables() {
        dropTableIfNeed(ALLOWED_DEP_STATION_CODES);
        dropTableIfNeed(ALLOWED_DEST_STATION_CODES);
    }

    /**
     * Удаляяет временную таблицу
     *
     * @param tableName Имя таблицы
     */
    private void dropTableIfNeed(String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE IF EXISTS ");
        sb.append(tableName);
        db().execSQL(sb.toString());
    }
}
