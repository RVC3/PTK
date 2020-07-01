package ru.ppr.chit.api.auth;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import ru.ppr.chit.domain.model.local.OAuth2Token;

/**
 * Выполняет авторизацию (переавторизацию) по требованию сервера
 *
 * @author Dmitry Nevolin
 */
public class OAuth2Authenticator implements Authenticator {

    private final OAuth2TokenProvider oAuth2TokenProvider;

    @Inject
    OAuth2Authenticator(OAuth2TokenProvider oAuth2TokenProvider) {
        this.oAuth2TokenProvider = oAuth2TokenProvider;
    }

    @Nullable
    @Override
    public Request authenticate(@NonNull Route route, @NonNull Response response) throws IOException {

        // получаем текущий токен авторизации
        OAuth2Token token = oAuth2TokenProvider.getToken();

        // Если токена еще нет, то запрашиваем его
        if (token == null){
            token = oAuth2TokenProvider.createToken();
        } else
        // Если валидный токен есть и в ответе пришла ошибка авторизации, то пытаемся обновить токен
        if (!token.isBroken() && response.code() == OAuth2TokenProvider.AUTHENTICATE_ERROR_CODE) {
            // Если в заголовке уже есть этот access_token, то значит истекло время действия старого токена и его надо обновить
            Pair<String, String> authorizationHeaderPair = OAuth2TokenHelper.getAuthorizationHeaderPairFromToken(token);
            if (authorizationHeaderPair.second.equals(response.request().header(authorizationHeaderPair.first))) {
                token = oAuth2TokenProvider.refreshToken(token);
            }
        }

        // Если токена нет или он битый, то сразу возвращаем null и не отправляем сетевой запрос
        if (token == null || token.isBroken()){
            return null;
        }

        Pair<String, String> authorizationHeaderPair = OAuth2TokenHelper.getAuthorizationHeaderPairFromToken(token);
        return response.request().newBuilder()
                .header(authorizationHeaderPair.first, authorizationHeaderPair.second)
                .build();
    }

}
