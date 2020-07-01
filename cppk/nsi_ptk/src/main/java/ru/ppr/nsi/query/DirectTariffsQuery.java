package ru.ppr.nsi.query;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.TariffDao;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.repository.TariffRepository;

/**
 * Запрос на список прямых тарифов между станциями.
 *
 * @author Aleksandr Brazhkin
 */
public class DirectTariffsQuery extends BaseNsiQuery {

    private static final String THERE = "THERE";
    private static final String THERE_TARIFF_CODE = "THERE_TARIFF_CODE";
    private static final String THERE_DEP_STATION_CODE = "THERE_DEP_STATION_CODE";
    private static final String THERE_DEST_STATION_CODE = "THERE_DEST_STATION_CODE";
    private static final String THERE_TARIFF_PLAN_CODE = "THERE_TARIFF_PLAN_CODE";
    private static final String THERE_TICKET_TYPE_CODE = "THERE_TICKET_TYPE_CODE";
    private static final String BACK = "BACK";
    private static final String BACK_TARIFF_CODE = "BACK_TARIFF_CODE";
    private static final String BACK_DEP_STATION_CODE = "BACK_DEP_STATION_CODE";
    private static final String BACK_DEST_STATION_CODE = "BACK_DEST_STATION_CODE";
    private static final String BACK_TARIFF_PLAN_CODE = "BACK_TARIFF_PLAN_CODE";
    private static final String BACK_TICKET_TYPE_CODE = "BACK_TICKET_TYPE_CODE";

    private TariffRepository tariffRepository;

    /**
     * Список кодов станций отправления
     */
    private final List<Long> fromStationCodes;
    /**
     * Список кодов станций назначения
     */
    private final List<Long> toStationCodes;
    /**
     * Код типа билета
     */
    private final List<Long> ticketTypeCodes;
    /**
     * Список кодовтарифных планов
     */
    private final List<Integer> tariffPlanCodes;
    /**
     * Версия НСИ
     */
    private final int versionId;

    public DirectTariffsQuery(NsiDaoSession nsiDaoSession,
                              TariffRepository tariffRepository,
                              List<Long> fromStationCodes,
                              List<Long> toStationCodes,
                              List<Long> ticketTypeCodes,
                              List<TariffPlan> tariffPlans,
                              int versionId) {
        super(nsiDaoSession);
        this.tariffRepository = tariffRepository;
        this.fromStationCodes = fromStationCodes;
        this.toStationCodes = toStationCodes;
        this.ticketTypeCodes = ticketTypeCodes;
        if (tariffPlans != null) {
            tariffPlanCodes = new ArrayList<>();
            for (TariffPlan tariffPlan : tariffPlans) {
                tariffPlanCodes.add(tariffPlan.getCode());
            }
        } else {
            tariffPlanCodes = null;
        }
        this.versionId = versionId;
    }

    /**
     * Выполняет запрос.
     *
     * @return Тариф "Туда" и парный ему тариф "Обратно"
     */
    @Nullable
    public Pair<Tariff, Tariff> query() {
        QueryBuilder qb = new QueryBuilder();
        qb.selectAll().from();
        qb.appendInBrackets(() -> {
            qb.select();
            qb.field(BaseEntityDao.Properties.Code).as(THERE_TARIFF_CODE).comma();
            qb.field(TariffDao.Properties.StationDepartureCode).as(THERE_DEP_STATION_CODE).comma();
            qb.field(TariffDao.Properties.StationDestinationCode).as(THERE_DEST_STATION_CODE).comma();
            qb.field(TariffDao.Properties.TariffPlanCode).as(THERE_TARIFF_PLAN_CODE).comma();
            qb.field(TariffDao.Properties.TicketTypeCode).as(THERE_TICKET_TYPE_CODE);
            qb.from(TariffDao.TABLE_NAME);
            qb.where().trueCond();
            if (ticketTypeCodes != null) {
                qb.and().field(TariffDao.Properties.TicketTypeCode).in(ticketTypeCodes);
            }
            if (fromStationCodes != null) {
                qb.and().field(TariffDao.Properties.StationDepartureCode).in(fromStationCodes);
            }
            if (toStationCodes != null) {
                qb.and().field(TariffDao.Properties.StationDestinationCode).in(toStationCodes);
            }
            if (tariffPlanCodes != null) {
                qb.and().field(TariffDao.Properties.TariffPlanCode).in(tariffPlanCodes);
            }
            qb.and().appendRaw(checkVersion(versionId));
        }).as(THERE);
        /////////////////////////////////////
        qb.leftJoin();
        /////////////////////////////////////
        qb.appendInBrackets(() -> {
            qb.select();
            qb.field(BaseEntityDao.Properties.Code).as(BACK_TARIFF_CODE).comma();
            qb.field(TariffDao.Properties.StationDepartureCode).as(BACK_DEP_STATION_CODE).comma();
            qb.field(TariffDao.Properties.StationDestinationCode).as(BACK_DEST_STATION_CODE).comma();
            qb.field(TariffDao.Properties.TariffPlanCode).as(BACK_TARIFF_PLAN_CODE).comma();
            qb.field(TariffDao.Properties.TicketTypeCode).as(BACK_TICKET_TYPE_CODE);
            qb.from(TariffDao.TABLE_NAME);
            qb.where().trueCond();
            if (ticketTypeCodes != null) {
                qb.and().field(TariffDao.Properties.TicketTypeCode).in(ticketTypeCodes);
            }
            if (fromStationCodes != null) {
                qb.and().field(TariffDao.Properties.StationDestinationCode).in(fromStationCodes);
            }
            if (toStationCodes != null) {
                qb.and().field(TariffDao.Properties.StationDepartureCode).in(toStationCodes);
            }
            qb.and().appendRaw(checkVersion(versionId));
        }).as(BACK);
        /////////////////////////////////////
        qb.on().appendInBrackets(() -> {
            qb.f1EqF2(THERE_TARIFF_PLAN_CODE, BACK_TARIFF_PLAN_CODE);
            qb.and().f1EqF2(THERE_TICKET_TYPE_CODE, BACK_TICKET_TYPE_CODE);
            qb.and().f1EqF2(THERE_DEP_STATION_CODE, BACK_DEST_STATION_CODE);
            qb.and().f1EqF2(THERE_DEST_STATION_CODE, BACK_DEP_STATION_CODE);
        });
        /////////////////////////////////////
        qb.orderBy(BACK_TARIFF_CODE).desc();
        qb.limit(1);

        Cursor cursor = null;
        try {
            Query query = qb.build();
            cursor = query.run(db());

            if (cursor.moveToNext()) {
                Tariff thereTariff = null;
                if (!cursor.isNull(cursor.getColumnIndex(THERE_TARIFF_CODE))) {
                    long thereTariffCode = cursor.getLong(cursor.getColumnIndex(THERE_TARIFF_CODE));
                    thereTariff = tariffRepository.load(thereTariffCode, versionId);
                }
                Tariff backTariff = null;
                if (!cursor.isNull(cursor.getColumnIndex(BACK_TARIFF_CODE))) {
                    long backTariffCode = cursor.getLong(cursor.getColumnIndex(BACK_TARIFF_CODE));
                    backTariff = tariffRepository.load(backTariffCode, versionId);
                }
                return new Pair<>(thereTariff, backTariff);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

}
