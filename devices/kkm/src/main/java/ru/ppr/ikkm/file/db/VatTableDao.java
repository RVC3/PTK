package ru.ppr.ikkm.file.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

/**
 * Created by Артем on 22.01.2016.
 */
public class VatTableDao extends AbstractPrinterDao {

    protected VatTableDao(PrinterDaoSession printerDaoSession) {
        super(printerDaoSession);
    }

    public void saveVatTable(SparseArray<Integer> vatTable) {

        SQLiteDatabase database = getPrinterDaoSession().getDatabase();
        database.beginTransaction();
        try {
            final int size = vatTable.size();
            for (int i = 0; i < size; i++) {
                Integer item = vatTable.valueAt(i);
                if(item != null) {

                    ContentValues cv = createValues(i, item);
                    database.insertWithOnConflict(Properties.TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public SparseArray<Integer> getVatTable() {

        Cursor cursor = getPrinterDaoSession().getDatabase().query(Properties.TABLE_NAME,
                null, null, null, null, null, null);

        SparseArray<Integer> vatTable = new SparseArray<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(Properties.ID));
            int value = cursor.getInt(cursor.getColumnIndex(Properties.VAT_VALUE));
            vatTable.append(id, value);
        }
        //на всякий случай нулевую ставку установим равной 0
        vatTable.put(0,0);
        cursor.close();
        return vatTable;
    }

    private ContentValues createValues(int id, int value) {
        ContentValues cv = new ContentValues(2);
        cv.put(Properties.ID, id);
        cv.put(Properties.VAT_VALUE, value);
        return cv;
    }

    public static class Properties {
        public static final String ID = "_id";
        public static final String VAT_VALUE = "VAT_VALUE";
        public static final String TABLE_NAME = "VatTable";
    }

    public static void createTable(SQLiteDatabase db, boolean ifNoExist) {
        String constraint = ifNoExist ? "IF NOT EXISTS " : "";
        final String query = "CREATE TABLE " + constraint + "\"" + Properties.TABLE_NAME + "\" (" +
                Properties.ID + " INTEGER PRIMARY KEY," +
                Properties.VAT_VALUE + " INTEGER NOT NULL)";
        db.execSQL(query);
    }

    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"" + Properties.TABLE_NAME + "\"";
        db.execSQL(sql);
    }
}
