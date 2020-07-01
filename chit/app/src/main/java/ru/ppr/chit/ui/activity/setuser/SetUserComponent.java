package ru.ppr.chit.ui.activity.setuser;

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
interface SetUserComponent extends ActivityComponent {

    SetUserPresenter setUserPresenter();

    void inject(SetUserActivity setUserActivity);

}
