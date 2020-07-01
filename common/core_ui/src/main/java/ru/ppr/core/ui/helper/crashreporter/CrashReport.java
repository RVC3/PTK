package ru.ppr.core.ui.helper.crashreporter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import ru.ppr.core.domain.model.ApplicationInfo;
import ru.ppr.core.domain.model.DeviceInfo;


/**
 * Информация о краше.
 *
 * @author Aleksandr Brazhkin
 */
public class CrashReport {

    private final Throwable throwable;
    private final Date crashDateTime;
    private final ApplicationInfo applicationInfo;
    private final DeviceInfo deviceInfo;
    private final DeviceStateInfo deviceStateInfo;

    CrashReport(Throwable throwable,
                       Date crashDateTime,
                       ApplicationInfo applicationInfo,
                       DeviceInfo deviceInfo,
                       DeviceStateInfo deviceStateInfo) {
        this.throwable = throwable;
        this.crashDateTime = crashDateTime;
        this.applicationInfo = applicationInfo;
        this.deviceInfo = deviceInfo;
        this.deviceStateInfo = deviceStateInfo;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Crash report collected on : ").append(crashDateTime).append('\n');
        stringBuilder.append('\n');
        ///////////////////////////////////////////
        stringBuilder.append("Application info: ").append('\n');
        stringBuilder.append("================================ \n");
        stringBuilder.append("Debug: ").append(applicationInfo.isDebug()).append('\n');
        stringBuilder.append("Application id: ").append(applicationInfo.getApplicationId()).append('\n');
        stringBuilder.append("Build type: ").append(applicationInfo.getBuildType()).append('\n');
        stringBuilder.append("Flavor: ").append(applicationInfo.getFlavor()).append('\n');
        stringBuilder.append("Version code: ").append(applicationInfo.getVersionCode()).append('\n');
        stringBuilder.append("Version name: ").append(applicationInfo.getVersionName()).append('\n');
        stringBuilder.append('\n');
        stringBuilder.append("Device info: ").append('\n');
        stringBuilder.append("================================ \n");
        stringBuilder.append("Android version: ").append(deviceInfo.getAndroidDisplayedVersion()).append('\n');
        stringBuilder.append("SDK version: ").append(deviceInfo.getAndroidSDKVersion()).append('\n');
        stringBuilder.append("Build version: ").append(deviceInfo.getBuildDisplayedVersion()).append('\n');
        stringBuilder.append("Brand: ").append(deviceInfo.getDeviceBrand()).append('\n');
        stringBuilder.append("Model: ").append(deviceInfo.getDeviceModel()).append('\n');
        stringBuilder.append('\n');
        stringBuilder.append("Device state info: ").append('\n');
        stringBuilder.append("================================ \n");
        stringBuilder.append("Total internal memory size: ").append(deviceStateInfo.getTotalInternalMemorySize()).append('\n');
        stringBuilder.append("Available internal memory size: ").append(deviceStateInfo.getAvailableInternalMemorySize()).append('\n');
        stringBuilder.append("\n");
        stringBuilder.append("Stack:").append('\n');
        stringBuilder.append("================================").append('\n');

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        String stacktrace = result.toString();

        stringBuilder.append(stacktrace);
        stringBuilder.append('\n');
        stringBuilder.append("Cause:").append('\n');
        stringBuilder.append("================================").append('\n');

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            stringBuilder.append(result.toString()).append('\n');
            cause = cause.getCause();
        }
        printWriter.close();

        stringBuilder.append("**** End of current crash report ***");

        return stringBuilder.toString();
    }
}
