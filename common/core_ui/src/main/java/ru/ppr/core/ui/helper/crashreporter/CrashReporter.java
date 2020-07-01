package ru.ppr.core.ui.helper.crashreporter;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.core.domain.model.ApplicationInfo;
import ru.ppr.core.domain.model.DeviceInfo;
import ru.ppr.logger.Logger;

/**
 * Создатель очетов о крашах.
 *
 * @author Aleksandr Brazhkin
 */
public class CrashReporter implements Thread.UncaughtExceptionHandler {

    private static final String TAG = Logger.makeLogTag(CrashReporter.class);

    private final ApplicationInfo applicationInfo;
    private final DeviceInfo deviceInfo;
    private final List<CrashListener> crashListeners;
    private final AppKiller appKiller;
    private Thread.UncaughtExceptionHandler previousHandler;

    CrashReporter(ApplicationInfo applicationInfo, DeviceInfo deviceInfo, AppKiller appKiller, List<CrashListener> crashListeners) {
        this.applicationInfo = applicationInfo;
        this.deviceInfo = deviceInfo;
        this.appKiller = appKiller;
        this.crashListeners = crashListeners;
    }

    public void apply() {
        previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {

        DeviceStateInfo deviceStateInfo = new DeviceStateInfo(getAvailableInternalMemorySize(), getTotalInternalMemorySize());

        CrashReport crashReport = new CrashReport(
                e,
                new Date(),
                applicationInfo,
                deviceInfo,
                deviceStateInfo);

        for (CrashListener crashListener : crashListeners) {
            try {
                crashListener.onCrash(crashReport);
            } catch (Exception e1) {
                Logger.error(TAG, e1);
            }
        }

        if (appKiller != null) {
            appKiller.killApp();
        } else {
            // Если не пробросим дальше, приложение повиснет с черным экраном
            previousHandler.uncaughtException(t, e);
        }
    }

    /**
     * Возвращает размер свободной внутренней памяти.
     *
     * @return Размер свободной внутренней памяти.
     */
    private long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * Возвращает размер внутренней памяти.
     *
     * @return Размер внутренней памяти.
     */
    private long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * Внешний слушатель события краша приложения.
     */
    public interface CrashListener {
        /**
         * Колбек при краше
         *
         * @param crashReport Отчет о краше
         */
        void onCrash(CrashReport crashReport);
    }

    /**
     * Киллер приложения, альренатива {@code previousHandler.uncaughtException(t, e);}
     */
    public interface AppKiller {
        /**
         * Колбек при краше приложения
         */
        void killApp();
    }

    public static class Builder {
        private ApplicationInfo applicationInfo;
        private DeviceInfo deviceInfo;
        private List<CrashListener> crashListeners = new ArrayList<>();
        private AppKiller appKiller;

        /**
         * Устанвливает информацию о приложении
         *
         * @param applicationInfo Информация о приложении
         */
        public Builder setApplicationInfo(ApplicationInfo applicationInfo) {
            this.applicationInfo = applicationInfo;
            return this;
        }

        /**
         * Устанвливает информацию об устройстве
         *
         * @param deviceInfo Информация об устройстве
         */
        public Builder setDeviceInfo(DeviceInfo deviceInfo) {
            this.deviceInfo = deviceInfo;
            return this;
        }

        /**
         * Устанавливает киллера приложения, как альтернативу {@code previousHandler.uncaughtException(t, e);}.
         *
         * @param appKiller Киллер приложения
         */
        public Builder setAppKiller(AppKiller appKiller) {
            this.appKiller = appKiller;
            return this;
        }

        /**
         * Добавляет слущателя на событие краша приложения в очередь по указанному индексу.
         *
         * @param crashListener Слушатель
         */
        public Builder addCrashListener(CrashListener crashListener) {
            crashListeners.add(crashListener);
            return this;
        }

        /**
         * Создает инстанс {@link CrashReporter}.
         */
        public CrashReporter build() {
            return new CrashReporter(applicationInfo, deviceInfo, appKiller, crashListeners);
        }
    }
}
