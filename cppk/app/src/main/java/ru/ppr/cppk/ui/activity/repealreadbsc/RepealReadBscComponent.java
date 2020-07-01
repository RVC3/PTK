package ru.ppr.cppk.ui.activity.repealreadbsc;

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
interface RepealReadBscComponent extends ActivityComponent {
    RepealReadBscPresenter repealReadBscPresenter();

    void inject(RepealReadBscActivity readBscActivity);
}
