package ru.ppr.cppk.di;

import ru.ppr.cppk.AppComponent;

/**
 * @author Aleksandr Brazhkin
 */
public class Dagger {

    private static AppComponent appComponent;

    public static AppComponent appComponent() {
        return appComponent;
    }

    public static void setAppComponent(AppComponent appComponent) {
        Dagger.appComponent = appComponent;
    }
}
