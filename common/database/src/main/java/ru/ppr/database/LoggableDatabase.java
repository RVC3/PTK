package ru.ppr.database;

import android.support.annotation.NonNull;

/**
 * @author Aleksandr Brazhkin
 */
public class LoggableDatabase extends AbstractLoggableDatabase<DatabaseStatement> {

    private final Database delegate;

    public LoggableDatabase(@NonNull Database delegate, @NonNull String loggingName) {
        super(delegate, loggingName);
        this.delegate = delegate;
    }

    @Override
    protected DatabaseStatement compileStatementInternal(String sql) {
        return delegate.compileStatement(sql);
    }
}
