package ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.network;

import io.reactivex.Single;
import retrofit2.http.GET;

/**
 * API для получения данных для первичной авторизации,
 * используется только для дебага, в обычной ситуации
 * эти данные получаются из QR-кода
 *
 * @author Dmitry Nevolin
 */
public interface AuthInfoApi {

    /**
     * Запрашивает данные для первичной авторизации
     *
     * @return данные для первичной авторизации
     */
    @GET("terminaladministration/getauthenticationdata")
    Single<AuthInfoEntity> getTerminalAuthenticationData();

}
