package ru.ppr.chit.api;

import javax.inject.Inject;

import ru.ppr.chit.api.retrofit.RetrofitApiFactory;
import ru.ppr.chit.api.stub.StubApi;

/**
 * Фабрика для {@link Api}.
 *
 * @author Dmitry Nevolin
 */
public class ApiFactory {

    private final ApiType apiType;
    private final RetrofitApiFactory retrofitApiFactory;

    @Inject
    ApiFactory(ApiType apiType, RetrofitApiFactory retrofitApiFactory) {
        this.apiType = apiType;
        this.retrofitApiFactory = retrofitApiFactory;
    }

    public Api create() {
        switch (apiType) {
            case STUB:
                return new StubApi();
            case RETROFIT:
                return retrofitApiFactory.create();
            default:
                throw new IllegalArgumentException("Incorrect apiType: " + apiType);
        }
    }

}
