package ru.ppr.nsi.query.stationswithtariffs;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.TariffDao;
import ru.ppr.nsi.model.TariffDescription;

/**
 * Стандартная реализация запроса на поиск информации о тарифах.
 * Для выборки только кодсв станций отправления или назначения
 * использовать {@link StationsWithTariffsQuery}.
 *
 * @author Aleksandr Brazhkin
 */
public class TariffsQuery extends BaseTariffsQuery {
    /**
     * Актуальные поля и выбранные индексы, необходимые для выборки
     */
    private final Map<TariffDescription.Field, Integer> actualEntryFields = new HashMap<>();

    public TariffsQuery(NsiDaoSession nsiDaoSession,
                        @NonNull Params params,
                        @NonNull EnumSet<TariffDescription.Field> entryFields) {
        super(nsiDaoSession, params);
        fillActualEntryFields(entryFields);
    }

    /**
     * Формирует индексы полей для выборки из БД
     *
     * @param entryFields Поля для выборки
     */
    private void fillActualEntryFields(@NonNull EnumSet<TariffDescription.Field> entryFields) {
        int i = 0;
        for (TariffDescription.Field field : entryFields) {
            actualEntryFields.put(field, i++);
        }
    }

    @Override
    @NonNull
    List<TariffDescription> queryImpl(@Nullable Collection<Long> allowedDepStationCodes,
                                      @Nullable Collection<Long> allowedDestStationCodes) {
        QueryBuilder qb = new QueryBuilder();
        // SELECT DISTINCT
        qb.selectDistinct();
        // DepartureStationCode, DestinationStationCode, TicketTypeCode, TariffPlanCode, Code
        appendSelectFields(qb);
        // FROM Tariffs
        qb.from(TariffDao.TABLE_NAME);
        // WHERE
        qb.where();
        // Tariffs.VersionId <= @versionId AND (Tariffs.DeleteInVersionId > @versionId OR Tariffs.DeleteInVersionId IS NULL)
        qb.appendRaw(checkVersion(TariffDao.TABLE_NAME, params.getVersionId()));
        if (allowedDepStationCodes != null) {
            // AND DepartureStationCode IN (SELECT Code FROM ALLOWED_DEP_STATION_CODES)
            qb.and().field(TariffDao.Properties.StationDepartureCode).in().appendInBrackets(() -> {
                qb.select().field(CODE).from(ALLOWED_DEP_STATION_CODES);
            });
        }
        if (allowedDestStationCodes != null) {
            // AND DestinationStationCode IN (SELECT Code FROM ALLOWED_DEST_STATION_CODES)
            qb.and().field(TariffDao.Properties.StationDestinationCode).in().appendInBrackets(() -> {
                qb.select().field(CODE).from(ALLOWED_DEST_STATION_CODES);
            });
        }
        if (params.getAllowedTicketTypeCodes() != null) {
            // AND TicketTypeCode IN (@allowedTicketTypeCodes)
            qb.and().field(TariffDao.Properties.TicketTypeCode).in(params.getAllowedTicketTypeCodes());
        }
        if (params.getAllowedTariffPlanCodes() != null) {
            // AND TariffPlanCode IN (@allowedTariffPlanCodes)
            qb.and().field(TariffDao.Properties.TariffPlanCode).in(params.getAllowedTariffPlanCodes());
        }

        List<TariffDescription> tariffs = new ArrayList<>();

        Query query = qb.build();
        query.logQuery();

        Cursor cursor = query.run(db());

        try {
            while (cursor.moveToNext()) {
                tariffs.add(cursorToEntry(cursor));
            }
        } finally {
            cursor.close();
        }

        return tariffs;
    }

    /**
     * Добавляет в запрос набор колонок таблицы Tariffs для выборки.
     *
     * @param qb Билдер запроса
     */
    private void appendSelectFields(QueryBuilder qb) {
        List<TariffDescription.Field> fields = new ArrayList<>(actualEntryFields.keySet());
        Collections.sort(fields, (field1, field2) -> actualEntryFields.get(field1) - actualEntryFields.get(field2));
        for (Iterator<TariffDescription.Field> iterator = fields.iterator(); iterator.hasNext(); ) {
            TariffDescription.Field field = iterator.next();

            switch (field) {
                case DEP_STATION_CODE:
                    qb.field(TariffDao.Properties.StationDepartureCode);
                    break;
                case DEST_STATION_CODE:
                    qb.field(TariffDao.Properties.StationDestinationCode);
                    break;
                case TARIFF_PLAN_CODE:
                    qb.field(TariffDao.Properties.TariffPlanCode);
                    break;
                case TICKET_TYPE_CODE:
                    qb.field(TariffDao.Properties.TicketTypeCode);
                    break;
                case TARIFF_CODE:
                    qb.field(TariffDao.Properties.Code);
                    break;
            }

            if (iterator.hasNext()) {
                qb.comma();
            }
        }
    }

    @NonNull
    private TariffDescription cursorToEntry(@NonNull Cursor cursor) {
        TariffDescription entry = new TariffDescription();

        for (Map.Entry<TariffDescription.Field, Integer> mapEntry : actualEntryFields.entrySet()) {
            Long value = cursor.getLong(mapEntry.getValue());

            switch (mapEntry.getKey()) {
                case DEP_STATION_CODE:
                    entry.setDepStationCode(value);
                    break;
                case DEST_STATION_CODE:
                    entry.setDestStationCode(value);
                    break;
                case TARIFF_PLAN_CODE:
                    entry.setTariffPlanCode(value);
                    break;
                case TICKET_TYPE_CODE:
                    entry.setTicketTypeCode(value);
                    break;
                case TARIFF_CODE:
                    entry.setTariffCode(value);
                    break;
            }
        }

        return entry;
    }
}
