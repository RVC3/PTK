package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * DAO для таблицы НСИ <i>ExemptionBannedForTicketStorageTypes</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ExemptionBannedForTicketStorageTypeDao extends BaseEntityDao<String, Integer> {

    public static final String TABLE_NAME = "ExemptionBannedForTicketStorageTypes";

    public static class Properties {
        public static final String ExemptionCode = "ExemptionCode";
        public static final String TicketStorageTypeCode = "TicketStorageTypeCode";
        public static final String RegionOkatoCode = "RegionOkatoCode";
    }

    public ExemptionBannedForTicketStorageTypeDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
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
     * Выполняет проверку "Разрешение на оформление определенного вида ПД по указанному коду льготы"
     *
     * @param ticketStorageType    Тип носителя
     * @param regionOkatoCode
     * @param exemptionExpressCode
     * @return
     */
    public boolean isExemptionBannedForTicketStorageType(
            TicketStorageType ticketStorageType,
            String regionOkatoCode,
            int exemptionExpressCode,
            int versionId) {

        StringBuilder stringBuilder = new StringBuilder();

        List<String> selectionArgsList = new ArrayList<>();

        stringBuilder.append("SELECT ");
        stringBuilder.append("*");
        stringBuilder.append(" FROM ");
        stringBuilder.append(TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(Properties.ExemptionCode).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(exemptionExpressCode));
        stringBuilder.append(" AND ");
        stringBuilder.append(Properties.TicketStorageTypeCode).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(ticketStorageType.getDBCode()));
        stringBuilder.append(" AND ");
        stringBuilder.append(Properties.RegionOkatoCode);
        if (regionOkatoCode == null)
            stringBuilder.append(" is null ");
        else {
            stringBuilder.append(" = ").append("?");
            selectionArgsList.add(String.valueOf(regionOkatoCode));
        }
        stringBuilder.append(" AND ");
        stringBuilder.append(checkVersion(TABLE_NAME, versionId));

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Cursor cursor = null;
        boolean result;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgs);
            result = cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }
}
