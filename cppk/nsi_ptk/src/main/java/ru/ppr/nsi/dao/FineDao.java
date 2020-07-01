package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.math.BigDecimal;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Fine;

/**
 * DAO для таблицы НСИ <i>Fines</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class FineDao extends BaseEntityDao<Fine, Long> {

    public static final String TABLE_NAME = "Fines";

    public static class Properties {
        public static final String Code = "Code";
        public static final String Name = "Name";
        public static final String NdsPercent = "NdsPercent";
        public static final String ShortName = "ShortName";
        public static final String Value = "Value";
        public static final String RegionCode = "RegionCode";
    }

    public FineDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Fine fromCursor(Cursor cursor) {
        Fine fine = new Fine();

        fine.setCode(cursor.getLong(cursor.getColumnIndex(Properties.Code)));
        fine.setName(cursor.getString(cursor.getColumnIndex(Properties.Name)));
        fine.setNdsPercent(cursor.getInt(cursor.getColumnIndex(Properties.NdsPercent)));
        fine.setShortName(cursor.getString(cursor.getColumnIndex(Properties.ShortName)));
        fine.setRegionCode(cursor.getString(cursor.getColumnIndex(Properties.RegionCode)));
        fine.setValue(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.Value))));

        return fine;
    }

}
