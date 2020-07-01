package ru.ppr.chit.di;

import ru.ppr.chit.AppComponent;
import ru.ppr.chit.api.di.ApiComponent;

/**
 * @author Dmitry Nevolin
 */
public class Dagger {

    private static AppComponent appComponent;
    private static ApiComponent apiComponent;

    public static AppComponent appComponent() {
        return appComponent;
    }

    public static void setAppComponent(AppComponent appComponent) {
        Dagger.appComponent = appComponent;
    }

    public static ApiComponent apiComponent() {
        return apiComponent;
    }

    public static void setApiComponent(ApiComponent apiComponent) {
        Dagger.apiComponent = apiComponent;
    }

}
