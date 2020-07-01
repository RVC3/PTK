package ru.ppr.cppk.ui.fragment.pd.servicefee;

import dagger.BindsInstance;
import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.FragmentScope;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.ui.fragment.base.FragmentComponent;

/**
 * @author Aleksandr Brazhkin
 */
@FragmentScope
@Component(dependencies = AppComponent.class)
interface ServiceFeePdComponent extends FragmentComponent {

    ServiceFeePdPresenter serviceFeePdPresenter();

    @Component.Builder
    interface Builder {
        Builder appComponent(AppComponent appComponent);

        @BindsInstance
        Builder pd(PD pd);

        ServiceFeePdComponent build();
    }

}
