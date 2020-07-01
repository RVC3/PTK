package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Carrier;

/**
 * DAO для таблицы НСИ <i>Carriers</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class CarrierDao extends BaseEntityDao<Carrier, String> {

    public static final String TABLE_NAME = "Carriers";

    public static class Properties {
        public static final String Name = "Name";
        public static final String ShortName = "ShortName";
        public static final String INN = "INN";
    }

    public CarrierDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Carrier fromCursor(Cursor cursor) {
        Carrier carrier = new Carrier();

        addBaseNSIData(carrier, String.class, cursor);

        int index = cursor.getColumnIndex(CarrierDao.Properties.Name);
        if (index != -1)
            carrier.setName(cursor.getString(index));

        index = cursor.getColumnIndex(CarrierDao.Properties.ShortName);
        if (index != -1)
            carrier.setShortName(cursor.getString(index));

        index = cursor.getColumnIndex(CarrierDao.Properties.INN);
        if (index != -1)
            carrier.setInn(cursor.getString(index));

        return carrier;
    }
}
