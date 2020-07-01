package ru.ppr.cppk.helpers.crashReporter;

import ru.ppr.core.ui.helper.crashreporter.CrashReport;
import ru.ppr.core.ui.helper.crashreporter.CrashReporter;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.localdb.model.LogActionType;
import ru.ppr.cppk.localdb.model.LogEvent;
import ru.ppr.logger.Logger;

/**
 * Пишет информацию о краше в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class DbLoggerCrashListener implements CrashReporter.CrashListener {

    private static final String TAG = Logger.makeLogTag(DbLoggerCrashListener.class);

    private final Globals globals;

    public DbLoggerCrashListener(Globals globals) {
        this.globals = globals;
    }

    @Override
    public void onCrash(CrashReport crashReport) {

        Logger.trace(TAG, "onCrash");

        LogEvent logEvent = Dagger.appComponent().logEventBuilder()
                .setLogActionType(LogActionType.APP_CRASH)
                .setMessage(crashReport.toString())
                .build();
        Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEvent);
    }
}
