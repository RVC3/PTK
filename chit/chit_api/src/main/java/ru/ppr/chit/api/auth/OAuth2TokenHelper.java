package ru.ppr.chit.api.auth;

import android.util.Pair;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import ru.ppr.chit.domain.model.local.OAuth2Token;

/**
 * Класс-помощник для работы с данными о токене авторизации
 *
 * @author Dmitry Nevolin
 */
class OAuth2TokenHelper {

    private static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Возвращает пару для заголовка Authorization,
     * first - название заголовка
     * second - значение заголовка
     *
     * @param token токен аутентификации
     * @return пару для заголовка Authorization
     */
    static Pair<String, String> getAuthorizationHeaderPairFromToken(@NonNull OAuth2Token token) {
        return Pair.create(HEADER_AUTHORIZATION, token.getTokenType() + " " + token.getAccessToken());
    }

}
