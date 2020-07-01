package ru.ppr.chit.ui.activity.workingstate;

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
interface WorkingStateComponent extends ActivityComponent {

    WorkingStatePresenter workingStatePresenter();

    void inject(WorkingStateActivity workingStateActivity);

}
