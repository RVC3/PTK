package ru.ppr.chit.ui.widget.networkstatus;

import dagger.Component;
import ru.ppr.chit.AppComponent;
import ru.ppr.chit.di.ViewScope;

/**
 * @author Dmitry Nevolin
 */
@ViewScope
@Component(dependencies = AppComponent.class)
interface NetworkStatusComponent {

    void inject(NetworkStatusAndroidView networkStatusAndroidView);

    NetworkStatusPresenter networkStatusPresenter();

}
