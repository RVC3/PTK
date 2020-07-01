package ru.ppr.utils.cache;

/**
 * Стабовая релизация кеша.
 * По факту кеширование не осуществляется.
 *
 * @author Aleksandr Brazhkin
 */
public class StubCache<K, V> implements Cache<K, V> {

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public void put(K key, V value) {

    }
}
