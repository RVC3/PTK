package ru.ppr.cppk.ui.activity.transfersale.preparation;

import dagger.Subcomponent;
import ru.ppr.cppk.dagger.FragmentScope;
import ru.ppr.cppk.ui.fragment.base.FragmentComponent;

/**
 * @author Dmitry Nevolin
 */
@FragmentScope
@Subcomponent
public interface PreparationComponent extends FragmentComponent {
    PreparationPresenter preparationPresenter();
}
