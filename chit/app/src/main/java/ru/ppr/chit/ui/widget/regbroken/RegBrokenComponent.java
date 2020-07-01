package ru.ppr.chit.ui.widget.regbroken;

import dagger.Component;
import ru.ppr.chit.AppComponent;
import ru.ppr.chit.di.ViewScope;

/**
 * @author Dmitry Nevolin
 */
@ViewScope
@Component(dependencies = AppComponent.class)
interface RegBrokenComponent {

    void inject(RegBrokenAndroidView regBrokenAndroidView);

    RegBrokenPresenter regBrokenPresenter();

}
