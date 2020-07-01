package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.math.BigDecimal;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.ValidityPeriod;

/**
 * DAO для таблицы НСИ <i>TicketTypes</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTypeDao extends BaseEntityDao<TicketType, Integer> {

    public static final String TABLE_NAME = "TicketTypes";

    public static class Properties {
        public static final String TicketCategoryCode = "TicketCategoryCode";
        public static final String ValidityPeriodCode = "ValidityPeriodCode";
        public static final String DurationOfValidity = "DurationOfValidity";
        public static final String TripsNumber = "TripsNumber";
        public static final String ShortName = "ShortName";
        public static final String Tax = "Tax";
        public static final String ExpressTicketTypeCode = "ExpressTicketTypeCode";
        public static final String Code = "Code";
        public static final String IsJointSale = "IsJointSale";
        public static final String IsRequireBindedFio = "IsRequireBindedFio";
        public static final String IsRequireSourceTicket = "IsRequireSourceTicket";
    }

    public TicketTypeDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    public TicketType load(Integer code, int versionId) {
        TicketType loaded = super.load(code, versionId);

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
    public TicketType fromCursor(Cursor cursor) {
        TicketType ticketType = new TicketType();

        ticketType.setTicketCategoryCode(cursor.getInt(cursor.getColumnIndex(TicketTypeDao.Properties.TicketCategoryCode)));

        int validityPeriodCodeIndex = cursor.getColumnIndex(Properties.ValidityPeriodCode);
        if (!cursor.isNull(validityPeriodCodeIndex)) {
            ticketType.setValidityPeriod(ValidityPeriod.getByCode(cursor.getInt(validityPeriodCodeIndex)));
        }

        int durationOfValidityIndex = cursor.getColumnIndex(Properties.DurationOfValidity);
        if (!cursor.isNull(durationOfValidityIndex)) {
            ticketType.setDurationOfValidity(cursor.getInt(durationOfValidityIndex));
        }

        int tripsNumberIndex = cursor.getColumnIndex(Properties.TripsNumber);
        if (!cursor.isNull(tripsNumberIndex)) {
            ticketType.setTripsNumber(cursor.getInt(tripsNumberIndex));
        }

        ticketType.setShortName(cursor.getString(cursor.getColumnIndex(Properties.ShortName)));

        int taxIndex = cursor.getColumnIndex(Properties.Tax);
        if (!cursor.isNull(taxIndex)) {
            ticketType.setTax(new BigDecimal(cursor.getString(taxIndex)));
        }

        ticketType.setExpressTicketTypeCode(cursor.getString(cursor.getColumnIndex(Properties.ExpressTicketTypeCode)));
        ticketType.setCode(cursor.getInt(cursor.getColumnIndex(Properties.Code)));
        ticketType.setJointSale(cursor.getInt(cursor.getColumnIndex(Properties.IsJointSale)) > 0);
        ticketType.setRequireBindedFio(cursor.getInt(cursor.getColumnIndex(Properties.IsRequireBindedFio)) > 0);
        ticketType.setRequireSourceTicket(cursor.getInt(cursor.getColumnIndex(Properties.IsRequireSourceTicket)) > 0);

        return ticketType;
    }

}
