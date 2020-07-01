package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.TariffDao;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.model.TariffDescription;
import ru.ppr.nsi.query.DirectTariffsQuery;
import ru.ppr.nsi.query.stationswithtariffs.Params;
import ru.ppr.nsi.query.stationswithtariffs.StationsWithTariffsQuery;
import ru.ppr.nsi.query.stationswithtariffs.TariffsQuery;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class TariffRepository extends BaseRepository<Tariff, Long> {

    private static String TAG = Logger.makeLogTag(TariffRepository.class);

    @Inject
    TariffRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<Tariff, Long> selfDao() {
        return daoSession().getTariffDao();
    }

    @Override
    public Tariff load(Long code, int versionId) {
        Tariff loaded = super.load(code, versionId);

        if (loaded != null) {
            loaded.setVersionId(versionId);
        }

        return loaded;
    }

    @Override
    public List<Tariff> loadAll(Iterable<Long> codes, int versionId) {
        List<Tariff> loaded = super.loadAll(codes, versionId);

        for (Tariff tariff : loaded) {
            tariff.setVersionId(versionId);
        }

        return loaded;
    }

    public Tariff getTariffToCodeIgnoreDeleteFlag(long code, int version) {

        Tariff object = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("select * from ").append(TariffDao.TABLE_NAME).append(" where ")
                .append(BaseEntityDao.Properties.Code).append(" = ").append(code)
                .append(" AND ").append(BaseEntityDao.Properties.VersionId).append(" <= ").append(version)
                .append(" ORDER BY ").append(BaseEntityDao.Properties.VersionId).append(" DESC");
        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                object = daoSession().getTariffDao().readEntity(cursor, 0);
                object.setVersionId(version);
                // костыль, если тариф уже удален, то подменим ему версию на последнюю неудаленную
                if (object.getDeleteInVersion() != null && version >= object.getDeleteInVersion()) {
                    object.setVersionId(object.getDeleteInVersion() - 1);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return object;
    }

    public Pair<Tariff, Tariff> loadDirectTariffs(List<Long> fromStationCodes,
                                                  List<Long> toStationCodes,
                                                  List<Long> ticketTypeCodes,
                                                  List<TariffPlan> tariffPlans,
                                                  int versionId) {
        return new DirectTariffsQuery(
                daoSession(),
                this,
                fromStationCodes,
                toStationCodes,
                ticketTypeCodes,
                tariffPlans,
                versionId).query();
    }

    public Collection<TariffDescription> loadTariffsForSaleQuery(
            @Nullable Collection<Long> allowedDepStationCodes,
            @Nullable Collection<Long> allowedDestStationCodes,
            @Nullable Collection<Long> deniedDepStationCodes,
            @Nullable Collection<Long> deniedDestStationCodes,
            @Nullable Collection<Long> allowedTariffPlanCodes,
            @Nullable Collection<Long> allowedTicketTypeCodes,
            @NonNull EnumSet<TariffDescription.Field> entryFields,
            int versionId) {

        long startTime = System.currentTimeMillis();

        Params params = new Params(
                allowedDepStationCodes,
                allowedDestStationCodes,
                deniedDepStationCodes,
                deniedDestStationCodes,
                allowedTariffPlanCodes,
                allowedTicketTypeCodes,
                versionId
        );

        Collection<TariffDescription> tariffs;

        if (allowedDepStationCodes != null && allowedDepStationCodes.size() == 1) {
            // Неважно, какие поля мы хотим выбрать. Если ограничивающий список кодов станций отправления
            // содержит всего одну запись - используем стандартный общий запрос TariffsQuery.
            // Он работает быстрее, чем StationsWithTariffsQuery
            tariffs = new TariffsQuery(daoSession(), params, entryFields).query();
        } else if (allowedDestStationCodes != null && allowedDestStationCodes.size() == 1) {
            // Неважно, какие поля мы хотим выбрать. Если ограничивающий список кодов станций назначения
            // содержит всего одну запись - используем стандартный общий запрос TariffsQuery.
            // Он работает быстрее, чем StationsWithTariffsQuery
            tariffs = new TariffsQuery(daoSession(), params, entryFields).query();
        } else if (entryFields.size() == 1 && entryFields.contains(TariffDescription.Field.DEP_STATION_CODE)) {
            // Требуется найти только станции отправления, для которых есть тариф
            // Используем оптмизированный для такого случай запрос
            tariffs = new StationsWithTariffsQuery(daoSession(), params, true).query();
        } else if (entryFields.size() == 1 && entryFields.contains(TariffDescription.Field.DEST_STATION_CODE)) {
            // Требуется найти только станции назначения, для которых есть тариф
            // Используем оптмизированный для такого случай запрос
            tariffs = new StationsWithTariffsQuery(daoSession(), params, false).query();
        } else {
            // Требуется найти какие-то иные поля из таблицы Tariffs
            // Используем стандартный запрос
            tariffs = new TariffsQuery(daoSession(), params, entryFields).query();
        }

        long queryTime = System.currentTimeMillis() - startTime;
        Logger.trace(TAG, "size = " + tariffs.size() + ", time = " + queryTime);

        return tariffs;
    }

}
