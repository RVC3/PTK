package ru.ppr.chit.ui.activity.readbarcode;

import dagger.Component;
import ru.ppr.chit.AppComponent;
import ru.ppr.chit.di.ActivityScope;
import ru.ppr.chit.ui.activity.base.ActivityComponent;
import ru.ppr.chit.ui.activity.base.ActivityModule;

/**
 * @author Grigoriy Kashka
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
interface ReadBarcodeComponent extends ActivityComponent {
    ReadBarcodePresenter readBarcodePresenter();

    void inject(ReadBarcodeActivity readBarcodeActivity);
}
