package ru.ppr.chit.ui.activity.menu;

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
interface MenuComponent extends ActivityComponent {

    MenuPresenter menuPresenter();

    void inject(MenuActivity menuActivity);

}
