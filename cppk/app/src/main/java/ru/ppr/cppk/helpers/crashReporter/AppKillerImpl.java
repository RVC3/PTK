package ru.ppr.cppk.helpers.crashReporter;

import android.content.Context;

import ru.ppr.core.ui.helper.crashreporter.CrashReporter;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.logger.Logger;

/**
 * Перезапускает приложение в аврийном режиме при краше.
 *
 * @author Aleksandr Brazhkin
 */
public class AppKillerImpl implements CrashReporter.AppKiller {

    private static final String TAG = Logger.makeLogTag(AppKillerImpl.class);

    private final Context context;

    public AppKillerImpl(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void killApp() {

        Logger.trace(TAG, "killApp");

        // Стартуем SplashActivity
        Navigator.navigateToSplashActivity(context, true);
        // Убиваем приложение
        System.exit(1);
    }
}
