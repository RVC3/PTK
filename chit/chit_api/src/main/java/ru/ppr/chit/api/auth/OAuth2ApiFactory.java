package ru.ppr.chit.api.auth;

import com.google.gson.GsonBuilder;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.ppr.chit.api.DeviceIdInterceptor;
import ru.ppr.chit.domain.model.local.AuthInfo;
import ru.ppr.logger.Logger;

/**
 * Фабрика для {@link OAuth2Api}
 *
 * @author Dmitry Nevolin
 */
public class OAuth2ApiFactory {

    private static final String TAG = Logger.makeLogTag(OAuth2ApiFactory.class);

    private final AuthInfo authInfo;
    private final DeviceIdInterceptor deviceIdInterceptor;

    @Inject
    OAuth2ApiFactory(AuthInfo authInfo,
                     DeviceIdInterceptor deviceIdInterceptor) {
        this.authInfo = authInfo;
        this.deviceIdInterceptor = deviceIdInterceptor;
    }

    public OAuth2Api create() {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .create()))
                .client(new OkHttpClient.Builder()
                        .addInterceptor(deviceIdInterceptor)
                        .addInterceptor(new HttpLoggingInterceptor(message -> Logger.trace(TAG, message))
                                .setLevel(HttpLoggingInterceptor.Level.BODY))
                        .build())
                .baseUrl(authInfo.getBaseUri())
                .build();
        return retrofit.create(OAuth2Api.class);
    }

}
