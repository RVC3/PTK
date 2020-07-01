package ru.ppr.core.domain.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Хранилище ключ-значение.
 *
 * @author Aleksandr Brazhkin
 */
public interface KeyValueStorage {
    @Nullable
    String getValue(@NonNull String key);

    @Nullable
    String getValue(@NonNull String key, @Nullable String defaultValue);

    void setValue(@NonNull String key, @Nullable String value);
}
