package ru.ppr.cppk.helpers.crashReporter;

import ru.ppr.core.ui.helper.crashreporter.CrashReport;
import ru.ppr.core.ui.helper.crashreporter.CrashReporter;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.export.ServiceUtils;
import ru.ppr.logger.Logger;

/**
 * Очищает ресурсы при краше.
 *
 * @author Aleksandr Brazhkin
 */
public class FreeResourcesCrashListener implements CrashReporter.CrashListener {

    private static final String TAG = Logger.makeLogTag(FreeResourcesCrashListener.class);

    public FreeResourcesCrashListener() {
    }

    @Override
    public void onCrash(CrashReport crashReport) {

        Logger.trace(TAG, "onCrash");

        Di.INSTANCE.getEdsManagerWrapper().closeBlocking();

        // Пытаемся избавиться от ситуации когда касса не видит state файл,
        // когда он создан. Вручную на всякий случай останавливаем сервис. Эта
        // же функция очистит папку State
        ServiceUtils.get().stopARMservice(TAG + " uncaughtException()");

        Logger.flushQueueSync();
    }
}
