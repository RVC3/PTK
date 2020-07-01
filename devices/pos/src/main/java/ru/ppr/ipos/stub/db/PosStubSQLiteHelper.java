package ru.ppr.ipos.stub.db;

import android.content.Context;

import java.io.File;

import ru.ppr.database.Database;
import ru.ppr.database.NewSQLiteDbOpenHelper;

/**
 * @author Dmitry Vinogradov
 */
public class PosStubSQLiteHelper extends NewSQLiteDbOpenHelper {

    private static final String TAG = PosStubSQLiteHelper.class.getSimpleName();

    public static final int VERSION = 1;
    public static final String DB_NAME = "PosStubState.db";

    private final Context context;

    public PosStubSQLiteHelper(Context context) {
        super(context, DB_NAME, VERSION);
        this.context = context;
    }

    File getDatabasePath() {
        return context.getDatabasePath(getDatabaseName());
    }

    private void createAlLTable(Database db, boolean ifNotExists) {
        TransactionDao.createTable(db, ifNotExists);
        PosDayDao.createTable(db, ifNotExists);
        PosPropertyDao.createTable(db, ifNotExists);
    }

    private void dropAllTable(Database db, boolean ifExists) {
        TransactionDao.dropTable(db, ifExists);
        PosDayDao.dropTable(db, ifExists);
        PosPropertyDao.dropTable(db, ifExists);
    }

    @Override
    public void onCreate(Database db) {
        db.beginTransaction();
        try {
            createAlLTable(db, true);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            dropAllTable(db, true);
            createAlLTable(db, true);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onOpen(Database db) {
    }
}
