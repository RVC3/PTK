package ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.network;

import dagger.Module;
import dagger.Provides;

/**
 * Di-модуль для {@link NetworkAuthInfoReader}.
 *
 * @author Aleksandr Brazhkin
 */
@Module
public class NetworkAuthInfoModule {

    @Provides
    AuthInfoApi authInfoApi(AuthInfoApiFactory authInfoApiFactory) {
        return authInfoApiFactory.create();
    }

}
