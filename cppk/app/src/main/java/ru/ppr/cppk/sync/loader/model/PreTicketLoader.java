package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CouponReadEventDao;
import ru.ppr.cppk.sync.kpp.model.PreTicket;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class PreTicketLoader extends BaseLoader {

    private final String loadQuery;

    private final StationLoader stationLoader;

    public PreTicketLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession, StationLoader stationLoader) {
        super(localDaoSession, nsiDaoSession);
        this.stationLoader = stationLoader;
        this.loadQuery = buildLoadQuery();
    }

    public static class Columns {
        static final Column PRE_TICKET_NUMBER = new Column(0, CouponReadEventDao.Properties.PreTicketNumber);
        static final Column PRINT_DATE_TIME = new Column(1, CouponReadEventDao.Properties.PrintDateTime);
        static final Column DEVICE_ID = new Column(2, CouponReadEventDao.Properties.DeviceId);
        static final Column STATION_CODE = new Column(3, CouponReadEventDao.Properties.StationCode);

        public static Column[] all = new Column[]{
                PRE_TICKET_NUMBER,
                PRINT_DATE_TIME,
                DEVICE_ID,
                STATION_CODE
        };
    }

    public PreTicket load(Cursor cursor, Offset offset, int nsiVersion) {
        PreTicket preTicket = new PreTicket();
        preTicket.preTicketNumber = cursor.getLong(offset.value + Columns.PRE_TICKET_NUMBER.index);
        preTicket.printDateTime = new Date(cursor.getLong(offset.value + Columns.PRINT_DATE_TIME.index));
        preTicket.deviceId = cursor.getString(offset.value + Columns.DEVICE_ID.index);
        long stationCode = cursor.getLong(offset.value + Columns.STATION_CODE.index);
        preTicket.station = stationCode > 0 ? stationLoader.loadStation(stationCode, nsiVersion) : null;
        offset.value += Columns.all.length;
        return preTicket;
    }

    public PreTicket load(long preTicketId, int nsiVersion) {

        String[] selectionArgs = new String[]{String.valueOf(preTicketId)};

        PreTicket preTicket = null;
        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                preTicket = load(cursor, new Offset(), nsiVersion);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return preTicket;
    }

    private String buildLoadQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(createColumnsForSelect(CouponReadEventDao.TABLE_NAME, Columns.all));
        sb.append(" FROM ");
        sb.append(CouponReadEventDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}