package ru.ppr.cppk.ui.activity.pdrepeal;

import javax.inject.Named;

import dagger.BindsInstance;
import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.ui.activity.base.ActivityComponent;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.pdrepeal.poscancel.PosCancelComponent;
import ru.ppr.cppk.ui.activity.pdrepeal.poscancelprintslip.PosCancelPrintSlipComponent;
import ru.ppr.cppk.ui.activity.pdrepeal.printrepealcheck.PrintRepealCheckComponent;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface PdRepealComponent extends ActivityComponent {

    PdRepealPresenter pdRepealPresenter();

    void inject(PdRepealActivity pdRepealActivity);

    PrintRepealCheckComponent printRepealCheckComponent();

    PosCancelComponent.Builder posCancelComponentBuilder();

    PosCancelPrintSlipComponent posCancelPrintSlipComponent();

    @Component.Builder
    interface Builder {

        PdRepealComponent.Builder appComponent(AppComponent appComponent);

        PdRepealComponent.Builder activityModule(ActivityModule activityModule);

        @BindsInstance
        PdRepealComponent.Builder pdRepealParams(@Named("pdRepealParams") PdRepealParams pdRepealParams);

        PdRepealComponent build();
    }
}
