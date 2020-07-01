package ru.ppr.chit.ui.activity.passengerlist;

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
interface PassengerListComponent extends ActivityComponent {
    PassengerListPresenter passengerListPresenter();

    void inject(PassengerListActivity readBscActivity);
}
