package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import java.math.BigDecimal;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.CppkTicketSaleDao;
import ru.ppr.cppk.db.local.TicketSaleReturnEventBaseDao;
import ru.ppr.cppk.sync.kpp.CPPKTicketReturn;
import ru.ppr.cppk.sync.kpp.CPPKTicketSale;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.TicketSaleReturnEventBaseLoader;
import ru.ppr.cppk.sync.loader.model.CheckLoader;
import ru.ppr.cppk.sync.loader.model.PreTicketLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class TicketSaleLoader extends BaseLoader {

    private final TicketSaleReturnEventBaseLoader ticketSaleReturnEventBaseLoader;
    private final CheckLoader checkLoader;
    private final PreTicketLoader preTicketLoader;

    public TicketSaleLoader(LocalDaoSession localDaoSession,
                            NsiDaoSession nsiDaoSession,
                            TicketSaleReturnEventBaseLoader ticketSaleReturnEventBaseLoader,
                            CheckLoader checkLoader,
                            PreTicketLoader preTicketLoader) {
        super(localDaoSession, nsiDaoSession);
        this.ticketSaleReturnEventBaseLoader = ticketSaleReturnEventBaseLoader;
        this.checkLoader = checkLoader;
        this.preTicketLoader = preTicketLoader;
    }

    public static class Columns {
        static final Column TRIPS_COUNT = new Column(0, CppkTicketSaleDao.Properties.TripsCount);
        static final Column STORAGE_TYPE_CODE = new Column(1, CppkTicketSaleDao.Properties.StorageTypeCode);
        static final Column EDS_KEY_NUMBER = new Column(2, CppkTicketSaleDao.Properties.EDSKeyNumber);
        static final Column COUPON_READ_EVENT_ID = new Column(3, CppkTicketSaleDao.Properties.CouponReadEventId);
        static final Column CONNECTION_TYPE = new Column(4, CppkTicketSaleDao.Properties.ConnectionType);

        public static Column[] all = new Column[]{
                TRIPS_COUNT,
                STORAGE_TYPE_CODE,
                EDS_KEY_NUMBER,
                COUPON_READ_EVENT_ID,
                CONNECTION_TYPE
        };
    }

    public static class TicketSaleReturnEventBaseColumns {
        static final Column IS_TICKET_WRITTEN = new Column(0, TicketSaleReturnEventBaseDao.Properties.IsTicketWritten);

        public static Column[] all = new Column[]{
                IS_TICKET_WRITTEN
        };
    }

    /**
     * Поля сущности {@link CPPKTicketSale}, предназначенные для {@link ru.ppr.cppk.sync.kpp.CPPKTicketReturn}
     */
    public static class CPPKTicketReturnColumns {
        static final Column FULL_TICKET_PRICE = new Column(0, CppkTicketSaleDao.Properties.FullTicketPrice);

        public static Column[] all = new Column[]{
                FULL_TICKET_PRICE
        };
    }

    public CPPKTicketSale load(Cursor cursor, Offset offset) {

        CPPKTicketSale cppkTicketSale = new CPPKTicketSale();

        int index = offset.value + Columns.TRIPS_COUNT.index;
        cppkTicketSale.TripsCount = cursor.isNull(index) ? null : cursor.getInt(index);
        cppkTicketSale.StorageTypeCode = cursor.getInt(offset.value + Columns.STORAGE_TYPE_CODE.index);
        cppkTicketSale.EDSKeyNumber = cursor.getLong(offset.value + Columns.EDS_KEY_NUMBER.index);
        int connectionTypeIndex = offset.value + Columns.CONNECTION_TYPE.index;
        cppkTicketSale.ConnectionType = cursor.isNull(connectionTypeIndex) ? null : cursor.getInt(connectionTypeIndex);
        long preTicketId = cursor.getLong(offset.value + Columns.COUPON_READ_EVENT_ID.index);

        offset.value += Columns.all.length;

        fillFromTicketSaleReturnEventBase(cppkTicketSale, cursor, offset);

        //Заполним TicketNumber
        checkLoader.fillTicketEventBaseFields(cppkTicketSale, cursor, offset);

        ticketSaleReturnEventBaseLoader.fill(cppkTicketSale, false, 0, cursor, offset);

        //заполним мелкие модельки отдельными запросами
        cppkTicketSale.PreTicket = (preTicketId > 0) ? preTicketLoader.load(preTicketId, cppkTicketSale.VersionId) : null;

        return cppkTicketSale;
    }

    /**
     * Заполнить поля физически находящиеся в сущности TicketSaleReturnEventBase
     *
     * @param cppkTicketSale
     * @param cursor
     * @param offset
     */
    private void fillFromTicketSaleReturnEventBase(CPPKTicketSale cppkTicketSale, Cursor cursor, Offset offset) {
        int index = offset.value + TicketSaleReturnEventBaseColumns.IS_TICKET_WRITTEN.index;
        cppkTicketSale.IsTicketWritten = cursor.isNull(index) ? null : cursor.getInt(index) == 1;
        offset.value += TicketSaleReturnEventBaseColumns.all.length;
    }

    /**
     * Заполнить поля в сущности {@link ru.ppr.cppk.sync.kpp.CPPKTicketReturn}
     *
     * @param cppkTicketReturn
     * @param cursor
     * @param offset
     */
    public void fillCppkReturnFields(CPPKTicketReturn cppkTicketReturn, Cursor cursor, Offset offset) {
        cppkTicketReturn.fullTicketPrice = new BigDecimal(cursor.getString(offset.value + CPPKTicketReturnColumns.FULL_TICKET_PRICE.index));
        offset.value += CPPKTicketReturnColumns.all.length;
    }

}
