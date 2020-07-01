package ru.ppr.ikkm.file.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Артем on 21.01.2016.
 */
public class PrinterSQLiteHelper extends SQLiteOpenHelper{

    private static final String TAG = PrinterSQLiteHelper.class.getSimpleName();

    public static final int VERSION = 3;
    public static final String DB_NAME = "PrinterState.db";

    public PrinterSQLiteHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            createAlLTable(db, true);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            dropAllTable(db, true);
            createAlLTable(db, true);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void createAlLTable(SQLiteDatabase db, boolean ifNotExists) {
        PrinterSettingDao.createTable(db, ifNotExists);
        OperatorDao.createTable(db, ifNotExists);
        ItemDao.createTable(db, ifNotExists);
        CheckDao.createTable(db, ifNotExists);
        ShiftDao.createTable(db, ifNotExists);
        VatTableDao.createTable(db, ifNotExists);
    }

    private void dropAllTable(SQLiteDatabase db, boolean ifExists) {
        CheckDao.dropTable(db, ifExists);
        ItemDao.dropTable(db, ifExists);
        OperatorDao.dropTable(db, ifExists);
        PrinterSettingDao.dropTable(db, ifExists);
        ShiftDao.dropTable(db, ifExists);
        VatTableDao.dropTable(db, ifExists);
    }
}
