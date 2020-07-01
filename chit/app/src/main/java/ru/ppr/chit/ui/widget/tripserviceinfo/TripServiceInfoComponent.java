package ru.ppr.chit.ui.widget.tripserviceinfo;

import dagger.Component;
import ru.ppr.chit.AppComponent;
import ru.ppr.chit.data.DataModule;
import ru.ppr.chit.di.ViewScope;

/**
 * @author Dmitry Nevolin
 */
@ViewScope
@Component(dependencies = AppComponent.class, modules = DataModule.class)
interface TripServiceInfoComponent {

    void inject(TripServiceInfoAndroidView tripServiceInfoAndroidView);

    TripServiceInfoPresenter tripServiceInfoPresenter();

}
