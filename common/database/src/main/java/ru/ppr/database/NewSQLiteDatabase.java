package ru.ppr.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import org.sqlite.database.sqlite.SQLiteDatabase;

import ru.ppr.logger.Logger;

/**
 * Реализация, работающая с новой верисей SqLite, вшитой в приложение {@link org.sqlite.database.sqlite.SQLiteDatabase)}.
 *
 * @author Aleksandr Brazhkin
 */
public class NewSQLiteDatabase implements Database {

    private static final String TAG = Logger.makeLogTag(NewSQLiteDatabase.class);

    private final SQLiteDatabase delegate;
    private TransactionListener transactionListener;

    NewSQLiteDatabase(SQLiteDatabase delegate) {
        this.delegate = delegate;
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return delegate.rawQuery(sql, selectionArgs);
    }

    @Override
    public void execSQL(String sql) throws SQLException {
        beforeModify();
        delegate.execSQL(sql);
    }

    @Override
    public void beginTransaction() {
        beforeModify();
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
        beforeModify();
        delegate.execSQL(sql, bindArgs);
    }

    @Override
    public DatabaseStatement compileStatement(String sql) {
        return new NewSQLiteDatabaseStatement(delegate.compileStatement(sql));
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
        beforeModify();
        return delegate.insertOrThrow(table, nullColumnHack, values);
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return delegate.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        beforeModify();
        return delegate.delete(table, whereClause, whereArgs);
    }

    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        beforeModify();
        return delegate.update(table, values, whereClause, whereArgs);
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        beforeModify();
        return delegate.insert(table, nullColumnHack, values);
    }

    @Override
    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
        beforeModify();
        return delegate.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
    }

    @Override
    public void setTransactionListener(TransactionListener transactionListener) {
        this.transactionListener = transactionListener;
    }

    // Вызывается пережд любой попыткой изменения данных
    private void beforeModify() {
        // В случае, если база в readOnly, фиксируем это в логе
        if (delegate.isReadOnly()) {
            Logger.warning(TAG, new Exception("attempt to modify data with readonly database state"));
        }
    }

}
