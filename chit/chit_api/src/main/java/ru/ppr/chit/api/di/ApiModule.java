package ru.ppr.chit.api.di;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.ppr.chit.api.Api;
import ru.ppr.chit.api.ApiFactory;
import ru.ppr.chit.api.ApiType;
import ru.ppr.chit.api.auth.OAuth2Api;
import ru.ppr.chit.api.auth.OAuth2ApiFactory;
import ru.ppr.chit.api.auth.OAuth2TokenStorage;
import ru.ppr.chit.domain.model.local.AuthInfo;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
@Singleton
@Module
public class ApiModule {

    private static final String TAG = Logger.makeLogTag(ApiModule.class);

    private final ApiType apiType;
    private final AuthInfo authInfo;
    private final OAuth2TokenStorage oAuth2TokenStorage;
    private final long deviceId;

    public ApiModule(ApiType apiType,
                     AuthInfo authInfo,
                     OAuth2TokenStorage oAuth2TokenStorage,
                     long deviceId) {
        this.apiType = apiType;
        this.authInfo = authInfo;
        this.oAuth2TokenStorage = oAuth2TokenStorage;
        this.deviceId = deviceId;

        Logger.info(TAG, "new ApiModule(" +
                "apiType = " + this.apiType +
                ", authInfo = " + this.authInfo +
                ", deviceId = " + this.deviceId + ")");
    }

    @Provides
    ApiType apiType() {
        return apiType;
    }

    @Provides
    AuthInfo authInfo() {
        return authInfo;
    }

    @Provides
    OAuth2TokenStorage oAuth2TokenStorage() {
        return oAuth2TokenStorage;
    }

    @Provides
    OAuth2Api oAuth2Api(OAuth2ApiFactory factory) {
        return factory.create();
    }

    @Provides
    Api api(ApiFactory factory) {
        return factory.create();
    }

    @Named("deviceId")
    @Provides
    long deviceId() {
        return deviceId;
    }

}
