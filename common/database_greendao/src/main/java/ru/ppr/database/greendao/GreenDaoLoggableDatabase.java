package ru.ppr.database.greendao;

import android.support.annotation.NonNull;

import ru.ppr.database.AbstractLoggableDatabase;

/**
 * @author Aleksandr Brazhkin
 */
public class GreenDaoLoggableDatabase extends AbstractLoggableDatabase<GreenDaoDatabaseStatement> implements GreenDaoDatabase {

    private final GreenDaoDatabase delegate;

    public GreenDaoLoggableDatabase(@NonNull GreenDaoDatabase delegate, @NonNull String loggingName) {
        super(delegate, loggingName);
        this.delegate = delegate;
    }

    @Override
    protected GreenDaoDatabaseStatement compileStatementInternal(String sql) {
        return delegate.compileStatement(sql);
    }

    @Override
    public Object getRawDatabase() {
        return delegate;
    }
}
