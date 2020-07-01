package ru.ppr.chit.api.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import retrofit2.Response;
import ru.ppr.chit.domain.model.local.AuthInfo;
import ru.ppr.chit.domain.model.local.OAuth2Token;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ObjectUtils;

/**
 * Предоставляет данные о токене авторизации
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class OAuth2TokenProvider {

    private static final String TAG = Logger.makeLogTag(OAuth2TokenProvider.class);

    public static final int AUTHENTICATE_ERROR_CODE = 401;
    public static final int AUTHENTICATE_BAD_REQUEST_CODE = 400; // Этот тип ошибки не относится к ошибке авторизации, но сейчас сервер возвращает такой ответ в случае refreshToken, которого нет на БС

    private static final String PARAM_CODE = "code";
    private static final String PARAM_REFRESH_TOKEN = "refresh_token";
    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_CLIENT_SECRET = "client_secret";
    private static final String PARAM_GRANT_TYPE = "grant_type";

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    private final AuthInfo authInfo;
    private final OAuth2Api api;
    private final OAuth2TokenStorage tokenStorage;
    private OAuth2Token savedToken;

    private boolean isLoaded = false;

    @Inject
    OAuth2TokenProvider(AuthInfo authInfo, OAuth2Api api, OAuth2TokenStorage tokenStorage) {
        this.authInfo = authInfo;
        this.api = api;
        this.tokenStorage = tokenStorage;
    }

    /**
     * Отдаёт данные о текущем токене авторизации
     *
     * @return данные о текущем токене авторизации
     */
    @Nullable
    synchronized OAuth2Token getToken() throws IOException {
        if (!isLoaded) {
            savedToken = tokenStorage.load();
            isLoaded = true;
            Logger.info(TAG, "getToken: " + savedToken);
        }
        // Если токен есть и он не битый, то проверяем, что не поменялся AuthInfo
        // Если поменялся, то помечаем токен как битый
        if (savedToken != null && !savedToken.isBroken() && !ObjectUtils.equals(authInfo.getId(), savedToken.getAuthInfoId()) ) {
            savedToken.setBroken(true);
            saveToken(savedToken);
        }
        return savedToken;
    }

    /**
     * Запрашивает новый токен по коду авторизации
     *
     * @return данные о новом токене авторизации
     * @throws IOException в случае ошибки работы с интернетом
     */
    @Nullable
    public synchronized OAuth2Token createToken() throws IOException {
        Logger.info(TAG, "createToken: ");

        // Запрашиваем новый токен по коду авторизации
        return saveToken(request(RequestParamsBuilder.fromAuthInfo(authInfo)));
    }

    /**
     * Запрашивает новый токен по старому токену
     *
     * @return данные о новом токене авторизации
     * @throws IOException в случае ошибки работы с интернетом
     */
    @Nullable
    public synchronized OAuth2Token refreshToken(@NonNull final OAuth2Token sourceToken) throws IOException {
        Logger.info(TAG, "refreshToken: " + sourceToken);
        // Запрашиваем новый токен по старому токену
        return saveToken(request(RequestParamsBuilder.fromToken(authInfo, sourceToken)));
    }

    /**
     * Сохраняет данные о токене
     * Токен может быть пустым, в этом случае функция ничего не сделает
     *
     * @return ВСЕГДА возвращает переданный токен
     */
    private OAuth2Token saveToken(final OAuth2Token token){
        if (token != null) {
            savedToken = token;
            savedToken.setAuthInfoId(authInfo.getId());
            tokenStorage.save(savedToken);
            if (savedToken.isBroken()){
                Logger.info(TAG, "markTokenAsBroken: " + savedToken);
            } else {
                Logger.info(TAG, "saveToken: " + savedToken);
            }
        }
        return token;
    }

    /**
     * Запрашивает у сервера новый токен с переданными параметрами
     * В случае ошибки авторизации ВСЕГДА должна вернуть битый токен !!!
     *
     * @param params параметры
     * @return данные о токене авторизации
     * @throws IOException в случае ошибки работы с интернетом
     */
    private @Nullable OAuth2Token request(Map<String, String> params) throws IOException {
        Response<OAuth2TokenEntity> oAuthTokenResponse = api.oAuth2Token(params).execute();
        // Если вернули ошибку авторизации, то помечаем токен как битый
        if (oAuthTokenResponse.code() == AUTHENTICATE_ERROR_CODE || oAuthTokenResponse.code() == AUTHENTICATE_BAD_REQUEST_CODE){
            // Если токена еще не было, то в случае ошибки авторизации создаем его, чтобы потом снова не проверять заведомо невалидную авторизацию БС
            if (savedToken == null) {
                savedToken = createBrokenToken();
            } else {
                savedToken.setBroken(true);
            }
            return savedToken;
        }
        // Здесь в случае каких то других ошибок (кроме ошибки авторизации) возвращаемвый токен может быть пустым,
        // в этом случае метод save() его не сохранит и оставит старый токен
        OAuth2TokenEntity oAuth2TokenEntity = oAuthTokenResponse.body();
        return OAuth2TokenMapper.INSTANCE.entityToModel(oAuth2TokenEntity);
    }

    // Создает пустой, битый токен
    private OAuth2Token createBrokenToken(){
        OAuth2Token token = new OAuth2Token();
        token.setId(new Long(0));
        token.setAccessToken("empty");
        token.setTokenType("empty");
        token.setRefreshToken("empty");
        token.setBroken(true);
        return token;
    }

    /**
     * Определяет актуальность токена по времени истечения действия токена
     *
     * @param token токен для определения его актуальности
     * @return true, если актуален, false в противном случае
     */
    private boolean isActual(OAuth2Token token) {
        // В будущем проверять время истечения токена
        return true;
    }

    static class RequestParamsBuilder {

        public static Map<String, String> fromAuthInfo(final AuthInfo authInfo) {
            Map<String, String> params = new HashMap<>();
            params.put(PARAM_CODE, authInfo.getAuthorizationCode());
            params.put(PARAM_CLIENT_ID, authInfo.getClientId());
            params.put(PARAM_CLIENT_SECRET, authInfo.getClientSecret());
            params.put(PARAM_GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
            return params;
        }

        private static Map<String, String> fromToken(final AuthInfo authInfo, final OAuth2Token token) throws IOException {
            Map<String, String> params = new HashMap<>();
            params.put(PARAM_REFRESH_TOKEN, token.getRefreshToken());
            params.put(PARAM_CLIENT_ID, authInfo.getClientId());
            params.put(PARAM_CLIENT_SECRET, authInfo.getClientSecret());
            params.put(PARAM_GRANT_TYPE, GRANT_TYPE_REFRESH_TOKEN);
            return params;
        }

    }

}
