package ru.ppr.core.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс, предсавляющий настройки в формате ключ-значение.
 *
 * @author Aleksandr Brazhkin
 */
public interface Properties {
    /**
     * Возвращает значение свойства по ключу
     *
     * @param key Ключ
     * @return Значение
     */
    @Nullable
    String getValue(@NonNull String key);

    /**
     * Возвращает значение свойства по ключу
     *
     * @param key          Ключ
     * @param defaultValue Значение по умолчанию
     * @return Значение
     */
    @Nullable
    String getValue(@NonNull String key, @Nullable String defaultValue);

    /**
     * Возращает значения для всех ключей
     *
     * @return Значения для ключей
     */
    @NonNull
    HashMap<String, String> getValues();

    /**
     * Устанавливает значение по ключу
     *
     * @param key   Ключ
     * @param value Значение
     */
    void setValue(@NonNull String key, @Nullable String value);

    /**
     * Устанавливает значения для всех ключей из {@code values}.
     *
     * @param values Значения для ключей
     */
    void setValues(@NonNull Map<String, String> values);

    /**
     * Удаляет свойство по ключу
     *
     * @param key Ключ
     */
    void clearValue(@NonNull String key);

    /**
     * Удаляет все свойства
     */
    void clear();
}
