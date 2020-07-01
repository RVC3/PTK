package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.ServiceFee;

/**
 * DAO для таблицы НСИ <i>ServiceFees</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceFeeDao extends BaseEntityDao<ServiceFee, Long> {

    public static final String TABLE_NAME = "ServiceFees";

    public static class Properties {
        public static final String Name = "Name";
        public static final String SequenceNumber = "SequenceNumber";
        public static final String Tariff = "Tariff";
        public static final String Tax = "Tax";
        public static final String AdvanceSale = "AdvanceSale";
        public static final String ValidityPeriod = "ValidityPeriod";
        public static final String IsJointSale = "IsJointSale";
        public static final String Code = "Code";
    }

    public ServiceFeeDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ServiceFee fromCursor(Cursor cursor) {
        ServiceFee serviceFee = new ServiceFee();

        addBaseNSIData(serviceFee, Long.class, cursor);

        int index = cursor.getColumnIndex(ServiceFeeDao.Properties.Name);
        if (index != -1)
            serviceFee.setName(cursor.getString(index));

        index = cursor.getColumnIndex(Properties.ValidityPeriod);
        if (index != -1)
            serviceFee.setValidityPeriod(cursor.isNull(index) ? null : cursor.getInt(index));

        return serviceFee;
    }
}
