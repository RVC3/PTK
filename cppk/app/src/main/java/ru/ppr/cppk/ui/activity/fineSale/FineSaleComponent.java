package ru.ppr.cppk.ui.activity.fineSale;

import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.ui.activity.base.ActivityComponent;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.fragment.printFineCheck.PrintFineCheckSharedComponent;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface FineSaleComponent extends ActivityComponent, PrintFineCheckSharedComponent {

    FineSalePresenter fineSalePresenter();

    void inject(FineSaleActivity fineSaleActivity);
}
