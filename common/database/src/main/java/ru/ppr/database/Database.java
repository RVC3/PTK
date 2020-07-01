package ru.ppr.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

/**
 * Интерфейс БД, замена {@link android.database.sqlite.SQLiteDatabase)}.
 *
 * @author Aleksandr Brazhkin
 */
public interface Database {

    /**
     * {@value android.database.sqlite.SQLiteDatabase#CONFLICT_ROLLBACK}
     */
    int CONFLICT_ROLLBACK = 1;
    /**
     * {@value android.database.sqlite.SQLiteDatabase#CONFLICT_ABORT}
     */
    int CONFLICT_ABORT = 2;
    /**
     * {@value android.database.sqlite.SQLiteDatabase#CONFLICT_FAIL}
     */
    int CONFLICT_FAIL = 3;
    /**
     * {@value android.database.sqlite.SQLiteDatabase#CONFLICT_IGNORE}
     */
    int CONFLICT_IGNORE = 4;
    /**
     * {@value android.database.sqlite.SQLiteDatabase#CONFLICT_REPLACE}
     */
    int CONFLICT_REPLACE = 5;
    /**
     * {@value android.database.sqlite.SQLiteDatabase#CONFLICT_NONE}
     */
    int CONFLICT_NONE = 0;

    Cursor rawQuery(String sql, String[] selectionArgs);

    void execSQL(String sql) throws SQLException;

    void beginTransaction();

    void endTransaction();

    boolean inTransaction();

    void setTransactionSuccessful();

    void execSQL(String sql, Object[] bindArgs) throws SQLException;

    DatabaseStatement compileStatement(String sql);

    boolean isDbLockedByCurrentThread();

    void close();

    String getPath();

    boolean isOpen();

    Cursor query(String table, String[] columns, String selection,
                 String[] selectionArgs, String groupBy, String having,
                 String orderBy);

    long insertOrThrow(String table, String nullColumnHack, ContentValues values)
            throws SQLException;

    Cursor query(String table, String[] columns, String selection,
                 String[] selectionArgs, String groupBy, String having,
                 String orderBy, String limit);

    int delete(String table, String whereClause, String[] whereArgs);

    int update(String table, ContentValues values, String whereClause, String[] whereArgs);

    long insert(String table, String nullColumnHack, ContentValues values);

    long insertWithOnConflict(String table, String nullColumnHack,
                              ContentValues initialValues, int conflictAlgorithm);

    void setTransactionListener(TransactionListener transactionListener);

    interface TransactionListener {
        void onEndTransaction();
    }
}
