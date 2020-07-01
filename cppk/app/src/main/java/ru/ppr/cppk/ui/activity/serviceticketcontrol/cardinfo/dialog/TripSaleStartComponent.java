package ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo.dialog;

import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.transfersalestart.TransferSaleStartPresenter;
import ru.ppr.cppk.ui.fragment.base.FragmentComponent;

/**
 * @author Sergey Kolesnikov
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface TripSaleStartComponent extends FragmentComponent {
    TripOpeningClosurePresenter tripOpeningClosurePresenter();
}
