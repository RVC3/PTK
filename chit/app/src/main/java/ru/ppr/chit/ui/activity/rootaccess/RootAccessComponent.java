package ru.ppr.chit.ui.activity.rootaccess;

import dagger.Component;
import ru.ppr.chit.AppComponent;
import ru.ppr.chit.di.ActivityScope;
import ru.ppr.chit.ui.activity.base.ActivityComponent;
import ru.ppr.chit.ui.activity.base.ActivityModule;

/**
 * @author Dmitry Nevolin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = {ActivityModule.class})
interface RootAccessComponent extends ActivityComponent {

    RootAccessPresenter rootAccessPresenter();

    void inject(RootAccessActivity rootAccessActivity);

}
