package ru.ppr.core.ui.helper.crashreporter;

import ru.ppr.logger.Logger;

/**
 * Логирует информацию о краше.
 *
 * @author Aleksandr Brazhkin
 */
public class LoggerCrashListener implements CrashReporter.CrashListener {

    private static final String TAG = Logger.makeLogTag(LoggerCrashListener.class);

    @Override
    public void onCrash(CrashReport crashReport) {
        Logger.error(TAG, crashReport.getThrowable());
        Logger.error(TAG, crashReport.toString());
        Logger.flushQueueSync();
    }
}
