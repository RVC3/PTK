package ru.ppr.cppk.ui.activity.serviceticketcontrol;

import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.ui.activity.base.ActivityComponent;
import ru.ppr.cppk.ui.activity.base.ActivityModule;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
interface ServiceTicketControlComponent extends ActivityComponent {

    ServiceTicketControlPresenter serviceTicketControlPresenter();

    void inject(ServiceTicketControlActivity serviceTicketControlActivity);

    @Component.Builder
    interface Builder {

        Builder appComponent(AppComponent appComponent);

        Builder activityModule(ActivityModule activityModule);

        ServiceTicketControlComponent build();
    }
}
