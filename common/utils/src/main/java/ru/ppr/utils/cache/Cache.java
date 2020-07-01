package ru.ppr.utils.cache;

/**
 * Кеш.
 *
 * @param <K> Тип ключа
 * @param <V> Тип значения
 * @author Aleksandr Brazhkin
 */
public interface Cache<K, V> {
    /**
     * Возвращает значение по ключу
     *
     * @param key Ключ
     * @return Значение из кеша
     */
    V get(K key);

    /**
     * Кладет значение в кеш
     *
     * @param key   Ключ
     * @param value Значение
     */
    void put(K key, V value);
}
