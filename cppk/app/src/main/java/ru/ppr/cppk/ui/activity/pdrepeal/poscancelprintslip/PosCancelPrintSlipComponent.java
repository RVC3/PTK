package ru.ppr.cppk.ui.activity.pdrepeal.poscancelprintslip;

import dagger.Subcomponent;
import ru.ppr.cppk.dagger.FragmentScope;
import ru.ppr.cppk.ui.fragment.base.FragmentComponent;

/**
 * @author Aleksandr Brazhkin
 */
@FragmentScope
@Subcomponent
public interface PosCancelPrintSlipComponent extends FragmentComponent {
    PosCancelPrintSlipPresenter posCancelPrintSlipPresenter();
}
