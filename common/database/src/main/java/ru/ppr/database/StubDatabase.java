package ru.ppr.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;

import ru.ppr.logger.Logger;

/**
 * Класс заглушка для несуществующей базы
 * Любой запрос к ней возвращает пустой курсор
 * База данных находится в режиме readOnly, соответсвенно любые попытки изменения данных логгируются как warning
 *
 * @author m.sidorov
 */
public class StubDatabase implements Database {

    private static final String TAG = Logger.makeLogTag(LoggableDatabase.class);

    private static final Cursor EMPTY_CURSOR = new EmptyStubCursor();
    private static final DatabaseStatement STUB_STATEMENT = new StubDatabaseStatement();

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return EMPTY_CURSOR;
    }

    @Override
    public void execSQL(String sql) throws SQLException {
        beforeModification();
    }

    @Override
    public void beginTransaction() {
        beforeModification();
    }

    @Override
    public void endTransaction() {
    }

    @Override
    public boolean inTransaction() {
        return false;
    }

    @Override
    public void setTransactionSuccessful() {

    }

    @Override
    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        beforeModification();
    }

    @Override
    public DatabaseStatement compileStatement(String sql) {
        return STUB_STATEMENT;
    }

    @Override
    public boolean isDbLockedByCurrentThread() {
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return EMPTY_CURSOR;
    }

    @Override
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
        beforeModification();
        return -1;
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return EMPTY_CURSOR;
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        beforeModification();
        return 0;
    }

    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        beforeModification();
        return 0;
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        beforeModification();
        return -1;
    }

    @Override
    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
        beforeModification();
        return -1;
    }

    @Override
    public void setTransactionListener(TransactionListener transactionListener) {

    }

    // Здесь можно контролировать попытки изменять данные.
    // Для данного класса по идее база данных находится в режиме readOnly и допускает только безопасное чтение
    // Пока решил не возбуждать исключение, но записывать его в лог необходимо
    private void beforeModification() {
        Exception e = new RuntimeException("Stub database class: data modification is not supported");
        Logger.warning(TAG, e);
        // throw e;
    }

    private static class EmptyStubCursor implements Cursor {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public int getPosition() {
            return -1;
        }

        @Override
        public boolean move(int offset) {
            return false;
        }

        @Override
        public boolean moveToPosition(int position) {
            return false;
        }

        @Override
        public boolean moveToFirst() {
            return false;
        }

        @Override
        public boolean moveToLast() {
            return false;
        }

        @Override
        public boolean moveToNext() {
            return false;
        }

        @Override
        public boolean moveToPrevious() {
            return false;
        }

        @Override
        public boolean isFirst() {
            return false;
        }

        @Override
        public boolean isLast() {
            return false;
        }

        @Override
        public boolean isBeforeFirst() {
            return false;
        }

        @Override
        public boolean isAfterLast() {
            return false;
        }

        @Override
        public int getColumnIndex(String columnName) {
            return 0;
        }

        @Override
        public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
            return 0;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return null;
        }

        @Override
        public String[] getColumnNames() {
            return new String[0];
        }

        @Override
        public int getColumnCount() {
            return 0;
        }

        @Override
        public byte[] getBlob(int columnIndex) {
            return new byte[0];
        }

        @Override
        public String getString(int columnIndex) {
            return null;
        }

        @Override
        public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {

        }

        @Override
        public short getShort(int columnIndex) {
            return 0;
        }

        @Override
        public int getInt(int columnIndex) {
            return 0;
        }

        @Override
        public long getLong(int columnIndex) {
            return 0;
        }

        @Override
        public float getFloat(int columnIndex) {
            return 0;
        }

        @Override
        public double getDouble(int columnIndex) {
            return 0;
        }

        @Override
        public int getType(int columnIndex) {
            return 0;
        }

        @Override
        public boolean isNull(int columnIndex) {
            return false;
        }

        @Override
        public void deactivate() {

        }

        @Override
        public boolean requery() {
            return false;
        }

        @Override
        public void close() {

        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public void registerContentObserver(ContentObserver observer) {

        }

        @Override
        public void unregisterContentObserver(ContentObserver observer) {

        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void setNotificationUri(ContentResolver cr, Uri uri) {

        }

        @Override
        public Uri getNotificationUri() {
            return null;
        }

        @Override
        public boolean getWantsAllOnMoveCalls() {
            return false;
        }

        @Override
        public void setExtras(Bundle extras) {

        }

        @Override
        public Bundle getExtras() {
            return null;
        }

        @Override
        public Bundle respond(Bundle extras) {
            return null;
        }
    }

    private static class StubDatabaseStatement implements DatabaseStatement {

        @Override
        public void execute() {
        }

        @Override
        public long simpleQueryForLong() {
            return 0;
        }

        @Override
        public void bindNull(int index) {
        }

        @Override
        public long executeInsert() {
            return -1;
        }

        @Override
        public void bindString(int index, String value) {
        }

        @Override
        public void bindBlob(int index, byte[] value) {
        }

        @Override
        public void bindLong(int index, long value) {
        }

        @Override
        public void clearBindings() {
        }

        @Override
        public void bindDouble(int index, double value) {
        }

        @Override
        public void close() {
        }
    }

}
