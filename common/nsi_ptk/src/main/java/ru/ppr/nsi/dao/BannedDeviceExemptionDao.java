package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.nsi.NsiDaoSession;

/**
 * DAO для таблицы НСИ <i>BannedDeviceExemptions</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class BannedDeviceExemptionDao extends BaseEntityDao<String, Integer> {

    public static final String TABLE_NAME = "BannedDeviceExemptions";

    public static class Properties {
        public static final String ExemptionCode = "ExemptionCode";
        public static final String DeviceTypeCode = "DeviceTypeCode";
        public static final String RegionCode = "RegionCode";
        public static final String RegionOkatoCode = "RegionOkatoCode";
        public static final String TrainCategory = "TrainCategory";
    }

    public BannedDeviceExemptionDao(NsiDaoSession nsiDaoSession) {
        super(nsiDaoSession);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String fromCursor(Cursor cursor, int requiredVersionId) {

        return null;
    }
}
