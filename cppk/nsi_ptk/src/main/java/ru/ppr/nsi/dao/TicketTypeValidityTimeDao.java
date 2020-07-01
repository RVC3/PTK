package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TicketTypesValidityTimes;

/**
 * DAO для таблицы НСИ <i>TicketTypesValidityTimes</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTypeValidityTimeDao extends BaseEntityDao<TicketTypesValidityTimes, Integer> {

    private static final String TAG = Logger.makeLogTag(TicketTypeValidityTimeDao.class);

    public static final String TABLE_NAME = "TicketTypesValidityTimes";

    public static class Properties {
        public static final String TicketTypeCode = "TicketTypeCode";
        public static final String ValidFrom = "ValidFrom";
        public static final String ValidTo = "ValidTo";
    }

    private final SimpleDateFormat simpleDateFormat;

    public TicketTypeValidityTimeDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public TicketTypesValidityTimes fromCursor(Cursor cursor) {

        int ticketCode = cursor.getInt(cursor.getColumnIndex(TicketTypeValidityTimeDao.Properties.TicketTypeCode));
        String fromTimeStr = cursor.getString(cursor.getColumnIndex(TicketTypeValidityTimeDao.Properties.ValidFrom));
        String toTimeStr = cursor.getString(cursor.getColumnIndex(TicketTypeValidityTimeDao.Properties.ValidTo));

        Date fromTime = convertToTime(fromTimeStr);
        Date toTome = convertToTime(toTimeStr);

        return new TicketTypesValidityTimes(ticketCode,
                (int) (fromTime.getTime() / 1000),
                (int) (toTome.getTime() / 1000));
    }

    private Date convertToTime(String time) {
        Date date;
        try {
            date = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            Logger.error(TAG, e);
            date = new Date(0);
        }
        return date;
    }
}
