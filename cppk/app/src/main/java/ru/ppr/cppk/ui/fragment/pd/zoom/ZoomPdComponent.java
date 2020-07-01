package ru.ppr.cppk.ui.fragment.pd.zoom;

import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.FragmentScope;
import ru.ppr.cppk.ui.fragment.base.FragmentComponent;

/**
 * @author Aleksandr Brazhkin
 */
@FragmentScope
@Component(dependencies = AppComponent.class)
interface ZoomPdComponent extends FragmentComponent {
    void inject(ZoomPdFragment zoomPdFragment);
}
