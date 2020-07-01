package ru.ppr.chit.ui.widget.statusbar;

import dagger.Component;
import ru.ppr.chit.AppComponent;
import ru.ppr.chit.di.ViewScope;

/**
 * @author Dmitry Nevolin
 */
@ViewScope
@Component(dependencies = AppComponent.class)
interface StatusBarComponent {

    void inject(StatusBarAndroidView statusBarAndroidView);

    StatusBarPresenter statusBarPresenter();

}
