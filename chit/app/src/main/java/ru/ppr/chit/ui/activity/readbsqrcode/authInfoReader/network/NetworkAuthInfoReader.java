package ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.network;

import javax.inject.Inject;

import io.reactivex.Single;
import ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.AuthInfoReader;
import ru.ppr.chit.domain.model.local.AuthInfo;

/**
 * Провайдер аутентификационных данных, получающий данные из сети.
 *
 * @author Dmitry Nevolin
 */
public class NetworkAuthInfoReader implements AuthInfoReader {

    private final AuthInfoApi api;

    @Inject
    NetworkAuthInfoReader(AuthInfoApi api) {
        this.api = api;
    }

    @Override
    public Single<AuthInfo> readAuthInfo() {
        return api.getTerminalAuthenticationData()
                .map(authInfoEntity -> {
                    AuthInfo authInfo = AuthInfoMapper.INSTANCE.entityToModel(authInfoEntity);
                    // В будущем пока мы работаем через впн оставляем хардкод, потом убрать
                    authInfo.setBaseUri("http://25.32.102.127:6102/");
                    return authInfo;
                });
    }


}
