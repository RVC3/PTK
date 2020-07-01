package ru.ppr.chit.helpers;

import javax.inject.Inject;

import ru.ppr.core.domain.model.ApplicationInfo;
import ru.ppr.core.domain.model.DeviceInfo;
import ru.ppr.core.ui.helper.crashreporter.CrashReporter;
import ru.ppr.core.ui.helper.crashreporter.FileLoggerCrashListener;
import ru.ppr.core.ui.helper.crashreporter.LoggerCrashListener;

/**
 * Билдер {@link CrashReporter}.
 *
 * @author Aleksandr Brazhkin
 */
public class CrashReporterBuilder {

    private final ApplicationInfo applicationInfo;
    private final DeviceInfo deviceInfo;
    private final FilePathProvider filePathProvider;

    @Inject
    CrashReporterBuilder(ApplicationInfo applicationInfo, DeviceInfo deviceInfo, FilePathProvider filePathProvider) {
        this.applicationInfo = applicationInfo;
        this.deviceInfo = deviceInfo;
        this.filePathProvider = filePathProvider;
    }

    public CrashReporter build() {
        return new CrashReporter.Builder()
                .setApplicationInfo(applicationInfo)
                .setDeviceInfo(deviceInfo)
                .addCrashListener(new LoggerCrashListener())
                .addCrashListener(new FileLoggerCrashListener(filePathProvider.getCrashLogsDir(), FileLoggerCrashListener.DEFAULT_MAX_FILE_COUNT))
                .build();
    }
}
