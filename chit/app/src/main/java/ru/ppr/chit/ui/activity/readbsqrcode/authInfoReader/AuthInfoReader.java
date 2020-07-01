package ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader;

import io.reactivex.Single;
import ru.ppr.chit.domain.model.local.AuthInfo;

/**
 * Ридер аутентификационных данных.
 *
 * @author Dmitry Nevolin
 */
public interface AuthInfoReader {
    /**
     * Читает аутентификационные данные.
     *
     * @return Аутентификационные данные
     */
    Single<AuthInfo> readAuthInfo();

}
