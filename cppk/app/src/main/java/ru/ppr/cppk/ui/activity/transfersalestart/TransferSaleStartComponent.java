package ru.ppr.cppk.ui.activity.transfersalestart;

import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.fragment.base.FragmentComponent;

/**
 * @author Dmitry Nevolin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface TransferSaleStartComponent extends FragmentComponent {
    TransferSaleStartPresenter transferSaleStartPresenter();
}
