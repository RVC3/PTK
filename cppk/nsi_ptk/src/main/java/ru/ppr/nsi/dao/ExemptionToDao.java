package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.ExemptionsTo;

/**
 * DAO для таблицы НСИ <i>ExemptionsTo</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ExemptionToDao extends BaseEntityDao<ExemptionsTo, Integer> {

    public static final String TABLE_NAME = "ExemptionsTo";

    public static class Properties {
        public static final String ExemptionCode = "ExemptionCode";
        public static final String CarrierCode = "CarrierCode";
        public static final String ExpressTicketTypeCode = "ExpressTicketTypeCode";
        public static final String TicketTypeCode = "TicketTypeCode";
        public static final String TrainCategoryCode = "TrainCategoryCode";
        public static final String CarClassCode = "CarClassCode";
        public static final String TicketProcessingDelay = "TicketProcessingDelay";
        public static final String VersionId = "VersionId";
        public static final String DeleteInVersionId = "DeleteInVersionId";
    }

    public ExemptionToDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ExemptionsTo fromCursor(Cursor cursor) {

        int exemptionCode = cursor.getInt(cursor.getColumnIndex(ExemptionToDao.Properties.ExemptionCode));
        int trainCategoryCode = cursor.getInt(cursor.getColumnIndex(ExemptionToDao.Properties.TrainCategoryCode));
        int carClassCode = cursor.getInt(cursor.getColumnIndex(ExemptionToDao.Properties.CarClassCode));

        String carrierCode = cursor.getString(cursor.getColumnIndex(ExemptionToDao.Properties.CarrierCode));
        String expressTicketCode = cursor.getString(cursor.getColumnIndex(ExemptionToDao.Properties.ExpressTicketTypeCode));

        int ticketTypeIndex = cursor.getColumnIndex(ExemptionToDao.Properties.TicketTypeCode);
        Integer ticketTypeCode = null;
        if (!cursor.isNull(ticketTypeIndex)) {
            ticketTypeCode = cursor.getInt(ticketTypeIndex);
        }

        int ticketProcessingDelayIndex = cursor.getColumnIndex(ExemptionToDao.Properties.TicketProcessingDelay);

        Integer ticketProcessingDelay = null;
        if (!cursor.isNull(ticketProcessingDelayIndex)) {
            ticketProcessingDelay = cursor.getInt(ticketProcessingDelayIndex);
        }

        return new ExemptionsTo(exemptionCode, carrierCode, expressTicketCode, ticketTypeCode,
                trainCategoryCode, carClassCode, ticketProcessingDelay);
    }

    /**
     * Ищет запись в таблице ExemptionsTo по указанным парамтерам
     *
     * @param exemptionCode
     * @param ticketTypeCode
     * @param trainCategoryCode
     * @param versionId
     * @return
     */
    public ExemptionsTo getExemptionsTo(int exemptionCode,
                                        int ticketTypeCode, int trainCategoryCode,
                                        int versionId) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ExemptionToDao.Properties.ExemptionCode).append(" = ").append(exemptionCode)
                .append(" AND ")
                .append(ExemptionToDao.Properties.TicketTypeCode).append(" = ").append(ticketTypeCode)
                .append(" AND ")
                .append(ExemptionToDao.Properties.TrainCategoryCode).append(" = ").append(trainCategoryCode)
                .append(" AND ").append(BaseEntityDao.Properties.VersionId).append(" <= ")
                .append(versionId).append(" AND ").append("(")
                .append(BaseEntityDao.Properties.DeleteInVersionId).append(" > ")
                .append(versionId).append(" OR ")
                .append(BaseEntityDao.Properties.DeleteInVersionId).append(" is NULL)");

        Cursor cursor = null;
        ExemptionsTo out = null;
        try {
            cursor = db().query(ExemptionToDao.TABLE_NAME, null, stringBuilder.toString(), null,
                    null, null, null);
            if (cursor.moveToFirst()) {
                out = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return out;
    }

    /**
     * Проверяет возможность применения указанной льготы для типа билета и категории поезда
     *
     * @param exemptionCode
     * @param ticketTypeCode
     * @param trainCategoryCode
     * @param versionId
     * @return
     */
    public boolean checkExemptionToTicketTypeAndTrainCategory(int exemptionCode, int ticketTypeCode, int trainCategoryCode, int versionId) {

        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");
        stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.Code);
        stringBuilder.append(" FROM ");
        stringBuilder.append(ExemptionDao.TABLE_NAME);
        stringBuilder.append(" JOIN ");
        stringBuilder.append(ExemptionToDao.TABLE_NAME);
        stringBuilder.append(" ON ");
        stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.Code);
        stringBuilder.append(" = ");
        stringBuilder.append(ExemptionToDao.TABLE_NAME).append(".").append(ExemptionToDao.Properties.ExemptionCode);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(checkVersion(ExemptionDao.TABLE_NAME, versionId));
        stringBuilder.append(" AND ");
        stringBuilder.append(checkVersion(ExemptionToDao.TABLE_NAME, versionId));
        stringBuilder.append(" AND ");
        stringBuilder.append(ExemptionDao.Properties.Code).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(exemptionCode));
        stringBuilder.append(" AND ");
        stringBuilder.append(ExemptionToDao.Properties.TrainCategoryCode).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(trainCategoryCode));
        stringBuilder.append(" AND ");
        stringBuilder.append(ExemptionToDao.Properties.TicketTypeCode).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(ticketTypeCode));

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Cursor cursor = null;
        boolean result;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgs);
            result = cursor.moveToFirst() && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }
}
