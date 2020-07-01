package ru.ppr.cppk.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Артем on 17.03.2016.
 */
// В будущем: 17.03.2016 Добавить документацию
public class SqlQueryBuilder {

    private static final String SPACE = " ";
    private static final String SELECT = "SELECT";
    private static final String FROM = "FROM";
    private static final String WHERE = "WHERE";
    private static final String ORDER_BY = "ORDER BY";
    private static final String JOIN = "JOIN";
    private static final String ON = "ON";
    private static final String DESC = "DESC";
    private static final String ASC = "ASC";
    private static final String AND = "AND";
    private static final String OR = "OR";
    private static final String EQUALS = "=";
    private static final String LIMIT = "LIMIT";
    private static final String IS_NULL = "IS NULL";

    private final StringBuilder builder;
    private boolean alreadyBuild = false;

    private SqlQueryBuilder() {
        builder = new StringBuilder();
    }

    public static SqlQueryBuilder newBuilder() {
        return new SqlQueryBuilder();
    }

    public SqlQueryBuilder select(@Nullable String columns) {

        if (!alreadyBuild) {
            builder.append(SELECT).append(SPACE).append(columns != null ? columns : "*").append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder selectAll() {
        return select(null);
    }

    public SqlQueryBuilder from(@NonNull String tableName) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(FROM).append(SPACE).append(tableName).append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder where(@NonNull String where) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(WHERE).append(SPACE).append(where).append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder whereEquals(@NonNull String key, @NonNull Object value) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(WHERE).append(SPACE).append(key).append("=")
                    .append(convertObjetToString(value)).append("").append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder whereEqualsOrLess(@NonNull String key, @NonNull Object value) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(WHERE).append(SPACE).append(key).append("<=")
                    .append(convertObjetToString(value)).append("").append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder whereEqualsOrLarger(@NonNull String key, @NonNull Object value) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(WHERE).append(SPACE).append(key).append(">=")
                    .append(convertObjetToString(value)).append("").append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder whereLess(@NonNull String key, @NonNull Object value) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(WHERE).append(SPACE).append(key).append("<")
                    .append(convertObjetToString(value)).append("").append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder whereLarger(@NonNull String key, @NonNull Object value) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(WHERE).append(SPACE).append(key).append(">")
                    .append(convertObjetToString(value)).append("").append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder whereIsNull(@NonNull String key) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(WHERE).append(SPACE).append(key).append(IS_NULL).append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder join(@NonNull String join) {

        if (!alreadyBuild) {
            builder.append(SPACE).append(JOIN).append(SPACE).append(join).append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder onEquals(@NonNull String onFirst, @NonNull String onSecond) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(ON).append(SPACE).append(onFirst).append(EQUALS)
                    .append(onSecond).append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder orderBy(@NonNull String orderBy) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(ORDER_BY).append(SPACE).append(orderBy).append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder desc() {
        if (!alreadyBuild) {
            builder.append(SPACE).append(DESC);
        }
        return this;
    }

    public SqlQueryBuilder asc() {
        if (!alreadyBuild) {
            builder.append(SPACE).append(ASC);
        }
        return this;
    }

    public SqlQueryBuilder and(@NonNull String and) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(AND).append(SPACE).append("(").append(and).append(")").append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder andEqualsOrLess(@NonNull String key, @NonNull Object value) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(AND).append(SPACE).append(key).append("<=")
                    .append(convertObjetToString(value)).append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder andEquals(String key, Object value) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(AND).append(SPACE).append(key);
            if (value == null)
                builder.append(SPACE).append(IS_NULL);
            else
                builder.append("=").append(convertObjetToString(value));
            builder.append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder append(String query) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(query).append(SPACE);
        }
        return this;
    }


    public SqlQueryBuilder or(@NonNull String or) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(OR).append(SPACE).append(or).append(SPACE);
        }
        return this;
    }

    public SqlQueryBuilder limit(int limit) {
        if (!alreadyBuild) {
            builder.append(SPACE).append(LIMIT).append(SPACE).append(limit);
        }
        return this;
    }

    public String buildQuery() {
        alreadyBuild = true;
        return builder.toString();
    }

    private String convertObjetToString(@NonNull Object object) {
        String valueStr;
        if (object instanceof String) {
            valueStr = "'" + object + "'";
        } else {
            valueStr = object.toString();
        }
        return valueStr;
    }

    @Override
    public String toString() {
        return builder.toString();
    }


}
