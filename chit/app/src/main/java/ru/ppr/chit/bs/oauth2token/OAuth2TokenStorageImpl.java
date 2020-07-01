package ru.ppr.chit.bs.oauth2token;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.chit.api.auth.OAuth2TokenStorage;
import ru.ppr.chit.domain.model.local.OAuth2Token;
import ru.ppr.chit.domain.repository.local.OAuth2TokenRepository;

/**
 * Релизация по-умолчанию для {@link OAuth2TokenStorage}
 * для сохранения/загрузки использует локальную БД
 *
 * @author Dmitry Nevolin
 */
public class OAuth2TokenStorageImpl implements OAuth2TokenStorage {

    private final OAuth2TokenManager oAuth2TokenManager;

    public OAuth2TokenStorageImpl(OAuth2TokenManager oAuth2TokenManager) {
        this.oAuth2TokenManager = oAuth2TokenManager;
    }

    @Nullable
    @Override
    public OAuth2Token load() {
        return oAuth2TokenManager.load();
    }

    @Override
    public void save(@NonNull OAuth2Token token) {
        oAuth2TokenManager.save(token);
    }

    @Override
    public void clear() {
        oAuth2TokenManager.clear();
    }

}
