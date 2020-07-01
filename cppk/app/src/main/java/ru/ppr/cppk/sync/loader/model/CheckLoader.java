package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CheckDao;
import ru.ppr.cppk.sync.kpp.CPPKTicketReturn;
import ru.ppr.cppk.sync.kpp.FinePaidEvent;
import ru.ppr.cppk.sync.kpp.ServiceSale;
import ru.ppr.cppk.sync.kpp.TestTicketEvent;
import ru.ppr.cppk.sync.kpp.baseEntities.TicketEventBase;
import ru.ppr.cppk.sync.kpp.model.Check;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class CheckLoader extends BaseLoader {

    private final String loadQuery;

    public CheckLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadQuery = buildLoadQuery();
    }

    /**
     * Собственные поля сущности {@link Check}
     */
    public static class Columns {
        static final Column SPND_NUMBER = new Column(0, CheckDao.Properties.SpndNumber);
        static final Column ADDITIONAL_INFO = new Column(1, CheckDao.Properties.AdditionalInfo);
        static final Column PRINT_DATE_TIME = new Column(2, CheckDao.Properties.PrintDateTime);

        public static Column[] all = new Column[]{
                SPND_NUMBER,
                ADDITIONAL_INFO,
                PRINT_DATE_TIME
        };
    }

    /**
     * Поля сущности Check предназначенные для {@link TicketEventBase}
     */
    public static class TicketEventBaseColumns {
        static final Column SERIAL_NUMBER = new Column(0, CheckDao.Properties.SerialNumber);

        public static Column[] all = new Column[]{
                SERIAL_NUMBER
        };
    }

    /**
     * Поля сущности Check предназначенные для {@link FinePaidEvent}
     */
    public static class FinePaidEventColumns {
        static final Column SERIAL_NUMBER = new Column(0, CheckDao.Properties.SerialNumber);

        public static Column[] all = new Column[]{
                SERIAL_NUMBER
        };
    }

    /**
     * Поля сущности Check предназначенные для {@link ru.ppr.cppk.sync.kpp.CPPKTicketReturn}
     */
    public static class CPPKTicketReturnColumns {
        static final Column SERIAL_NUMBER = new Column(0, CheckDao.Properties.SerialNumber);

        public static Column[] all = new Column[]{
                SERIAL_NUMBER
        };
    }

    /**
     * Поля сущности Check предназначенные для {@link ru.ppr.cppk.sync.kpp.TestTicketEvent}
     */
    public static class TestTicketEventColumns {
        static final Column SERIAL_NUMBER = new Column(0, CheckDao.Properties.SerialNumber);
        static final Column PRINT_DATE_TIME = new Column(1, CheckDao.Properties.PrintDateTime);

        public static Column[] all = new Column[]{
                SERIAL_NUMBER,
                PRINT_DATE_TIME
        };
    }

    /**
     * Поля сущности Check предназначенные для {@link ru.ppr.cppk.sync.kpp.ServiceSale}
     */
    public static class ServiceSaleColumns {
        static final Column SERIAL_NUMBER = new Column(0, CheckDao.Properties.SerialNumber);

        public static Column[] all = new Column[]{
                SERIAL_NUMBER
        };
    }

    public Check load(Cursor cursor, Offset offset) {
        Check check = new Check();
        check.Number = cursor.getInt(offset.value + Columns.SPND_NUMBER.index);
        check.AdditionalInfo = cursor.getString(offset.value + Columns.ADDITIONAL_INFO.index);
        check.PrintDateTime = new Date(cursor.getLong(offset.value + Columns.PRINT_DATE_TIME.index));
        offset.value += Columns.all.length;
        return check;
    }

    public Check load(long checkId) {

        String[] selectionArgs = new String[]{String.valueOf(checkId)};

        Check check = null;
        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                check = load(cursor, new Offset());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return check;
    }

    /**
     * Заполнить поля в сущности {@link TicketEventBase}
     *
     * @param ticketEventBase
     * @param cursor
     * @param offset
     */
    public void fillTicketEventBaseFields(TicketEventBase ticketEventBase, Cursor cursor, Offset offset) {
        ticketEventBase.TicketNumber = cursor.getInt(offset.value + TicketEventBaseColumns.SERIAL_NUMBER.index);
        offset.value += TicketEventBaseColumns.all.length;
    }

    /**
     * Заполнить поля в сущности {@link FinePaidEvent}
     *
     * @param finePaidEvent
     * @param cursor
     * @param offset
     */
    public void fillFinePaidEventFields(FinePaidEvent finePaidEvent, Cursor cursor, Offset offset) {
        finePaidEvent.docNumber = cursor.getInt(offset.value + FinePaidEventColumns.SERIAL_NUMBER.index);
        offset.value += FinePaidEventColumns.all.length;
    }

    /**
     * Заполнить поля в сущности {@link CPPKTicketReturn}
     *
     * @param cppkTicketReturn
     * @param cursor
     * @param offset
     */
    public void fillCppkReturnFields(CPPKTicketReturn cppkTicketReturn, Cursor cursor, Offset offset) {
        cppkTicketReturn.recallTicketNumber = cursor.getInt(offset.value + CPPKTicketReturnColumns.SERIAL_NUMBER.index);
        offset.value += CPPKTicketReturnColumns.all.length;
    }

    /**
     * Заполнить поля в сущности {@link TestTicketEvent}
     *
     * @param testTicketEvent
     * @param cursor
     * @param offset
     */
    public void fillTestTicketEventFields(TestTicketEvent testTicketEvent, Cursor cursor, Offset offset) {
        testTicketEvent.number = cursor.getInt(offset.value + TestTicketEventColumns.SERIAL_NUMBER.index);
        testTicketEvent.printDateTime = new Date(cursor.getLong(offset.value + TestTicketEventColumns.PRINT_DATE_TIME.index));
        offset.value += TestTicketEventColumns.all.length;
    }

    /**
     * Заполнить поля в сущности {@link ServiceSale}
     *
     * @param serviceSale
     * @param cursor
     * @param offset
     */
    public void fillServiceSaleFields(ServiceSale serviceSale, Cursor cursor, Offset offset) {
        serviceSale.ticketNumber = cursor.getInt(offset.value + ServiceSaleColumns.SERIAL_NUMBER.index);
        offset.value += ServiceSaleColumns.all.length;
    }


    private String buildLoadQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(createColumnsForSelect(CheckDao.TABLE_NAME, Columns.all));
        sb.append(" FROM ");
        sb.append(CheckDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}