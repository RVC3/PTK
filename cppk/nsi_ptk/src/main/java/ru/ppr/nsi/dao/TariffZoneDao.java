package ru.ppr.nsi.dao;

import android.database.Cursor;
import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TariffZone;
/**
 * DAO для таблицы НСИ <i>TariffZones</i>.
 */
public class TariffZoneDao extends BaseEntityDao<TariffZone, Long> {

    public static final String TABLE_NAME = "TariffZones";

    public static class Properties {
        public static final String Name = "Name";
        public static final String DistanceFrom = "DistanceFrom";
        public static final String DistanceTo = "DistanceTo";
        public static final String IsSpecialZone = "IsSpecialZone";
        public static final String TicketIssuedOutsideZone = "TicketIssuedOutsideZone";
        public static final String TariffPlanCode = "TariffPlanCode";
        public static final String Code = "Code";
    }

    public TariffZoneDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public TariffZone fromCursor(Cursor cursor) {

        TariffZone tariffZone = new TariffZone();

        tariffZone.setName(cursor.getString(cursor.getColumnIndex(Properties.Name)));
        int index = cursor.getColumnIndex(Properties.DistanceFrom);
        tariffZone.setDistanceFrom(cursor.isNull(index) ? null : cursor.getInt(index));
        index = cursor.getColumnIndex(Properties.DistanceTo);
        tariffZone.setDistanceTo(cursor.isNull(index) ? null : cursor.getInt(index));
        tariffZone.setSpecialZone(cursor.getInt(cursor.getColumnIndex(Properties.IsSpecialZone)) != 0);
        tariffZone.setTicketIssuedOutsideZone(cursor.getInt(cursor.getColumnIndex(Properties.TicketIssuedOutsideZone)) != 0);
        index = cursor.getColumnIndex(Properties.TariffPlanCode);
        tariffZone.setTariffPlanCode(cursor.isNull(index) ? null : cursor.getLong(index));

        //добавим базовые поля
        addBaseNSIData(tariffZone, Long.class, cursor);

        return tariffZone;
    }
}
