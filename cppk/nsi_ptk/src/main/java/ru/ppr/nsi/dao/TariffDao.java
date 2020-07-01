package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.math.BigDecimal;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Tariff;

/**
 * DAO для таблицы НСИ <i>Tariffs</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TariffDao extends BaseEntityDao<Tariff, Long> {

    public static final String TABLE_NAME = "Tariffs";

    public static class Properties {
        public static final String TariffPlanCode = "TariffPlanCode";
        public static final String RouteCode = "RouteCode";
        public static final String StationDestinationCode = "DestinationStationCode";
        public static final String StationDepartureCode = "DepartureStationCode";
        public static final String TicketTypeCode = "TicketTypeCode";
        public static final String Code = "Code";
        public static final String Price = "Price";
        public static final String DepartureTariffZoneCode = "DepartureTariffZoneCode";
        public static final String DestinationTariffZoneCode = "DestinationTariffZoneCode";
    }

    public TariffDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    public Tariff load(Long code, int versionId) {
        Tariff loaded = super.load(code, versionId);

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
    public Tariff fromCursor(Cursor cursor) {
        Tariff out = new Tariff();

        int index = cursor.getColumnIndex(TariffDao.Properties.TariffPlanCode);
        if (index != -1)
            out.setTariffPlanCode(cursor.getInt(index));

        index = cursor.getColumnIndex(TariffDao.Properties.RouteCode);
        if (index != -1) {
            out.setRouteCode(cursor.getString(index));
        }

        index = cursor.getColumnIndex(TariffDao.Properties.StationDestinationCode);
        if (index != -1)
            out.setStationDestinationCode(cursor.getInt(index));

        index = cursor.getColumnIndex(TariffDao.Properties.StationDepartureCode);
        if (index != -1)
            out.setStationDepartureCode(cursor.getInt(index));

        index = cursor.getColumnIndex(TariffDao.Properties.TicketTypeCode);
        if (index != -1)
            out.setTicketTypeCode(cursor.getInt(index));

        index = cursor.getColumnIndex(TariffDao.Properties.Code);
        if (index != -1)
            out.setCode(cursor.getInt(index));

        index = cursor.getColumnIndex(TariffDao.Properties.Price);
        if (index != -1)
            out.setPricePd(new BigDecimal(cursor.getString(index)));

        index = cursor.getColumnIndex(BaseEntityDao.Properties.DeleteInVersionId);
        if (index != -1)
            out.setDeleteInVersion(cursor.isNull(index) ? null : cursor.getInt(index));

        index = cursor.getColumnIndex(Properties.DepartureTariffZoneCode);
        out.setDepartureTariffZoneCode(cursor.isNull(index) ? null : cursor.getLong(index));

        index = cursor.getColumnIndex(Properties.DestinationTariffZoneCode);
        out.setDestinationTariffZoneCode(cursor.isNull(index) ? null : cursor.getLong(index));

        return out;
    }

}
