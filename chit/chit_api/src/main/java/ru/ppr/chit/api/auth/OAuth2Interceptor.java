package ru.ppr.chit.api.auth;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import ru.ppr.chit.domain.model.local.OAuth2Token;

/**
 * Подсовывает в запросы данные об авторизации
 *
 * @author Dmitry Nevolin
 */
public class OAuth2Interceptor implements Interceptor {

    private final OAuth2TokenProvider oAuth2TokenProvider;

    @Inject
    OAuth2Interceptor(OAuth2TokenProvider oAuth2TokenProvider) {
        this.oAuth2TokenProvider = oAuth2TokenProvider;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        final Request.Builder requestBuilder = chain.request().newBuilder();

        OAuth2Token token = oAuth2TokenProvider.getToken();
        if (token != null) {
            Pair<String, String> authorizationHeaderPair = OAuth2TokenHelper.getAuthorizationHeaderPairFromToken(token);
            requestBuilder.addHeader(authorizationHeaderPair.first, authorizationHeaderPair.second);
        }

        return chain.proceed(requestBuilder.build());
    }

}
