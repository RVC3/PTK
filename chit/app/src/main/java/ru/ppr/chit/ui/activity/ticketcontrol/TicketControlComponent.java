package ru.ppr.chit.ui.activity.ticketcontrol;

import javax.inject.Named;

import dagger.BindsInstance;
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
interface TicketControlComponent extends ActivityComponent {
    TicketControlPresenter ticketControlPresenter();

    void inject(TicketControlActivity ticketControlActivity);

    @Component.Builder
    interface Builder {

        Builder appComponent(AppComponent appComponent);

        Builder activityModule(ActivityModule  activityModule);

        @BindsInstance
        Builder ticketId(@Named("ticketId") long ticketId);

        @BindsInstance
        Builder fromBsc(@Named("fromBsc") boolean fromBsc);

        @BindsInstance
        Builder fromBarcode(@Named("fromBarcode") boolean fromBarcode);

        TicketControlComponent build();
    }
}
