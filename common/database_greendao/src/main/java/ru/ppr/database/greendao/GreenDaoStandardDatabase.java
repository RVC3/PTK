package ru.ppr.database.greendao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


/**
 * @author Aleksandr Brazhkin
 */
public class GreenDaoStandardDatabase implements GreenDaoDatabase {

    private final SQLiteDatabase delegate;
    private TransactionListener transactionListener;

    GreenDaoStandardDatabase(SQLiteDatabase delegate) {
        this.delegate = delegate;
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return delegate.rawQuery(sql, selectionArgs);
    }

    @Override
    public void execSQL(String sql) throws SQLException {
        delegate.execSQL(sql);
    }

    @Override
    public void beginTransaction() {
        delegate.beginTransaction();
    }

    @Override
    public void endTransaction() {
        delegate.endTransaction();
        if (transactionListener != null && !delegate.inTransaction()) {
            transactionListener.onEndTransaction();
        }
    }

    @Override
    public boolean inTransaction() {
        return delegate.inTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        delegate.setTransactionSuccessful();
    }

    @Override
    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        delegate.execSQL(sql, bindArgs);
    }

    @Override
    public GreenDaoStandardDatabaseStatement compileStatement(String sql) {
        return new GreenDaoStandardDatabaseStatement(delegate.compileStatement(sql));
    }

    @Override
    public boolean isDbLockedByCurrentThread() {
        return delegate.isDbLockedByCurrentThread();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return delegate.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    @Override
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
        return delegate.insertOrThrow(table, nullColumnHack, values);
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return delegate.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return delegate.delete(table, whereClause, whereArgs);
    }

    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return delegate.update(table, values, whereClause, whereArgs);
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return delegate.insert(table, nullColumnHack, values);
    }

    @Override
    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
        return delegate.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
    }

    @Override
    public void setTransactionListener(TransactionListener transactionListener) {
        this.transactionListener = transactionListener;
    }

    @Override
    public Object getRawDatabase() {
        return delegate;
    }

}
