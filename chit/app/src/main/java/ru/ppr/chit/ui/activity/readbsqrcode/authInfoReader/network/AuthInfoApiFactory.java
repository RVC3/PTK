package ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.network;

import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.ppr.chit.api.retrofit.RetrofitApiFactory;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
class AuthInfoApiFactory {

    private static final String TAG = Logger.makeLogTag(AuthInfoApiFactory.class);

    private static final String BASE_URL = "http://25.32.102.127:6102/";
    private static final long CONNECT_TIMEOUT = 4;
    private static final long READ_TIMEOUT = 4;

    @Inject
    AuthInfoApiFactory() {

    }

    public AuthInfoApi create() {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .create()))
                .client(new OkHttpClient.Builder()
                        .addInterceptor(new HttpLoggingInterceptor(message -> Logger.trace(TAG, message))
                                .setLevel(HttpLoggingInterceptor.Level.BODY))
                        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                        .build())
                .baseUrl(BASE_URL)
                .build();
        return retrofit.create(AuthInfoApi.class);
    }

}
