package ru.ppr.database;

import android.content.Context;
import android.util.Log;

import org.sqlite.database.DatabaseErrorHandler;
import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteException;
import org.sqlite.database.sqlite.SQLiteOpenHelper;

import ru.ppr.logger.Logger;

/**
 * SQLiteOpenHelper с возможностб открытия БД только на чтение.
 * Системный (или библиотечный) {@link SQLiteOpenHelper} всегда открывает БД на запись, даже если запрошена БД на чтение.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class NewSQLiteReadOnlyOpenHelper extends SQLiteOpenHelper {

    private final String TAG = Logger.makeLogTag(NewSQLiteReadOnlyOpenHelper.class);

    private final Context mContext;
    private final String mName;
    private final SQLiteDatabase.CursorFactory mFactory;
    private final int mNewVersion;
    private SQLiteDatabase mDatabase;
    private boolean mIsInitializing;
    private final DatabaseErrorHandler mErrorHandler;

    protected NewSQLiteReadOnlyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    private NewSQLiteReadOnlyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        this.mContext = context;
        this.mName = name;
        this.mFactory = factory;
        this.mNewVersion = version;
        this.mErrorHandler = errorHandler;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        synchronized (this) {
            return getDatabaseLocked(false);
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        synchronized (this) {
            return getDatabaseLocked(true);
        }
    }

    /**
     * Метод позволяет создать и/или открыть базу данных.
     * Если база данных открывается только для чтения, но версия БД отлична, то база данных
     * будет переоткрыта для чтения/записи и выпонятся действия необходимые для обновления.
     * Затем база данных будет открыта только для чтения.
     *
     * @param readonly флаг, сигланизирующий об открытии БД только для чтения.
     * @return открытая база данных.
     */
    private SQLiteDatabase getDatabaseLocked(final boolean readonly) {
        if (mDatabase != null) {
            if (!mDatabase.isOpen()) {
                mDatabase = null;
            } else if (readonly || !mDatabase.isReadOnly()) {
                return mDatabase;
            }
        }

        if (mIsInitializing) {
            throw new IllegalStateException("getDatabase called recursively");
        }

        final String path = mContext.getDatabasePath(mName).getPath();
        SQLiteDatabase db = mDatabase;

        try {
            mIsInitializing = true;

            try {
                if (readonly) {
                    db = SQLiteDatabase.openDatabase(path, mFactory, SQLiteDatabase.OPEN_READONLY, mErrorHandler);
                } else {
                    db = SQLiteDatabase.openOrCreateDatabase(path, mFactory, mErrorHandler);
                }
            } catch (SQLiteException ex) {
                if (!readonly) {
                    throw ex;
                }

                Log.e(TAG, "Couldn't open " + mName + " for writing (will try read-only):", ex);
                db = SQLiteDatabase.openDatabase(path, mFactory, SQLiteDatabase.OPEN_READONLY, mErrorHandler);
            }

            onConfigure(db);

            final int version = db.getVersion();
            if (version != mNewVersion) {
                if (readonly) {
                    db.close();
                    db = SQLiteDatabase.openOrCreateDatabase(path, mFactory, mErrorHandler);
                }

                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        if (version > mNewVersion) {
                            onDowngrade(db, version, mNewVersion);
                        } else {
                            onUpgrade(db, version, mNewVersion);
                        }
                    }
                    db.setVersion(mNewVersion);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (readonly) {
                    db.close();
                    db = SQLiteDatabase.openDatabase(path, mFactory, SQLiteDatabase.OPEN_READONLY, mErrorHandler);
                }
            }

            onOpen(db);

            if (db.isReadOnly()) {
                Log.w(TAG, "Opened " + mName + " in read-only mode, version = " + String.valueOf(db.getVersion()));
            }

            mDatabase = db;
            return db;
        } finally {
            mIsInitializing = false;
            if ((db != null) && (db != mDatabase)) {
                db.close();
            }
        }
    }

    @Override
    public synchronized void close() {
        if (mIsInitializing) {
            throw new IllegalStateException("Closed during initialization");
        } else {
            if (mDatabase != null && mDatabase.isOpen()) {
                mDatabase.close();
                mDatabase = null;
            }
        }
    }
}
