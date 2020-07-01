package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.CPPKServiceSaleDao;
import ru.ppr.cppk.sync.kpp.ServiceSale;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.CashierLoader;
import ru.ppr.cppk.sync.loader.model.CheckLoader;
import ru.ppr.cppk.sync.loader.model.PriceLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class ServiceSaleLoader extends BaseLoader {

    private final CheckLoader checkLoader;
    private final WorkingShiftEventLoader workingShiftEventLoader;
    private final CashierLoader cashierLoader;
    private final PriceLoader priceLoader;
    private final EventLoader eventLoader;

    public ServiceSaleLoader(LocalDaoSession localDaoSession,
                             NsiDaoSession nsiDaoSession,
                             CheckLoader checkLoader,
                             WorkingShiftEventLoader workingShiftEventLoader,
                             CashierLoader cashierLoader,
                             PriceLoader priceLoader,
                             EventLoader eventLoader) {
        super(localDaoSession, nsiDaoSession);
        this.checkLoader = checkLoader;
        this.workingShiftEventLoader = workingShiftEventLoader;
        this.cashierLoader = cashierLoader;
        this.priceLoader = priceLoader;
        this.eventLoader = eventLoader;
    }

    public static class Columns {
        static final Column SERVICE_CODE = new Column(0, CPPKServiceSaleDao.Properties.ServiceFeeCode);
        static final Column SERVICE_NAME = new Column(1, CPPKServiceSaleDao.Properties.ServiceFeeName);
        static final Column SALE_DATE_TIME = new Column(2, CPPKServiceSaleDao.Properties.SaleDateTime);

        public static Column[] all = new Column[]{
                SERVICE_CODE,
                SERVICE_NAME,
                SALE_DATE_TIME
        };
    }

    public ServiceSale load(Cursor cursor, Offset offset) {

        ServiceSale serviceSale = new ServiceSale();

        serviceSale.paymentType = "0";  //PaymentType.INDIVIDUAL_CASH.getCode();

        serviceSale.serviceCode = cursor.getString(offset.value + Columns.SERVICE_CODE.index);
        serviceSale.serviceName = cursor.getString(offset.value + Columns.SERVICE_NAME.index);
        serviceSale.saleDateTime = new Date(TimeUnit.SECONDS.toMillis(cursor.getLong(offset.value + Columns.SALE_DATE_TIME.index)));

        offset.value += Columns.all.length;

        //Заполним ticketNumber
        checkLoader.fillServiceSaleFields(serviceSale, cursor, offset);

        //Заполним workingShiftNumber
        workingShiftEventLoader.fillServiceSaleFields(serviceSale, cursor, offset);

        //Заполним price и priceNds
        priceLoader.fillServiceSaleFields(serviceSale, cursor, offset);

        //Заполним cashier
        serviceSale.cashier = cashierLoader.load(cursor, offset);

        //заполним чек
        serviceSale.check = checkLoader.load(cursor, offset);

        eventLoader.fill(serviceSale, cursor, offset);

        return serviceSale;
    }

}