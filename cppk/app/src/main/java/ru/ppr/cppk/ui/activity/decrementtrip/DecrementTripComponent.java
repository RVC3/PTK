package ru.ppr.cppk.ui.activity.decrementtrip;

import dagger.BindsInstance;
import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.ui.activity.base.ActivityComponent;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripParams;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
interface DecrementTripComponent extends ActivityComponent {
    DecrementTripPresenter decrementTripPresenter();

    void inject(DecrementTripActivity readBscActivity);

    @Component.Builder
    interface Builder {

        Builder appComponent(AppComponent appComponent);

        Builder activityModule(ActivityModule activityModule);

        @BindsInstance
        Builder decrementTripParams(DecrementTripParams decrementTripParams);

        DecrementTripComponent build();
    }
}
