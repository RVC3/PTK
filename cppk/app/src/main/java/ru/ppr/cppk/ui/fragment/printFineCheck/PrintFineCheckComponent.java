package ru.ppr.cppk.ui.fragment.printFineCheck;

import dagger.Subcomponent;
import ru.ppr.cppk.dagger.FragmentScope;
import ru.ppr.cppk.ui.fragment.base.FragmentComponent;

/**
 * @author Aleksandr Brazhkin
 */
@FragmentScope
@Subcomponent
public interface PrintFineCheckComponent extends FragmentComponent {
    PrintFineCheckPresenter printFineCheckPresenter();
}
