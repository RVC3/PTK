package ru.ppr.ikkm.file.db;

/**
 * Created by Артем on 21.01.2016.
 */
public abstract class AbstractPrinterDao {

    private final PrinterDaoSession printerDaoSession;

    protected AbstractPrinterDao(PrinterDaoSession printerDaoSession) {
        this.printerDaoSession = printerDaoSession;
    }

    protected PrinterDaoSession getPrinterDaoSession() {
        return printerDaoSession;
    }
}
