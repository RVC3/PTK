package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.FeeType;
import ru.ppr.nsi.entity.ProcessingFee;

/**
 * DAO для таблицы НСИ <i>ProcessingFees</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ProcessingFeeDao extends BaseEntityDao<ProcessingFee, Integer> {

    public static final String TABLE_NAME = "ProcessingFees";

    public static class Properties {
        public static final String CarrierCode = "CarrierCode";
        public static final String RegionCode = "RegionCode";
        public static final String TrainCategoryCode = "TrainCategoryCode";
        public static final String Tariff = "Tariff";
        public static final String Tax = "Tax";
        public static final String FeeType = "FeeType";
    }

    public ProcessingFeeDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ProcessingFee fromCursor(Cursor cursor) {
        ProcessingFee tax = new ProcessingFee();

        int index = cursor.getColumnIndex(ProcessingFeeDao.Properties.TrainCategoryCode);
        if (index != -1)
            tax.setTrainCategoryCode(cursor.getInt(index));

        index = cursor.getColumnIndex(ProcessingFeeDao.Properties.Tariff);
        if (index != -1)
            tax.setTariff(new BigDecimal(cursor.getString(index)));

        index = cursor.getColumnIndex(ProcessingFeeDao.Properties.Tax);
        if (index != -1)
            tax.setTax(new BigDecimal(cursor.getString(index)));

        index = cursor.getColumnIndex(ProcessingFeeDao.Properties.FeeType);
        if (index != -1)
            tax.setFeeType(FeeType.valueOf(cursor.getInt(index)));

        return tax;
    }

    /**
     * Возвращает сбор для категории поезда
     */
    public ProcessingFee getProcessingFee(int trainCategoryCode, FeeType feeType, int nsiVersion) {
        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");
        stringBuilder.append("*");
        stringBuilder.append(" FROM ");
        stringBuilder.append(ProcessingFeeDao.TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(ProcessingFeeDao.Properties.TrainCategoryCode).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(trainCategoryCode));
        stringBuilder.append(" AND ");
        stringBuilder.append(ProcessingFeeDao.Properties.FeeType).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(feeType.getCode()));
        stringBuilder.append(" AND ");
        stringBuilder.append(checkVersion(ProcessingFeeDao.TABLE_NAME, nsiVersion));

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Cursor cursor = null;
        ProcessingFee processingFee;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgs);

            processingFee = null;
            if (cursor.moveToFirst()) {
                processingFee = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return processingFee;
    }
}
