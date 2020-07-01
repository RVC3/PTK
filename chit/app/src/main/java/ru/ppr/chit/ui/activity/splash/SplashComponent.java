package ru.ppr.chit.ui.activity.splash;

import dagger.Component;
import ru.ppr.chit.AppComponent;
import ru.ppr.chit.di.ActivityScope;
import ru.ppr.chit.ui.activity.base.ActivityComponent;
import ru.ppr.chit.ui.activity.base.ActivityModule;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
interface SplashComponent extends ActivityComponent {
    SplashPresenter splashPresenter();

    void inject(SplashActivity splashActivity);
}
