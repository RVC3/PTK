package ru.ppr.cppk.ui.activity.senddocstoofd;

import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.ui.activity.base.ActivityComponent;
import ru.ppr.cppk.ui.activity.base.ActivityModule;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface SendDocsToOfdComponent extends ActivityComponent {

    SendDocsToOfdPresenter sendDocsToOfdPresenter();

    void inject(SendDocsToOfdActivity sendDocsToOfdActivity);
}
