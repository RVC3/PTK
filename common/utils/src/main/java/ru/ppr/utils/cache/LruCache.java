package ru.ppr.utils.cache;

/**
 * Реализация кеша LRU.
 *
 * @author Aleksandr Brazhkin
 */
public class LruCache<K, V> implements Cache<K, V> {

    private final android.util.LruCache<K, V> lruCache;

    public LruCache(int maxSize) {
        lruCache = new android.util.LruCache<>(maxSize);
    }

    @Override
    public V get(K key) {
        return lruCache.get(key);
    }

    @Override
    public void put(K key, V value) {
        lruCache.put(key, value);
    }
}
