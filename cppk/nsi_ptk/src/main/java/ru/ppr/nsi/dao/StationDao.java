package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Station;

/**
 * DAO для таблицы НСИ <i>Stations</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class StationDao extends BaseEntityDao<Station, Long> {

    private static String TAG = Logger.makeLogTag(StationDao.class);

    public static final String TABLE_NAME = "Stations";

    public static class Properties {
        public static final String EsrCode = "EsrCode";
        public static final String Name = "Name";
        public static final String ShortName = "ShortName";
        public static final String RegionCode = "RegionCode";
        public static final String IsTransitStation = "IsTransitStation";
        public static final String CanSaleTickets = "CanSaleTickets";
        public static final String Code = "Code";
        public static final String IsForTransfer = "IsForTransfer";
    }

    public StationDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    public Station load(Long code, int versionId) {
        Station loaded = super.load(code, versionId);

        if (loaded != null) {
            loaded.setVersionId(versionId);
        }

        return loaded;
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Station fromCursor(Cursor cursor) {
        Station out = new Station();
        int index = cursor.getColumnIndex(StationDao.Properties.EsrCode);
        if (index != -1)
            out.setErcCode(cursor.getInt(index));
        index = cursor.getColumnIndex(StationDao.Properties.Name);
        if (index != -1)
            out.setName(cursor.getString(index).trim());
        index = cursor.getColumnIndex(StationDao.Properties.ShortName);
        if (index != -1)
            out.setShortName(cursor.getString(index).trim());
        index = cursor.getColumnIndex(StationDao.Properties.RegionCode);
        if (index != -1)
            out.setRegionCode(cursor.getInt(index));
        index = cursor.getColumnIndex(StationDao.Properties.IsTransitStation);
        if (index != -1)
            out.setTransitStation(cursor.getInt(index) > 0);
        index = cursor.getColumnIndex(StationDao.Properties.CanSaleTickets);
        if (index != -1)
            out.setCanSaleTickets(cursor.getInt(index) > 0);
        index = cursor.getColumnIndex(StationDao.Properties.Code);
        if (index != -1)
            out.setCode(cursor.getInt(index));
        index = cursor.getColumnIndex(StationDao.Properties.IsForTransfer);
        if (index != -1)
            out.setForTransfer(cursor.getInt(index) != 0);
        // out.versionId =
        // cursor.getInt(cursor.getColumnIndex(ConstantsDB.versionId));
        // out.deleteInVersion =
        // cursor.getInt(cursor.getColumnIndex(ConstantsDB.deleteInVersion));
        return out;
    }

}
