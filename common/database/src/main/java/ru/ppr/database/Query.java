package ru.ppr.database;

import android.database.Cursor;
import android.support.annotation.Nullable;

/**
 * SQL-запрос к БД.
 *
 * @author Aleksandr Brazhkin
 */
public class Query {

    /**
     * Текст запроса
     */
    private final String sql;
    /**
     * Аргументы запроса
     */
    @Nullable
    private final Object[] args;

    Query(String sql, @Nullable Object[] args) {
        this.sql = sql;
        this.args = args;
    }

    private String getSql() {
        return sql;
    }

    @Nullable
    private Object[] getArgs() {
        return args;
    }

    @Nullable
    private String[] getStringArgs() {
        if (args == null) {
            return null;
        }
        String[] stringArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            stringArgs[i] = String.valueOf(args[i]);
        }
        return stringArgs;
    }

    public Cursor run(Database database) {
        return database.rawQuery(getSql(), getStringArgs());
    }

    public void logExplainQueryPlan(Database database) {
        SqLiteUtils.logExplainQueryPlan(database, getSql(), getStringArgs());
    }

    public void logQuery() {
        SqLiteUtils.logQuery(getSql(), getArgs());
    }
}
