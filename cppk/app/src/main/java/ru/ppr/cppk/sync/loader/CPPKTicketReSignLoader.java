package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.CPPKTicketReSignDao;
import ru.ppr.cppk.sync.kpp.CPPKTicketReSign;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class CPPKTicketReSignLoader extends BaseLoader {

    private final EventLoader eventLoader;

    public CPPKTicketReSignLoader(LocalDaoSession localDaoSession,
                                  NsiDaoSession nsiDaoSession,
                                  EventLoader eventLoader) {
        super(localDaoSession, nsiDaoSession);
        this.eventLoader = eventLoader;
    }

    public static class Columns {
        static final Column TICKET_NUMBER = new Column(0, CPPKTicketReSignDao.Properties.TicketNumber);
        static final Column SALE_DATE_TIME = new Column(1, CPPKTicketReSignDao.Properties.SaleDateTime);
        static final Column TICKET_DEVICE_ID = new Column(2, CPPKTicketReSignDao.Properties.TicketDeviceId);
        static final Column EDS_KEY_NUMBER = new Column(3, CPPKTicketReSignDao.Properties.EDSKeyNumber);
        static final Column RE_SIGN_DATE_TIME = new Column(4, CPPKTicketReSignDao.Properties.ReSignDateTime);

        public static Column[] all = new Column[]{
                TICKET_NUMBER,
                SALE_DATE_TIME,
                TICKET_DEVICE_ID,
                EDS_KEY_NUMBER,
                RE_SIGN_DATE_TIME
        };
    }

    public CPPKTicketReSign load(Cursor cursor, Offset offset) {

        CPPKTicketReSign cppkTicketReSign = new CPPKTicketReSign();

        cppkTicketReSign.ticketNumber = cursor.getInt(offset.value + Columns.TICKET_NUMBER.index);
        cppkTicketReSign.saleDateTime = new Date(cursor.getLong(offset.value + Columns.SALE_DATE_TIME.index));
        cppkTicketReSign.ticketDeviceId = cursor.getString(offset.value + Columns.TICKET_DEVICE_ID.index);
        cppkTicketReSign.edsKeyNumber = cursor.getLong(offset.value + Columns.EDS_KEY_NUMBER.index);
        cppkTicketReSign.reSignDateTime = new Date(cursor.getLong(offset.value + Columns.RE_SIGN_DATE_TIME.index));

        offset.value += Columns.all.length;

        eventLoader.fill(cppkTicketReSign, cursor, offset);

        return cppkTicketReSign;
    }

}