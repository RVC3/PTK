package ru.ppr.nsi.query;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.SqLiteUtils;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.StationDao;
import ru.ppr.nsi.dao.TariffDao;
import ru.ppr.nsi.dao.TariffPlanDao;
import ru.ppr.nsi.entity.TariffPlan;

/**
 * Запрос спсика тарифных планов.
 *
 * @author Aleksandr Brazhkin
 */
public class TariffPlansQuery extends BaseNsiQuery {

    /**
     * Код станции отправления, -1 - для любой станции
     */
    private final long fromStationCode;
    /**
     * Код станции назначения, -1 - для любой станции
     */
    private final long toStationCode;
    /**
     * {@code true} - Искать тарифный планы для доплаты, {@code false} - иначе
     */
    private final boolean isSurcharge;
    /**
     * Код категории поезда, -1 - для любой категории
     */
    private final int trainCategoryCode;
    /**
     * Ограничивающий список типов билетов
     */
    @Nullable
    private final Iterable<Long> allowedTicketTypeCodes;
    /**
     * Версия НСИ
     */
    private final int versionId;


    public TariffPlansQuery(NsiDaoSession nsiDaoSession,
                            long fromStationCode,
                            long toStationCode,
                            boolean isSurcharge,
                            int trainCategoryCode,
                            @Nullable Iterable<Long> allowedTicketTypeCodes,
                            int versionId) {
        super(nsiDaoSession);
        this.fromStationCode = fromStationCode;
        this.toStationCode = toStationCode;
        this.isSurcharge = isSurcharge;
        this.trainCategoryCode = trainCategoryCode;
        this.allowedTicketTypeCodes = allowedTicketTypeCodes;
        this.versionId = versionId;
    }


