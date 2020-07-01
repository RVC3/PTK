package ru.ppr.ikkm.file.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ppr.ikkm.file.state.model.PrinterSettings;

/**
 * Created by Артем on 21.01.2016.
 */
public class PrinterSettingDao extends AbstractPrinterDao {

    private final static String SELECTION_FOR_STATE_ID = Properties.ID + " =?";
    private static final String SEPARATOR = "~";

    protected PrinterSettingDao(PrinterDaoSession database) {
        super(database);
    }

    /**
     * Загружает первую запись из настроек.
     * По факту в БД храниться только 1 запись, которая постоянно обновляется.
     *
     * @return
     * @param printerId
     */
    @NonNull
    public PrinterSettings loadFirst(long printerId, String printerModel) {

        String[] selectionArgs = new String[]{String.valueOf(printerId)};
        Cursor cursor = getPrinterDaoSession().getDatabase().query(Properties.TABLE_NAME, null,
                SELECTION_FOR_STATE_ID, selectionArgs, null, null, null, null);
        PrinterSettings printerSettings;
        if (cursor.moveToFirst()) {
            printerSettings = readEntity(cursor);
            SparseArray<Integer> vatTable = getPrinterDaoSession().getVatTableDao().getVatTable();
            printerSettings.setVatTable(vatTable);
        } else {
            //настройки по умолчанию
            printerSettings = new PrinterSettings(printerId, printerModel);
        }
        cursor.close();
        return printerSettings;
    }

    public long saveOrUpdate(@NonNull PrinterSettings printerSettings) {

        getPrinterDaoSession().getVatTableDao().saveVatTable(printerSettings.getVatTable());

        ContentValues contentValues = createValues(printerSettings);
        //id ставим вручную, пэтому повторно устанавлвать не надо
        return getPrinterDaoSession().getDatabase().insertWithOnConflict(Properties.TABLE_NAME,
                null,
                contentValues,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    private ContentValues createValues(@NonNull PrinterSettings printerSettings) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.ID, printerSettings.getId());
        contentValues.put(Properties.CHECK_NUMBER, printerSettings.getCheckNumber());
        contentValues.put(Properties.CURRENT_SHIFT_NUMBER, printerSettings.getShiftNumber());
        contentValues.put(Properties.INN, printerSettings.getInn());
        contentValues.put(Properties.REGISTER_NUMBER, printerSettings.getRegisterNumber());
        contentValues.put(Properties.ODOMETER_VALUE, printerSettings.getOdometerValue());
        contentValues.put(Properties.AVAILABLE_DOCS, printerSettings.getAvailableDocs());
        contentValues.put(Properties.AVAILABLE_SHIFTS, printerSettings.getAvailableShifts());
        contentValues.put(Properties.EKLZ_NUMBER, printerSettings.getEklzNumber());
        contentValues.put(Properties.MODEL, printerSettings.getModel());
        String headersAsString = "";
        List<String> headers = printerSettings.getHeaderLines();
        if (!headers.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String line : headers) {
                builder.append(line).append(SEPARATOR);
            }
            //Последний разделительно не нужен
            headersAsString = builder.substring(0, builder.length() - 1);
        }
        contentValues.put(Properties.HEADERS, headersAsString);
        contentValues.put(Properties.KKM_NUMBER, printerSettings.getSerialNumber());

        return contentValues;
    }

    public PrinterSettings readEntity(Cursor cursor) {
        long printerId = cursor.getInt(cursor.getColumnIndex(Properties.ID));
        String model = cursor.getString(cursor.getColumnIndex(Properties.MODEL));
        PrinterSettings printerSettings = new PrinterSettings(printerId, model);
        printerSettings.setShiftNumber(cursor.getInt(cursor.getColumnIndex(Properties.CURRENT_SHIFT_NUMBER)));
        printerSettings.setCheckNumber(cursor.getInt(cursor.getColumnIndex(Properties.CHECK_NUMBER)));
        printerSettings.setSerialNumber(cursor.getString(cursor.getColumnIndex(Properties.KKM_NUMBER)));
        printerSettings.setInn(cursor.getString(cursor.getColumnIndex(Properties.INN)));
        printerSettings.setRegisterNumber(cursor.getString(cursor.getColumnIndex(Properties.REGISTER_NUMBER)));
        printerSettings.setOdometerValue(cursor.getLong(cursor.getColumnIndex(Properties.ODOMETER_VALUE)));
        printerSettings.setAvailableShifts(cursor.getLong(cursor.getColumnIndex(Properties.AVAILABLE_SHIFTS)));
        printerSettings.setAvailableDocs(cursor.getLong(cursor.getColumnIndex(Properties.AVAILABLE_DOCS)));
        printerSettings.setEklzNumber(cursor.getString(cursor.getColumnIndex(Properties.EKLZ_NUMBER)));
        String headers = cursor.getString(cursor.getColumnIndex(Properties.HEADERS));
        if (headers.isEmpty()) {
            printerSettings.setHeaderLines(Collections.emptyList());
        } else {
            String[] headersArray = headers.split(SEPARATOR);
            printerSettings.setHeaderLines(new ArrayList<>(Arrays.asList(headersArray)));
        }

        return printerSettings;
    }

    public static class Properties {
        public static final String TABLE_NAME = "Settings";
        public static final String ID = "_id";
        public static final String HEADERS = "Headers";
        public static final String KKM_NUMBER = "KkmNumber";
        public static final String CURRENT_SHIFT_NUMBER = "CurrentShiftNumber";
        public static final String CHECK_NUMBER = "CheckNumber";
        public static final String INN = "Inn";
        public static final String REGISTER_NUMBER = "RegisterNumber";
        public static final String ODOMETER_VALUE = "OdometerValue";
        public static final String AVAILABLE_DOCS = "AvailableDocs";
        public static final String AVAILABLE_SHIFTS = "AvailableShifts";
        public static final String MODEL = "Model";
        public static final String EKLZ_NUMBER = "EklzNumber";
    }

    public static void createTable(SQLiteDatabase db, boolean ifNoExist) {
        String constraint = ifNoExist ? "IF NOT EXISTS " : "";
        String builder = "CREATE TABLE " + constraint + "\"" + Properties.TABLE_NAME + "\" (" +
                Properties.ID + " INTEGER PRIMARY KEY, " +
                Properties.HEADERS + " TEXT NOT NULL, " +
                Properties.KKM_NUMBER + " TEXT NOT NULL, " +
                Properties.CURRENT_SHIFT_NUMBER + " INTEGER NOT NULL, " +
                Properties.ODOMETER_VALUE + " INTEGER NOT NULL, " +
                Properties.AVAILABLE_DOCS + " INTEGER NOT NULL, " +
                Properties.AVAILABLE_SHIFTS + " INTEGER NOT NULL, " +
                Properties.EKLZ_NUMBER + " TEXT NOT NULL, " +
                Properties.MODEL + " TEXT NOT NULL, " +
                Properties.INN + " TEXT NOT NULL, " +
                Properties.REGISTER_NUMBER + " TEXT NOT NULL, " +
                Properties.CHECK_NUMBER + " INTEGER NOT NULL)";
        db.execSQL(builder);
    }

    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"" + Properties.TABLE_NAME + "\"";
        db.execSQL(sql);
    }
}
