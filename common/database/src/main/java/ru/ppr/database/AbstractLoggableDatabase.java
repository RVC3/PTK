package ru.ppr.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.support.annotation.NonNull;

import java.util.Arrays;

import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public abstract class AbstractLoggableDatabase<DBS extends DatabaseStatement> implements Database {

    private static final String TAG = Logger.makeLogTag(LoggableDatabase.class);
    /**
     * БД, для кооторой выполняется логирование запросов.
     */
    private final Database delegate;
    /**
     * Наименование БД в логах
     */
    private final String loggingName;
    /**
     * Переиспользуемый {@link StringBuilder}
     */
    private final ThreadLocal<StringBuilder> stringBuilder;
    /**
     * Флаг, что логирование включено
     */
    private boolean logEnabled;

    public AbstractLoggableDatabase(@NonNull Database delegate, @NonNull String loggingName) {
        this.delegate = delegate;
        this.loggingName = loggingName;

        stringBuilder = new ThreadLocal<StringBuilder>() {
            @Override
            protected StringBuilder initialValue() {
                return new StringBuilder();
            }
        };
    }

    private StringBuilder sb() {
        StringBuilder sb = stringBuilder.get();
        sb.delete(0, sb.length());
        sb.append(loggingName);
        sb.append(" ");
        return sb;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        if (logEnabled) {
            Logger.trace(TAG, sb().append("rawQuery, sql = ").append(sql).append(", args = ").append(Arrays.toString(selectionArgs)).toString());
        }
        return delegate.rawQuery(sql, selectionArgs);
    }

    @Override
    public void execSQL(String sql) throws SQLException {
        if (logEnabled) {
            Logger.trace(TAG, sb().append("rawQuery, sql = ").append(sql).toString());
        }
        delegate.execSQL(sql);
    }

    @Override
    public void beginTransaction() {
        if (logEnabled) {
            Logger.trace(TAG, sb().append("beginTransaction").toString());
        }
        delegate.beginTransaction();
    }

    @Override
    public void endTransaction() {
        if (logEnabled) {
            Logger.trace(TAG, sb().append("endTransaction").toString());
        }
        delegate.endTransaction();
    }

    @Override
    public boolean inTransaction() {
        boolean res = delegate.inTransaction();
        if (logEnabled) {
            Logger.trace(TAG, sb().append("inTransaction, res = ").append(res).toString());
        }
        return res;
    }

    @Override
    public void setTransactionSuccessful() {
        if (logEnabled) {
            Logger.trace(TAG, sb().append("setTransactionSuccessful").toString());
        }
        delegate.setTransactionSuccessful();
    }

    @Override
    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        if (logEnabled) {
            Logger.trace(TAG, sb().append("execSQL, sql = ").append(sql).append(", bindArgs = ").append(Arrays.toString(bindArgs)).toString());
        }
        delegate.execSQL(sql, bindArgs);
    }

    @Override
    public DBS compileStatement(String sql) {
        if (logEnabled) {
            Logger.trace(TAG, sb().append("compileStatement, sql = ").append(sql).toString());
        }
        return compileStatementInternal(sql);
    }

    protected abstract DBS compileStatementInternal(String sql);

    @Override
    public boolean isDbLockedByCurrentThread() {
        boolean res = delegate.isDbLockedByCurrentThread();
        if (logEnabled) {
            Logger.trace(TAG, sb().append("isDbLockedByCurrentThread, res = ").append(res).toString());
        }
        return res;
    }

    @Override
    public void close() {
        if (logEnabled) {
            Logger.trace(TAG, sb().append("close").toString());
        }
        delegate.close();
    }

    @Override
    public String getPath() {
        String res = delegate.getPath();
        if (logEnabled) {
            Logger.trace(TAG, sb().append("getPath, res = ").append(res).toString());
        }
        return res;
    }

    @Override
    public boolean isOpen() {
        boolean res = delegate.isOpen();
        if (logEnabled) {
            Logger.trace(TAG, sb().append("isOpen, res = ").append(res).toString());
        }
        return res;
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        if (logEnabled) {
            Logger.trace(TAG, sb().append("query, table = ").append(table)
                    .append(", columns = ").append(Arrays.toString(columns))
                    .append(", selection = ").append(selection)
                    .append(", selectionArgs = ").append(Arrays.toString(selectionArgs))
                    .append(", groupBy = ").append(groupBy)
                    .append(", having = ").append(having)
                    .append(", orderBy = ").append(orderBy)
                    .toString());
        }
        return delegate.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    @Override
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
        long res = delegate.insertOrThrow(table, nullColumnHack, values);
        if (logEnabled) {
            Logger.trace(TAG, sb().append("insertOrThrow, table = ").append(table)
                    .append(", nullColumnHack = ").append(nullColumnHack)
                    .append(", values = ").append(values)
                    .append(", res = ").append(res)
                    .toString());
        }
        return res;
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        if (logEnabled) {
            Logger.trace(TAG, sb().append("query, table = ").append(table)
                    .append(", columns = ").append(Arrays.toString(columns))
                    .append(", selection = ").append(selection)
                    .append(", selectionArgs = ").append(Arrays.toString(selectionArgs))
                    .append(", groupBy = ").append(groupBy)
                    .append(", having = ").append(having)
                    .append(", orderBy = ").append(orderBy)
                    .append(", limit = ").append(limit)
                    .toString());
        }
        return delegate.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        int res = delegate.delete(table, whereClause, whereArgs);
        if (logEnabled) {
            Logger.trace(TAG, sb().append("delete, table = ").append(table)
                    .append(", whereClause = ").append(whereClause)
                    .append(", whereArgs = ").append(Arrays.toString(whereArgs))
                    .append(", res = ").append(res)
                    .toString());
        }
        return res;
    }

    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        int res = delegate.update(table, values, whereClause, whereArgs);
        if (logEnabled) {
            Logger.trace(TAG, sb().append("update, table = ").append(table)
                    .append(", values = ").append(values)
                    .append(", whereClause = ").append(whereClause)
                    .append(", whereArgs = ").append(Arrays.toString(whereArgs))
                    .append(", res = ").append(res)
                    .toString());
        }
        return res;
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        long res = delegate.insert(table, nullColumnHack, values);
        if (logEnabled) {
            Logger.trace(TAG, sb().append("insert, table = ").append(table)
                    .append(", nullColumnHack = ").append(nullColumnHack)
                    .append(", values = ").append(values)
                    .append(", res = ").append(res)
                    .toString());
        }
        return res;
    }

    @Override
    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
        long res = delegate.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
        if (logEnabled) {
            Logger.trace(TAG, sb().append("insert, table = ").append(table)
                    .append(", nullColumnHack = ").append(nullColumnHack)
                    .append(", initialValues = ").append(initialValues)
                    .append(", conflictAlgorithm = ").append(conflictAlgorithm)
                    .append(", res = ").append(res)
                    .toString());
        }
        return res;
    }

    @Override
    public void setTransactionListener(TransactionListener transactionListener) {
        delegate.setTransactionListener(transactionListener);
    }
}
