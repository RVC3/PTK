package ru.ppr.cppk.ui.activity.base;

import android.app.Activity;

import dagger.Module;
import dagger.Provides;
import ru.ppr.cppk.dagger.ActivityScope;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
@Module
public class ActivityModule {

    private final Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    public Activity activity() {
        return activity;
    }
}
