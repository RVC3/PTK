package ru.ppr.cppk.ui.fragment.base;


import dagger.Component;
import ru.ppr.core.ui.mvp.core.MvpProcessor;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.FragmentScope;

/**
 * Di для {@link MvpFragment}.
 *
 * @author Aleksandr Brazhkin
 */
@FragmentScope
@Component(dependencies = AppComponent.class)
interface MvpFragmentComponent {
    MvpProcessor mvpProcessor();
}
