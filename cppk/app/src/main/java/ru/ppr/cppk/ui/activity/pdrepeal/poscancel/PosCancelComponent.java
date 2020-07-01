package ru.ppr.cppk.ui.activity.pdrepeal.poscancel;

import javax.inject.Named;

import dagger.BindsInstance;
import dagger.Subcomponent;
import ru.ppr.cppk.dagger.FragmentScope;
import ru.ppr.cppk.ui.fragment.base.FragmentComponent;

/**
 * @author Aleksandr Brazhkin
 */
@FragmentScope
@Subcomponent
public interface PosCancelComponent extends FragmentComponent {
    PosCancelPresenter posCancelPresenter();

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        PosCancelComponent.Builder saleTransactionEventId(@Named("saleTransactionEventId") long saleTransactionEventId);

        PosCancelComponent build();
    }
}
