package ru.ppr.chit.api.auth;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * API для получения токена авторизации
 *
 * @author Dmitry Nevolin
 */
public interface OAuth2Api {

    /**
     * Загружает сущность токета авторизации
     *
     * @param params параметры запроса
     * @return сущность токена авторизации
     */
    @FormUrlEncoded
    @POST("oauth2/token")
    Call<OAuth2TokenEntity> oAuth2Token(@FieldMap Map<String, String> params);

}
