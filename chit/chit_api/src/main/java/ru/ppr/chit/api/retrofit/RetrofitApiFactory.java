package ru.ppr.chit.api.retrofit;

import android.util.Base64;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.ppr.chit.api.DeviceIdInterceptor;
import ru.ppr.chit.api.auth.OAuth2Authenticator;
import ru.ppr.chit.api.auth.OAuth2Interceptor;
import ru.ppr.chit.domain.model.local.AuthInfo;
import ru.ppr.logger.Logger;

/**
 * Фабрика для {@link RetrofitApi}.
 *
 * @author Dmitry Nevolin
 */
public class RetrofitApiFactory {

    private static final String TAG = Logger.makeLogTag(RetrofitApiFactory.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";
    private static final long CONNECT_TIMEOUT = 1;
    private static final long READ_TIMEOUT = 1;

    private final AuthInfo authInfo;
    private final OAuth2Authenticator oAuth2Authenticator;
    private final OAuth2Interceptor oAuth2Interceptor;
    private final DeviceIdInterceptor deviceIdInterceptor;

    @Inject
    RetrofitApiFactory(AuthInfo authInfo,
                       OAuth2Authenticator oAuth2Authenticator,
                       OAuth2Interceptor oAuth2Interceptor,
                       DeviceIdInterceptor deviceIdInterceptor) {
        this.authInfo = authInfo;
        this.oAuth2Authenticator = oAuth2Authenticator;
        this.oAuth2Interceptor = oAuth2Interceptor;
        this.deviceIdInterceptor = deviceIdInterceptor;
    }

    /**
     * Создаёт объект {@link RetrofitApi}
     *
     * @return объект {@link RetrofitApi}
     */
    public RetrofitApi create() {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        // убираем ограничение на expose и ставим данную стратегию нейминга,
                        // чтобы не аннотировать все поля всех реквестов/респонсов/моделей
                        .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                        .setDateFormat(DATE_FORMAT)
                        .registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter())
                        .create()))
                .client(new OkHttpClient.Builder()
                        .authenticator(oAuth2Authenticator)
                        .addInterceptor(oAuth2Interceptor)
                        .addInterceptor(deviceIdInterceptor)
                        .addInterceptor(new HttpLoggingInterceptor(message -> Logger.trace(TAG, message))
                                .setLevel(HttpLoggingInterceptor.Level.BODY))
                        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MINUTES)
                        .readTimeout(READ_TIMEOUT, TimeUnit.MINUTES)
                        .build())
                .baseUrl(authInfo.getBaseUri())
                .build();
        return new RetrofitApi(retrofit.create(RetrofitPerformer.class));
    }

    /**
     * Конвертер массива байтов в Base64 и обратно, для БС это формат по-умолчанию.
     */
    private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

        @Override
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.decode(json.getAsString(), Base64.NO_WRAP);
        }

        @Override
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.encodeToString(src, Base64.NO_WRAP));
        }

    }

}
