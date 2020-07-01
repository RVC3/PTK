package ru.ppr.chit.api.auth;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.local.OAuth2Token;

/**
 * Хранилище для {@link ru.ppr.chit.domain.model.local.OAuth2Token}
 *
 * @author Dmitry Nevolin
 */
public interface OAuth2TokenStorage {

    /**
     * Загружает сохранённый в хранилище токен
     *
     * @return сохранённый в хранилище токен
     */
    @Nullable
    OAuth2Token load();

    /**
     * Сохраняет токен в хранилище
     *
     * @param token токен для сохранения
     */
    void save(@NonNull OAuth2Token token);

    /**
     * Очищает хранилище
     */
    void clear();

}
