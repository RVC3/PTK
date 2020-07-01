package ru.ppr.database;


import org.sqlite.database.sqlite.SQLiteStatement;

/**
 * @author Aleksandr Brazhkin
 */
public class NewSQLiteDatabaseStatement implements DatabaseStatement {

    private final SQLiteStatement delegate;

    NewSQLiteDatabaseStatement(SQLiteStatement delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute() {
        delegate.execute();
    }

    @Override
    public long simpleQueryForLong() {
        return delegate.simpleQueryForLong();
    }

    @Override
    public void bindNull(int index) {
        delegate.bindNull(index);
    }

    @Override
    public long executeInsert() {
        return delegate.executeInsert();
    }

    @Override
    public void bindString(int index, String value) {
        delegate.bindString(index, value);
    }

    @Override
    public void bindBlob(int index, byte[] value) {
        delegate.bindBlob(index, value);
    }

    @Override
    public void bindLong(int index, long value) {
        delegate.bindLong(index, value);
    }

    @Override
    public void clearBindings() {
        delegate.clearBindings();
    }

    @Override
    public void bindDouble(int index, double value) {
        delegate.bindDouble(index, value);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
