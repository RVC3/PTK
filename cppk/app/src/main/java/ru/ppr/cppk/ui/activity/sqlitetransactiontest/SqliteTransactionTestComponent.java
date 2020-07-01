package ru.ppr.cppk.ui.activity.sqlitetransactiontest;

import dagger.Component;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.ui.activity.base.ActivityModule;

/**
 * @author Dmitry Nevolin
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
interface SqliteTransactionTestComponent {

    SqliteTransactionTestPresenter sqliteTransactionTestPresenter();

    void inject(SqliteTransactionTestActivity sqliteTransactionTestActivity);

}
