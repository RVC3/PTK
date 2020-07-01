package ru.ppr.core.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ru.ppr.logger.Logger;

/**
 * Обертка над {@link Properties}, предоставляющая типизированные методы получения и установки значения.
 *
 * @author Aleksandr Brazhkin
 */
public class TypedProperties implements Properties {

    private static final String TAG = Logger.makeLogTag(TypedProperties.class);

    private final Properties properties;

    public TypedProperties(Properties properties) {
        this.properties = properties;
    }

    @Nullable
    @Override
    public String getValue(@NonNull String key) {
        return properties.getValue(key);
    }

    @Nullable
    @Override
    public String getValue(@NonNull String key, @Nullable String defaultValue) {
        return properties.getValue(key, defaultValue);
    }

    @NonNull
    @Override
    public HashMap<String, String> getValues() {
        return properties.getValues();
    }

    @Override
    public void setValue(@NonNull String key, @Nullable String value) {
        properties.setValue(key, value);
    }

    @Override
    public void setValues(@NonNull Map<String, String> values) {
        properties.setValues(values);
    }

    @Override
    public void clearValue(@NonNull String key) {
        properties.clearValue(key);
    }

    @Override
    public void clear() {
        properties.clear();
    }

    @Override
    public String toString() {
        return "TypedProperties{" +
                "properties=" + properties +
                '}';
    }

    /**
     * Возвращает значение свойства по ключу
     *
     * @param key          Ключ
     * @param defaultValue Значение по умолчанию
     * @return Значение
     */
    public String getString(@NonNull String key, @Nullable String defaultValue) {
        return getValue(key, defaultValue);
    }

    /**
     * Устанавливает значение по ключу
     *
     * @param key   Ключ
     * @param value Значение
     */
    public void setString(@NonNull String key, @Nullable String value) {
        setValue(key, value);
    }

    /**
     * Возвращает значение свойства по ключу
     *
     * @param key          Ключ
     * @param defaultValue Значение по умолчанию
     * @return Значение
     */
    public Integer getInteger(@NonNull String key, @Nullable Integer defaultValue) {
        String rawValue = properties.getValue(key);

        if (rawValue == null) {
            return defaultValue;
        }

        try {
            return Integer.valueOf(rawValue);
        } catch (NumberFormatException e) {
            Logger.error(TAG, e);
        }

        return defaultValue;
    }

    /**
     * Устанавливает значение по ключу
     *
     * @param key   Ключ
     * @param value Значение
     */
    public void setInteger(@NonNull String key, @Nullable Integer value) {
        if (value == null) {
            properties.setValue(key, null);
        } else {
            properties.setValue(key, String.valueOf(value));
        }
    }

    /**
     * Возвращает значение свойства по ключу
     *
     * @param key          Ключ
     * @param defaultValue Значение по умолчанию
     * @return Значение
     */
    public Long getLong(@NonNull String key, @Nullable Long defaultValue) {
        String rawValue = properties.getValue(key);

        if (rawValue == null) {
            return defaultValue;
        }

        try {
            return Long.valueOf(rawValue);
        } catch (NumberFormatException e) {
            Logger.error(TAG, e);
        }

        return defaultValue;
    }

    /**
     * Устанавливает значение по ключу
     *
     * @param key   Ключ
     * @param value Значение
     */
    public void setLong(@NonNull String key, @Nullable Long value) {
        if (value == null) {
            properties.setValue(key, null);
        } else {
            properties.setValue(key, String.valueOf(value));
        }
    }

    public UUID getUUID(@NonNull String key, @Nullable UUID defaultValue) {
        UUID value = defaultValue;
        String rawValue = properties.getValue(key);
        if (rawValue != null){
            try {
                value = UUID.fromString(rawValue);
            } catch (Exception e) {
                Logger.error(TAG, e);
            }
        }

        return value;
    }

    public void setUUID(@NonNull String key, @Nullable UUID value) {
        if (value == null) {
            properties.setValue(key, null);
        } else {
            properties.setValue(key, value.toString());
        }
    }

    /**
     * Возвращает значение свойства по ключу
     *
     * @param key          Ключ
     * @param defaultValue Значение по умолчанию
     * @return Значение
     */
    public Boolean getBoolean(@NonNull String key, @Nullable Boolean defaultValue) {
        String rawValue = properties.getValue(key);

        if (rawValue == null) {
            return defaultValue;
        }

        try {
            return Boolean.valueOf(rawValue);
        } catch (NumberFormatException e) {
            Logger.error(TAG, e);
        }

        return defaultValue;
    }

    /**
     * Устанавливает значение по ключу
     *
     * @param key   Ключ
     * @param value Значение
     */
    public void setBoolean(@NonNull String key, @Nullable Boolean value) {
        if (value == null) {
            properties.setValue(key, null);
        } else {
            properties.setValue(key, String.valueOf(value));
        }
    }

    /**
     * Возвращает значение свойства по ключу
     *
     * @param key          Ключ
     * @param defaultValue Значение по умолчанию
     * @return Значение
     */
    public List<Long> getLongList(@NonNull String key, @Nullable List<Long> defaultValue) {
        String rawValue = properties.getValue(key);

        if (rawValue == null) {
            return defaultValue;
        }

        if (rawValue.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> list = new ArrayList<>();
        String[] array = rawValue.split(",");
        for (String strValue : array) {
            Long value = null;
            try {
                value = Long.valueOf(strValue);
            } catch (NumberFormatException e) {
                Logger.error(TAG, e);
            }
            list.add(value);
        }

        return list;
    }

    /**
     * Устанавливает значение по ключу
     *
     * @param key   Ключ
     * @param value Значение
     */
    public void setLongList(@NonNull String key, @Nullable List<Long> value) {
        if (value == null) {
            properties.setValue(key, null);
        } else if (value.isEmpty()) {
            properties.setValue(key, "");
        } else {
            int iMax = value.size() - 1;

            StringBuilder sb = new StringBuilder();
            for (int i = 0; ; i++) {
                sb.append(value.get(i));
                if (i == iMax) {
                    properties.setValue(key, sb.toString());
                    return;
                }
                sb.append(",");
            }
        }
    }
}
