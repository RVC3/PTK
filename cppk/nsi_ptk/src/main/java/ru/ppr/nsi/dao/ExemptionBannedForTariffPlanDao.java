package ru.ppr.nsi.dao;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;

/**
 * DAO для таблицы НСИ <i>ExemptionsBannedForTariffPlans</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ExemptionBannedForTariffPlanDao extends BaseEntityDao<String, Integer> {

    public static final String TABLE_NAME = "ExemptionsBannedForTariffPlans";

    public static class Properties {
        public static final String TariffPlanCode = "TariffPlanCode";
        public static final String ExemptionCode = "ExemptionCode";
        public static final String RegionOkatoCode = "RegionOkatoCode";
    }

    public ExemptionBannedForTariffPlanDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String fromCursor(Cursor cursor) {

        return null;
    }

    /**
     * Возвращает тарифные, для которых которых нельзя производить оформление льготы
     *
     * @param exemption
     * @param versionId
     * @return
     */
    public
    @NonNull
    List<Integer> getBannedTariffPlanCodes(Exemption exemption, int versionId) {

        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");
        stringBuilder.append(ExemptionBannedForTariffPlanDao.Properties.TariffPlanCode);
        stringBuilder.append(" FROM ");
        stringBuilder.append(ExemptionBannedForTariffPlanDao.TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(ExemptionBannedForTariffPlanDao.Properties.ExemptionCode).append("=").append(exemption.getExemptionExpressCode());
        stringBuilder.append(" AND ");
        if (exemption.getRegionOkatoCode() == null)
            stringBuilder.append(ExemptionBannedForTariffPlanDao.Properties.RegionOkatoCode + " is null ");
        else {
            stringBuilder.append(ExemptionBannedForTariffPlanDao.Properties.RegionOkatoCode).append("=").append("?");
            selectionArgsList.add(String.valueOf(exemption.getRegionOkatoCode()));
        }
        stringBuilder.append(" AND ");
        stringBuilder.append(checkVersion(ExemptionBannedForTariffPlanDao.TABLE_NAME, versionId));

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Cursor cursor = null;
        List<Integer> bannedTariffPlanCodes = new ArrayList<>();
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgs);

            while (cursor.moveToNext()) {
                bannedTariffPlanCodes.add(cursor.getInt(0));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return bannedTariffPlanCodes;
    }
}
