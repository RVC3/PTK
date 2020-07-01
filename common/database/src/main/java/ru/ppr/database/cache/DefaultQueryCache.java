package ru.ppr.database.cache;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import ru.ppr.logger.Logger;

/**
 * Стандартная реализация кеша запросов к БД.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultQueryCache implements QueryCache {

    private static final String TAG = Logger.makeLogTag(DefaultQueryCache.class);

    private static final int MAX_CACHE_SIZE = 20;

    private final HashMap<Key, Value> map = new HashMap<>(MAX_CACHE_SIZE);

    private final Object LOCK = new Object();

    @Override
    public Object get(@NonNull Key key) {
        Value value = map.get(key);
        Logger.trace(TAG, "getFromCache, key = " + key + ", value = " + value);
        if (value == null) {
            return null;
        }

        synchronized (LOCK) {
            value.setAccessCount(value.getAccessCount() + 1);
            value.setLastAccessTime(SystemClock.uptimeMillis());
            return value.getValue();
        }
    }

    @Override
    public void put(@NonNull Key key, Value value) {
        Logger.trace(TAG, "putToCache, key = " + key + ", value = " + value);
        value.setLastAccessTime(SystemClock.uptimeMillis());

        synchronized (LOCK) {
            if (map.size() == MAX_CACHE_SIZE) {
                Map.Entry<Key, Value> minEntry = Collections.min(map.entrySet(), entryComparator);
                map.remove(minEntry.getKey());
                Logger.trace(TAG, "clearing cache, deletedKey = " + minEntry.getKey() + ", deletedValue = " + minEntry.getValue());
            }
            map.put(key, value);
        }
    }

    @Override
    public Object remove(@NonNull Key key) {
        synchronized (LOCK) {
            Value value = map.remove(key);
            Logger.trace(TAG, "removeFromCache, key = " + key + ", value = " + value);
            return value == null ? null : value.getValue();
        }
    }

    private final Comparator<Map.Entry<Key, Value>> entryComparator = new Comparator<Map.Entry<Key, Value>>() {
        @Override
        public int compare(Map.Entry<Key, Value> entry1, Map.Entry<Key, Value> entry2) {
            Value value1 = entry1.getValue();
            Value value2 = entry2.getValue();
            int accessCountDiff = value1.getAccessCount() - value2.getAccessCount();
            if (accessCountDiff == 0) {
                long lastAccessTimeDiff = value1.getLastAccessTime() - value2.getLastAccessTime();
                return (int) lastAccessTimeDiff;
            } else {
                return accessCountDiff;
            }
        }
    };
}
