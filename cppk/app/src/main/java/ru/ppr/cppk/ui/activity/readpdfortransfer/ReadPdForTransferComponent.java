package ru.ppr.cppk.ui.activity.readpdfortransfer;

import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.ui.activity.base.ActivityComponent;
import ru.ppr.cppk.ui.activity.base.ActivityModule;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
interface ReadPdForTransferComponent extends ActivityComponent {
    ReadPdForTransferPresenter repealReadBscPresenter();
}
