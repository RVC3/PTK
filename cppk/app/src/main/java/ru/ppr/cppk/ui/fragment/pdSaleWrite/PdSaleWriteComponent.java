package ru.ppr.cppk.ui.fragment.pdSaleWrite;

import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.FragmentScope;

/**
 * @author Aleksandr Brazhkin
 */
@FragmentScope
@Component(dependencies = AppComponent.class)
public interface PdSaleWriteComponent {
    void inject(PdSaleWriteFragment pdSaleWriteFragment);
}
