package ru.ppr.ikkm.file.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.file.state.model.Check;
import ru.ppr.ikkm.file.state.model.Item;
import ru.ppr.ikkm.file.state.model.Shift;


/**
 * Created by Артем on 21.01.2016.
 */
public class CheckDao extends AbstractPrinterDao {

    protected CheckDao(PrinterDaoSession database) {
        super(database);
    }

    public Check load(long id) {

        String selection = Properties.ID + " =?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        Cursor cursor = getPrinterDaoSession().getDatabase().query(Properties.TABLE_NAME, null, selection,
                selectionArgs, null, null, null, null);
        Check check = null;
        if (cursor.moveToFirst()) {
            check = readEntity(cursor);
            //Найдем все позиции для данного чека
            List<Item> items = getPrinterDaoSession().getItemDao().loadItemsForCheck(check);
            check.setItems(items);
            //найдем смену для данного чека
            long shiftId = cursor.getLong(cursor.getColumnIndex(Properties.SHIFT_ID));
            Shift shift = getPrinterDaoSession().getShiftDao().load(shiftId);
            check.setShift(shift);
        }
        cursor.close();
        return check;
    }

    public List<Check> loadCheckForShift(@NonNull Shift shift) {

        String selection = Properties.SHIFT_ID + " =?";
        String[] selectionArgs = new String[] {String.valueOf(shift.getId())};

        Cursor cursor = getPrinterDaoSession().getDatabase().query(Properties.TABLE_NAME, null,
                selection, selectionArgs, null, null, Properties.SPDN_NUMBER);

        List<Check> checks = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Check check = readEntity(cursor);
            check.setShift(shift);
            checks.add(check);
        }
        cursor.close();
        return checks;
    }

    public long save(@NonNull Check check) {

        getPrinterDaoSession().beginTransaction();

        long id = -1;
        try {
            ContentValues cv = createValues(check);
            id = getPrinterDaoSession().getDatabase().insertOrThrow(Properties.TABLE_NAME, null, cv);
            check.setId(id);
            ItemDao checkDao = getPrinterDaoSession().getItemDao();
            for (Item item : check.getItems()) {
                checkDao.save(item);
            }
            getPrinterDaoSession().setTransactionSuccessful();
        } finally {
            getPrinterDaoSession().endTransaction();
        }
        return id;
    }

    private ContentValues createValues(@NonNull Check check) {
        ContentValues cv = new ContentValues();
        cv.put(Properties.CHECK_TYPE, check.getType().getCode());
        cv.put(Properties.DATE, check.getPrintTime().getTime());
        cv.put(Properties.PAYMENT_METHOD, check.getPaymentMethod().getCode());
        cv.put(Properties.SPDN_NUMBER, check.getSpdnNumber());
        cv.put(Properties.SHIFT_ID, check.getShift().getId());
        cv.put(Properties.TOTAL, check.getTotal().toString());
        cv.put(Properties.PAYMENT, check.getPayment().toString());
        return cv;
    }

    public Check readEntity(Cursor cursor) {
        Check check = new Check();
        check.setSpdnNumber(cursor.getInt(cursor.getColumnIndex(Properties.SPDN_NUMBER)));
        check.setPrintTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.DATE))));
        check.setId(cursor.getInt(cursor.getColumnIndex(Properties.ID)));
        check.setPaymentMethod(IPrinter.PaymentType.create(cursor.getInt(cursor.getColumnIndex(Properties.PAYMENT_METHOD))));
        check.setType(IPrinter.DocType.create(cursor.getInt(cursor.getColumnIndex(Properties.CHECK_TYPE))));
        check.setTotal(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.TOTAL))));
        check.setPayment(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.PAYMENT))));
        return check;
    }

    public static class Properties {
        public static final String TABLE_NAME = "CHECKS";
        public static final String ID = "_id";
        public static final String CHECK_TYPE = "CheckType";
        public static final String PAYMENT_METHOD = "PaymentMethod";
        public static final String SPDN_NUMBER = "SpdnNumber";
        public static final String DATE = "Date";
        public static final String SHIFT_ID = "ShiftId";
        public static final String TOTAL = "Total";
        public static final String PAYMENT = "Payment";
    }

    public static void createTable(SQLiteDatabase db, boolean ifNoExist) {
        String constraint = ifNoExist? "IF NOT EXISTS ": "";
        String builder = "CREATE TABLE " + constraint + "\"" + Properties.TABLE_NAME + "\" (" +
                Properties.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Properties.CHECK_TYPE + " INTEGER NOT NULL, " +
                Properties.PAYMENT_METHOD + " INTEGER NOT NULL," +
                Properties.SPDN_NUMBER + " INTEGER NOT NULL," +
                Properties.DATE + " INTEGER NOT NULL," +
                Properties.TOTAL + " REAL NOT NULL," +
                Properties.PAYMENT + " REAL NOT NULL," +
                Properties.SHIFT_ID + " INTEGER NOT NULL)";

        db.execSQL(builder);
    }

    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"" + Properties.TABLE_NAME + "\"";
        db.execSQL(sql);
    }
}
