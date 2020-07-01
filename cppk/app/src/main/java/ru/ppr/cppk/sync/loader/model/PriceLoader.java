package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import java.math.BigDecimal;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.PriceDao;
import ru.ppr.cppk.sync.kpp.CPPKTicketReturn;
import ru.ppr.cppk.sync.kpp.ServiceSale;
import ru.ppr.cppk.sync.kpp.model.Price;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class PriceLoader extends BaseLoader {

    public PriceLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
    }

    public static class Columns {
        static final Column FULL = new Column(0, PriceDao.Properties.Full);
        static final Column NDS = new Column(1, PriceDao.Properties.Nds);
        static final Column PAYED = new Column(2, PriceDao.Properties.Payed);
        static final Column SUM_FOR_RETURN = new Column(3, PriceDao.Properties.SummForReturn);

        public static Column[] all = new Column[]{
                FULL,
                NDS,
                PAYED,
                SUM_FOR_RETURN
        };
    }

    /**
     * Поля сущности Price, предназначенные для {@link ru.ppr.cppk.sync.kpp.CPPKTicketReturn}
     */
    public static class CPPKTicketReturnColumns {
        static final Column PAYED = new Column(0, PriceDao.Properties.Payed);

        public static Column[] all = new Column[]{
                PAYED
        };
    }

    /**
     * Поля сущности Price, предназначенные для {@link ru.ppr.cppk.sync.kpp.ServiceSale}
     */
    public static class ServiceSaleColumns {
        static final Column FULL = new Column(0, PriceDao.Properties.Full);
        static final Column NDS = new Column(1, PriceDao.Properties.Nds);

        public static Column[] all = new Column[]{
                FULL,
                NDS
        };
    }

    /**
     * Заполнить поля в сущности {@link CPPKTicketReturn}
     *
     * @param cppkTicketReturn
     * @param cursor
     * @param offset
     */
    public void fillCppkReturnFields(CPPKTicketReturn cppkTicketReturn, Cursor cursor, Offset offset) {
        cppkTicketReturn.sumToReturn = new BigDecimal(cursor.getString(offset.value + CPPKTicketReturnColumns.PAYED.index));
        offset.value += CPPKTicketReturnColumns.all.length;
    }

    /**
     * Заполнить поля в сущности {@link ru.ppr.cppk.sync.kpp.ServiceSale}
     *
     * @param serviceSale
     * @param cursor
     * @param offset
     */
    public void fillServiceSaleFields(ServiceSale serviceSale, Cursor cursor, Offset offset) {
        serviceSale.price = new BigDecimal(cursor.getString(offset.value + ServiceSaleColumns.FULL.index));
        serviceSale.priceNds = new BigDecimal(cursor.getString(offset.value + ServiceSaleColumns.NDS.index));
        offset.value += ServiceSaleColumns.all.length;
    }

    public Price load(Cursor cursor, Offset offset, BigDecimal tax) {
        Price price = new Price();
        price.Full = new BigDecimal(cursor.getString(offset.value + Columns.FULL.index));
        price.Nds = new BigDecimal(cursor.getString(offset.value + Columns.NDS.index));
        price.Payed = new BigDecimal(cursor.getString(offset.value + Columns.PAYED.index));
        price.SummForReturn = new BigDecimal(cursor.getString(offset.value + Columns.SUM_FOR_RETURN.index));
        price.NdsPercent = tax == null ? 0 : tax.intValue();
        offset.value += Columns.all.length;
        return price;
    }
}