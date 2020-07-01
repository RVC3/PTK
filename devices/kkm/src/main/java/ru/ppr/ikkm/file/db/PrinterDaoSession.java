package ru.ppr.ikkm.file.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Артем on 21.01.2016.
 */
public class PrinterDaoSession {

    private final PrinterSQLiteHelper helper;
    private final SQLiteDatabase database;
    private final ShiftDao shiftDao;
    private final OperatorDao cashierDao;
    private final CheckDao checkDao;
    private final ItemDao itemDao;
    private final PrinterSettingDao printerSettingDao;
    private final VatTableDao vatTableDao;

    // В будущем переделать на singleton
    public PrinterDaoSession(PrinterSQLiteHelper sqLiteHelper) {
        this.helper = sqLiteHelper;
        this.database = helper.getWritableDatabase();
        shiftDao = new ShiftDao(this);
        printerSettingDao = new PrinterSettingDao(this);
        cashierDao = new OperatorDao(this);
        checkDao = new CheckDao(this);
        itemDao = new ItemDao(this);
        vatTableDao = new VatTableDao(this);
    }

    public PrinterSQLiteHelper getHelper(){
        return helper;
    }

    SQLiteDatabase getDatabase(){
        return database;
    }

    public ShiftDao getShiftDao() {
        return shiftDao;
    }

    public OperatorDao getCashierDao() {
        return cashierDao;
    }

    public CheckDao getCheckDao() {
        return checkDao;
    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    public PrinterSettingDao getPrinterSettingDao() {
        return printerSettingDao;
    }

    public VatTableDao getVatTableDao() {
        return vatTableDao;
    }

    public void beginTransaction(){
        database.beginTransaction();
    }

    public void endTransaction(){
        database.endTransaction();
    }

    public void setTransactionSuccessful(){
        database.setTransactionSuccessful();
    }
}
