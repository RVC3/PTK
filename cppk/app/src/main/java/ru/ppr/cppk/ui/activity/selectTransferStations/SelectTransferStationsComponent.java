package ru.ppr.cppk.ui.activity.selectTransferStations;

import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.ui.activity.base.ActivityComponent;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.nsi.repository.StationRepository;

/**
 * @author Grigoriy Kashka
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
interface SelectTransferStationsComponent extends ActivityComponent {
    SelectTransferStationsPresenter selectTransferStationsPresenter();

    void inject(SelectTransferStationsActivity selectTransferStationsActivity);
}
