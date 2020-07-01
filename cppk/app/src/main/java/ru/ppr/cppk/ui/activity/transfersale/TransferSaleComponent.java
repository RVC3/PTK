package ru.ppr.cppk.ui.activity.transfersale;

import dagger.BindsInstance;
import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.transfersale.model.TransferSaleParams;
import ru.ppr.cppk.ui.activity.transfersale.preparation.PreparationComponent;

/**
 * @author Dmitry Nevolin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface TransferSaleComponent {

    TransferSalePresenter transferSalePresenter();

    void inject(TransferSaleActivity transferSaleActivity);

    PreparationComponent preparationComponent();

    @Component.Builder
    interface Builder {

        Builder appComponent(AppComponent appComponent);

        Builder activityModule(ActivityModule activityModule);

        @BindsInstance
        Builder transferSaleParams(TransferSaleParams transferSaleParams);

        TransferSaleComponent build();

    }

}
