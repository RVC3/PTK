package ru.ppr.chit.ui.widget.clock;

import dagger.Component;
import ru.ppr.chit.AppComponent;
import ru.ppr.chit.di.ViewScope;

/**
 * @author Dmitry Nevolin
 */
@ViewScope
@Component(dependencies = AppComponent.class)
interface ClockComponent {

    void inject(ClockAndroidView clockAndroidView);

    ClockPresenter clockPresenter();

}
