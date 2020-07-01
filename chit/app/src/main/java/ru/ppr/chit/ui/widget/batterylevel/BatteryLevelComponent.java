package ru.ppr.chit.ui.widget.batterylevel;

import dagger.Component;
import ru.ppr.chit.AppComponent;
import ru.ppr.chit.di.ViewScope;

/**
 * @author Dmitry Nevolin
 */
@ViewScope
@Component(dependencies = AppComponent.class)
interface BatteryLevelComponent {

    void inject(BatteryLevelAndroidView batteryLevelAndroidView);

    BatteryLevelPresenter batteryLevelPresenter();

}
