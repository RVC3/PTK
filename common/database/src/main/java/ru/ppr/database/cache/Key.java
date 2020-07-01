package ru.ppr.database.cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

/**
 * Ключ элемента кеша.
 *
 * @author Aleksandr Brazhkin
 */
public class Key {

    public static Key create(@NonNull String queryName, @Nullable Object... queryParams) {
        return new Key(queryName, queryParams == null ? new Object[0] : queryParams);
    }

    /**
     * Наименование запроса
     */
    @NonNull
    private final String queryName;
    /**
     * Параметры запроса
     */
    @NonNull
    private final Object[] queryParams;

    private Key(@NonNull String queryName, @NonNull Object... queryParams) {
        this.queryName = queryName;
        this.queryParams = queryParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (!queryName.equals(key.queryName)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(queryParams, key.queryParams);

    }

    @Override
    public int hashCode() {
        int result = queryName.hashCode();
        result = 31 * result + Arrays.hashCode(queryParams);
        return result;
    }

    @Override
    public String toString() {
        return "Key{" +
                "queryName='" + queryName + '\'' +
                ", queryParams=" + Arrays.toString(queryParams) +
                '}';
    }
}
