package ru.ppr.chit.ui.activity.base;

import android.app.Activity;

import dagger.Module;
import dagger.Provides;
import ru.ppr.chit.data.DataModule;
import ru.ppr.chit.di.ActivityScope;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
@Module(includes = DataModule.class)
public class ActivityModule {

    private final Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    public Activity activity() {
        return activity;
    }

}
