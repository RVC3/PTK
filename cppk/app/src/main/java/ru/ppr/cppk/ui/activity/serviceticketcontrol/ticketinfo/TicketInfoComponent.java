package ru.ppr.cppk.ui.activity.serviceticketcontrol.ticketinfo;

import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.FragmentScope;
import ru.ppr.cppk.ui.fragment.base.FragmentComponent;

/**
 * @author Aleksandr Brazhkin
 */
@FragmentScope
@Component(dependencies = AppComponent.class)
interface TicketInfoComponent extends FragmentComponent {
    TicketInfoPresenter ticketInfoPresenter();

    @Component.Builder
    interface Builder {

        Builder appComponent(AppComponent appComponent);

        TicketInfoComponent build();
    }
}
