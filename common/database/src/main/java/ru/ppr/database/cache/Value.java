package ru.ppr.database.cache;

import android.support.annotation.Nullable;

import java.util.Date;

/**
 * Значение элемента кеша.
 *
 * @author Aleksandr Brazhkin
 */
public class Value {

    public static Value create(@Nullable Object value) {
        return Value.create(value, null);
    }

    public static Value create(@Nullable Object value, @Nullable Date expireTime) {
        return new Value(value, expireTime);
    }

    /**
     * Значение
     */
    @Nullable
    private final Object value;
    /**
     * Срок действия
     */
    @Nullable
    private final Date expireTime;
    /**
     * Количество чтений значения из кеша
     */
    private int accessCount = 0;
    /**
     * Время последнего чтения значения из кеша
     */
    private long lastAccessTime;

    private Value(@Nullable Object value, @Nullable Date expireTime) {
        this.expireTime = expireTime;
        this.value = value;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    @Nullable
    public Date getExpireTime() {
        return expireTime;
    }

    int getAccessCount() {
        return accessCount;
    }

    void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }

    long getLastAccessTime() {
        return lastAccessTime;
    }

    void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @Override
    public String toString() {
        return "Value{" +
                "expireTime=" + expireTime +
                ", accessCount=" + accessCount +
                ", lastAccessTime=" + lastAccessTime +
                '}';
    }
}
