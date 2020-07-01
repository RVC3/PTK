package ru.ppr.database.cache;

import android.support.annotation.NonNull;

/**
 * Кеш запросов к БД.
 *
 * @author Aleksandr Brazhkin
 */
public interface QueryCache {

    /**
     * Получает значение из кеша по ключу
     *
     * @param key Ключ
     * @return Значение в кеше
     */
    Object get(@NonNull Key key);

    /**
     * Кладет значение в кеш
     *
     * @param key   Ключ
     * @param value Значение
     */
    void put(@NonNull Key key, Value value);

    /**
     * Удаляет значение из кеша по ключу
     *
     * @param key Ключ
     * @return Удаленное из кеша значение
     */
    Object remove(@NonNull Key key);

}