    /**
     * Выполняет запрос.
     *
     * @return Список тарифных планов
     */
    public List<TariffPlan> query() {

        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT DISTINCT ");
        stringBuilder.append("TARIFF_PLANS.*");
        stringBuilder.append(" FROM ");
        /////////////////////////////////////
        {
            stringBuilder.append("(");
            stringBuilder.append("SELECT ");
            stringBuilder.append("*");
            stringBuilder.append(" FROM ");
            stringBuilder.append(TariffPlanDao.TABLE_NAME);
            stringBuilder.append(" WHERE ");
            stringBuilder.append(TariffPlanDao.Properties.IsSurcharge).append(" = ").append("?");
            selectionArgsList.add(String.valueOf(isSurcharge ? 1 : 0));
            if (trainCategoryCode > -1) {
                // Только для категории trainCategoryCode
                stringBuilder.append(" AND ");
                stringBuilder.append(TariffPlanDao.Properties.TrainCategoryCode).append(" = ").append("?");
                selectionArgsList.add(String.valueOf(trainCategoryCode));
            }
            stringBuilder.append(" AND ");
            stringBuilder.append(checkVersion(TariffPlanDao.TABLE_NAME, versionId));
            stringBuilder.append(")").append(" AS TARIFF_PLANS");
        }
        /////////////////////////////////////
        stringBuilder.append(" JOIN ");
        /////////////////////////////////////
        {
            stringBuilder.append("(");
            stringBuilder.append("SELECT ");
            stringBuilder.append(TariffDao.Properties.Code).append(" AS TARIFF_CODE").append(", ");
            stringBuilder.append(TariffDao.Properties.TariffPlanCode).append(" AS TARIFF_PLAN_CODE").append(", ");
            stringBuilder.append(TariffDao.Properties.TicketTypeCode).append(" AS TICKET_TYPE_CODE");
            stringBuilder.append(" FROM ");
            stringBuilder.append(TariffDao.TABLE_NAME);
            stringBuilder.append(" WHERE ");
            stringBuilder.append("1 = 1");
            if (allowedTicketTypeCodes != null) {
                stringBuilder.append(" AND ");
                stringBuilder.append(TariffDao.Properties.TicketTypeCode);
                stringBuilder.append(" IN ");
                stringBuilder.append(" ( ");
                int ticketTypeCodeCount = 0;
                for (Long ticketTypeCode : allowedTicketTypeCodes) {
                    selectionArgsList.add(String.valueOf(ticketTypeCode));
                    ticketTypeCodeCount++;
                }
                stringBuilder.append(SqLiteUtils.makePlaceholders(ticketTypeCodeCount));
                stringBuilder.append(" ) ");
            }
            if (fromStationCode > -1) {
                // Только от станции fromStationCode
                stringBuilder.append(" AND ");
                stringBuilder.append(TariffDao.Properties.StationDepartureCode).append(" = ").append("?");
                selectionArgsList.add(String.valueOf(fromStationCode));
            }
            if (toStationCode > -1) {
                // Только до станции toStationCode
                stringBuilder.append(" AND ");
                stringBuilder.append(TariffDao.Properties.StationDestinationCode).append(" = ").append("?");
                selectionArgsList.add(String.valueOf(toStationCode));
            }
            stringBuilder.append(" AND ");
            stringBuilder.append(checkVersion(TariffDao.TABLE_NAME, versionId));
            stringBuilder.append(" UNION ");
            stringBuilder.append("SELECT ");
            stringBuilder.append("TARIFFS1").append(".").append(TariffDao.Properties.Code).append(" AS TARIFF_CODE").append(", ");
            stringBuilder.append("TARIFFS1").append(".").append(TariffDao.Properties.TariffPlanCode).append(" AS TARIFF_PLAN_CODE").append(", ");
            stringBuilder.append("TARIFFS1").append(".").append(TariffDao.Properties.TicketTypeCode).append(" AS TICKET_TYPE_CODE");
            stringBuilder.append(" FROM ");
            stringBuilder.append(TariffDao.TABLE_NAME).append(" AS ").append("TARIFFS1");
            stringBuilder.append(" INNER JOIN ");
            stringBuilder.append(TariffDao.TABLE_NAME).append(" AS ").append("TARIFFS2");
            stringBuilder.append(" ON ");
            stringBuilder.append("TARIFFS1").append(".").append(TariffDao.Properties.TariffPlanCode);
            stringBuilder.append(" = ");
            stringBuilder.append("TARIFFS2").append(".").append(TariffDao.Properties.TariffPlanCode);
            stringBuilder.append(" AND ");
            stringBuilder.append("TARIFFS1").append(".").append(TariffDao.Properties.StationDestinationCode);
            stringBuilder.append(" = ");
            stringBuilder.append("TARIFFS2").append(".").append(TariffDao.Properties.StationDepartureCode);
            stringBuilder.append(" AND ");
            stringBuilder.append("TARIFFS1").append(".").append(TariffDao.Properties.StationDepartureCode);
            stringBuilder.append(" <> ");
            stringBuilder.append("TARIFFS2").append(".").append(TariffDao.Properties.StationDestinationCode);
            stringBuilder.append(" AND ");
            stringBuilder.append("TARIFFS1").append(".").append(TariffDao.Properties.TariffPlanCode);
            stringBuilder.append(" = ");
            stringBuilder.append("TARIFFS2").append(".").append(TariffDao.Properties.TariffPlanCode);
            stringBuilder.append(" AND ");
            stringBuilder.append("TARIFFS1").append(".").append(TariffDao.Properties.TicketTypeCode);
            stringBuilder.append(" = ");
            stringBuilder.append("TARIFFS2").append(".").append(TariffDao.Properties.TicketTypeCode);
            stringBuilder.append(" INNER JOIN ");
            stringBuilder.append(StationDao.TABLE_NAME).append(" AS ").append("MID_STATIONS");
            stringBuilder.append(" ON ");
            stringBuilder.append("TARIFFS1").append(".").append(TariffDao.Properties.StationDestinationCode);
            stringBuilder.append(" = ");
            stringBuilder.append("MID_STATIONS").append(".").append(BaseEntityDao.Properties.Code);
            stringBuilder.append(" AND ");
            stringBuilder.append("MID_STATIONS").append(".").append(StationDao.Properties.IsTransitStation).append(" = ").append("1");
            ///
            stringBuilder.append(" WHERE ");
            stringBuilder.append(checkVersion("TARIFFS1", versionId));
            stringBuilder.append(" AND ");
            stringBuilder.append(checkVersion("TARIFFS2", versionId));
            stringBuilder.append(" AND ");
            stringBuilder.append(checkVersion("MID_STATIONS", versionId));
            if (allowedTicketTypeCodes != null) {
                stringBuilder.append(" AND ");
                stringBuilder.append("TARIFFS1").append(".").append(TariffDao.Properties.TicketTypeCode);
                stringBuilder.append(" IN ");
                stringBuilder.append(" ( ");
                int ticketTypeCodeCount = 0;
                for (Long ticketType : allowedTicketTypeCodes) {
                    selectionArgsList.add(String.valueOf(ticketType));
                    ticketTypeCodeCount++;
                }
                stringBuilder.append(SqLiteUtils.makePlaceholders(ticketTypeCodeCount));
                stringBuilder.append(" ) ");
            }
            if (fromStationCode > -1) {
                // Только от станции fromStationCode
                stringBuilder.append(" AND ");
                stringBuilder.append("TARIFFS1").append(".").append(TariffDao.Properties.StationDepartureCode).append(" = ").append("?");
                selectionArgsList.add(String.valueOf(fromStationCode));
            }
            if (toStationCode > -1) {
                // Только до станции toStationCode
                stringBuilder.append(" AND ");
                stringBuilder.append("TARIFFS2").append(".").append(TariffDao.Properties.StationDestinationCode).append(" = ").append("?");
                selectionArgsList.add(String.valueOf(toStationCode));
            }
            stringBuilder.append(")").append(" AS TARIFFS");
        }
        /////////////////////////////////////
        stringBuilder.append(" ON ");
        stringBuilder.append("TARIFF_PLANS.").append(BaseEntityDao.Properties.Code).append(" = TARIFFS.TARIFF_PLAN_CODE");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        SqLiteUtils.logExplainQueryPlan(db(), stringBuilder.toString(), selectionArgs);

        List<TariffPlan> tariffPlans = new ArrayList<>();
        //LOGD("findAllTariffPlansForStations", "Start");
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                TariffPlan tariffPlan = nsiDaoSession().getTariffPlanDao().fromCursor(cursor);
                tariffPlan.setVersionId(versionId);
                tariffPlans.add(tariffPlan);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        //LOGD("findAllTariffPlansForStations", "End");
        return tariffPlans;
    }
}
