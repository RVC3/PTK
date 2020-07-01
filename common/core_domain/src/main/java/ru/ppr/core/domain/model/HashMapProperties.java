package ru.ppr.core.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация {@link Properties}, хранящая даныне в {@link HashMap}.
 *
 * @author Aleksandr Brazhkin
 */
public class HashMapProperties implements Properties {
    /**
     * Структура данных для хранения значений свойств.
     */
    private final HashMap<String, String> map = new HashMap<>();

    @Nullable
    @Override
    public String getValue(@NonNull String key) {
        return map.get(key);
    }

    @Nullable
    @Override
    public String getValue(@NonNull String key, @Nullable String defaultValue) {
        String value = map.get(key);
        return value == null ? defaultValue : value;
    }

    @NonNull
    @Override
    public HashMap<String, String> getValues() {
        return new HashMap<>(map);
    }

    @Override
    public void setValue(@NonNull String key, @Nullable String value) {
        map.put(key, value);
    }

    @Override
    public void setValues(@NonNull Map<String, String> values) {
        map.putAll(values);
    }

    @Override
    public void clearValue(@NonNull String key) {
        map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        return "HashMapProperties" + map.toString();
    }
}
