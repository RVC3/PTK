package ru.ppr.chit.ui.widget.syncstatus;

import dagger.Component;
import ru.ppr.chit.AppComponent;
import ru.ppr.chit.di.ViewScope;

/**
 * @author Dmitry Nevolin
 */
@ViewScope
@Component(dependencies = AppComponent.class)
interface SyncStatusComponent {

    void inject(SyncStatusAndroidView syncStatusAndroidView);

    SyncStatusPresenter syncStatusPresenter();

}
