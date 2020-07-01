package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Region;

/**
 * DAO для таблицы НСИ <i>Regions</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class RegionDao extends BaseEntityDao<Region, Integer> {

    public static final String TABLE_NAME = "Regions";

    public static class Properties {
        public static final String Code = "Code";
        public static final String RegionOkatoCode = "RegionOkatoCode";
        public static final String Name = "Name";
    }

    public RegionDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Region fromCursor(Cursor cursor) {
        Region region = new Region();

        addBaseNSIData(region, Integer.class, cursor);

        int index = cursor.getColumnIndex(RegionDao.Properties.Name);
        if (index != -1)
            region.setName(cursor.getString(index));

        index = cursor.getColumnIndex(RegionDao.Properties.RegionOkatoCode);
        if (index != -1)
            region.setRegionOkatoCode(cursor.getString(index));

        return region;
    }
}
