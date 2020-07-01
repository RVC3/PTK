package ru.ppr.cppk.ui.activity.controlreadbsc;

import dagger.BindsInstance;
import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.ui.activity.base.ActivityComponent;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.controlreadbsc.model.ControlReadBscParams;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
interface ControlReadBscComponent extends ActivityComponent {
    ControlReadBscPresenter controlReadBscPresenter();

    void inject(ControlReadBscActivity readBscActivity);

    @Component.Builder
    interface Builder {

        Builder appComponent(AppComponent appComponent);

        Builder activityModule(ActivityModule activityModule);

        @BindsInstance
        Builder controlReadBscParams(ControlReadBscParams controlReadBscParams);

        ControlReadBscComponent build();
    }
}
