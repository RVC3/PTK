package ru.ppr.chit.api;

import android.support.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Подсовывает в запросы данные разные данные, требуемые БС
 *
 * @author Dmitry Nevolin
 */
public class DeviceIdInterceptor implements Interceptor {

    private static final String HEADER_DEVICE_ID = "DeviceId";

    private final long deviceId;

    @Inject
    DeviceIdInterceptor(@Named("deviceId") long deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .addHeader(HEADER_DEVICE_ID, String.valueOf(deviceId))
                .build();
        return chain.proceed(request);
    }

}
