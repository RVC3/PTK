package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.CppkTicketControlsDao;
import ru.ppr.cppk.sync.kpp.CPPKTicketControl;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.TicketEventBaseLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
public class TicketControlLoader extends BaseLoader {

    private final TicketEventBaseLoader ticketEventBaseLoader;

    public TicketControlLoader(LocalDaoSession localDaoSession,
                               NsiDaoSession nsiDaoSession,
                               TicketEventBaseLoader ticketEventBaseLoader) {
        super(localDaoSession, nsiDaoSession);
        this.ticketEventBaseLoader = ticketEventBaseLoader;
    }

    public static class Columns {
        static final Column CONTROL_DATETIME = new Column(0, CppkTicketControlsDao.Properties.ControlDateTime);
        static final Column EDS_KEY_NUMBER = new Column(1, CppkTicketControlsDao.Properties.EdsKeyNumber);
        static final Column IS_REVOKED_EDS = new Column(2, CppkTicketControlsDao.Properties.IsRevokedEds);
        static final Column STOP_LIST_ID = new Column(3, CppkTicketControlsDao.Properties.StopListId);
        static final Column EXEMPTION_CODE = new Column(4, CppkTicketControlsDao.Properties.ExemptionCode);
        static final Column VALIDATION_RESULT = new Column(5, CppkTicketControlsDao.Properties.ValidationResult);
        static final Column TRIPS_SPEND = new Column(6, CppkTicketControlsDao.Properties.TripsSpend);
        static final Column SELL_TICKET_DEVICE_ID = new Column(7, CppkTicketControlsDao.Properties.SellTicketDeviceId);
        static final Column TICKET_NUMBER = new Column(8, CppkTicketControlsDao.Properties.TicketNumber);
        static final Column IS_RESTORED_TICKET = new Column(9, CppkTicketControlsDao.Properties.IsRestoredTicket);
        static final Column TRANSFER_DEPARTURE_POINT = new Column(10, CppkTicketControlsDao.Properties.TransferDeparturePoint);
        static final Column TRANSFER_DESTINATION_POINT = new Column(11, CppkTicketControlsDao.Properties.TransferDestinationPoint);
        static final Column TRIPS_7000_SPEND = new Column(12, CppkTicketControlsDao.Properties.Trips7000Spend);
        static final Column TRIPS_COUNT = new Column(13, CppkTicketControlsDao.Properties.TripsCount);
        static final Column TRIPS_7000_COUNT = new Column(14, CppkTicketControlsDao.Properties.Trips7000Count);
        static final Column TRANSFER_DEPARTURE_DATETIME = new Column(15, CppkTicketControlsDao.Properties.TransferDepartureDateTime);

        public static Column[] all = new Column[]{
                CONTROL_DATETIME,
                EDS_KEY_NUMBER,
                IS_REVOKED_EDS,
                STOP_LIST_ID,
                EXEMPTION_CODE,
                VALIDATION_RESULT,
                TRIPS_SPEND,
                SELL_TICKET_DEVICE_ID,
                TICKET_NUMBER,
                IS_RESTORED_TICKET,
                TRANSFER_DEPARTURE_POINT,
                TRANSFER_DESTINATION_POINT,
                TRIPS_7000_SPEND,
                TRIPS_COUNT,
                TRIPS_7000_COUNT,
                TRANSFER_DEPARTURE_DATETIME
        };
    }

    public CPPKTicketControl load(Cursor cursor, Offset offset) {
        CPPKTicketControl cppkTicketControl = new CPPKTicketControl();

        cppkTicketControl.ControlDateTime = new Date(cursor.getLong(offset.value + Columns.CONTROL_DATETIME.index));
        cppkTicketControl.EdsKeyNumber = cursor.getLong(offset.value + Columns.EDS_KEY_NUMBER.index);
        cppkTicketControl.IsRevokedEds = cursor.getInt(offset.value + Columns.IS_REVOKED_EDS.index) > 0;
        cppkTicketControl.StopListId = cursor.getInt(offset.value + Columns.STOP_LIST_ID.index);
        cppkTicketControl.ExemptionCode = cursor.getInt(offset.value + Columns.EXEMPTION_CODE.index);
        cppkTicketControl.ValidationResult = cursor.getInt(offset.value + Columns.VALIDATION_RESULT.index);
        cppkTicketControl.TripsSpend = cursor.getInt(offset.value + Columns.TRIPS_SPEND.index);
        cppkTicketControl.SellTicketDeviceId = cursor.getString(offset.value + Columns.SELL_TICKET_DEVICE_ID.index);
        cppkTicketControl.isRestoredTicket = cursor.getInt(offset.value + Columns.IS_RESTORED_TICKET.index) > 0;
        //заполним номер билета который в нашей базе лежит в CppkTicketControlEvent а в сущности ЦОДа на уровне TicketEventBase
        cppkTicketControl.TicketNumber = cursor.getInt(offset.value + Columns.TICKET_NUMBER.index);

        int index = offset.value + Columns.TRANSFER_DEPARTURE_POINT.index;
        cppkTicketControl.DeparturePoint = cursor.isNull(index) ? null : cursor.getLong(index);

        index = offset.value + Columns.TRANSFER_DESTINATION_POINT.index;
        cppkTicketControl.DestinationPoint = cursor.isNull(index) ? null : cursor.getLong(index);

        index = offset.value + Columns.TRIPS_7000_SPEND.index;
        cppkTicketControl.trips7000Spend = cursor.isNull(index) ? null : cursor.getInt(index);

        index = offset.value + Columns.TRIPS_COUNT.index;
        cppkTicketControl.tripsCount = cursor.isNull(index) ? null : cursor.getInt(index);

        index = offset.value + Columns.TRIPS_7000_COUNT.index;
        cppkTicketControl.trips7000Count = cursor.isNull(index) ? null : cursor.getInt(index);

        index = offset.value + Columns.TRANSFER_DEPARTURE_DATETIME.index;
        cppkTicketControl.TransferDepartureDateTime = cursor.isNull(index) ? null : new Date(cursor.getLong(index));

        offset.value += Columns.all.length;

        //заполним сущности TicketEventBase
        //http://agile.srvdev.ru/browse/CPPKPP-38484
        ticketEventBaseLoader.fill(cppkTicketControl, true, false, 0, cursor, offset);

        return cppkTicketControl;
    }
}
