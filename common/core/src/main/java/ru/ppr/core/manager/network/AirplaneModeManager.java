package ru.ppr.core.manager.network;

import android.content.Context;
import android.provider.Settings;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.logger.Logger;
import ru.ppr.utils.ShellCommand;

/**
 * Менеджер по работе с режимом "В самолете"
 *
 * @author Grigoriy Kashka
 */
@Singleton
public class AirplaneModeManager {

    private static final String TAG = Logger.makeLogTag(AirplaneModeManager.class);

    /**
     * shell команда "активировать режим в самолете"
     */
    private static final String ENABLE_COMMAND = "settings put global airplane_mode_on 1";
    /**
     * shell команда "выключить режим в самолете"
     */
    private static final String DISABLE_COMMAND = "settings put global airplane_mode_on 0";
    /**
     * shell команда "уведомить систему о включении режима в самолете"
     */
    private static final String NOTIFY_ENABLE_COMMAND = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true";
    /**
     * shell команда "уведомить систему о выключении режима в самолете"
     */
    private static final String NOTIFY_DISABLE_COMMAND = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";


    private final Context context;

    @Inject
    public AirplaneModeManager(Context context) {
        this.context = context.getApplicationContext();
        Logger.trace(TAG, "AirplaneModeManager initialized");
    }

    /**
     * Вернет флаг активированности ирежима в самолете
     *
     * @return
     */
    public boolean isEnabled() {
        //параметр только для чтения
        boolean isEnabled = Settings.System.getInt(context.getContentResolver(), android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        Logger.trace(TAG, "isEnabled = " + isEnabled);
        return isEnabled;
    }

    /**
     * Включить или выключить режим в самолете
     *
     * @param enabled - true, если нужно включить
     * @return - true в случае успеха выполнения операции
     */
    public boolean setEnabled(boolean enabled) {
        Logger.trace(TAG, "setEnabled(" + enabled + ") START");
        boolean out;
        if (enabled != isEnabled()) {
            ShellCommand onOffCommand = new ShellCommand.Builder(enabled ? ENABLE_COMMAND : DISABLE_COMMAND).build();
            ShellCommand notifyCommand = new ShellCommand.Builder(enabled ? NOTIFY_ENABLE_COMMAND : NOTIFY_DISABLE_COMMAND).build();
            try {
                onOffCommand.run();
                Logger.trace(TAG, "onOffCommand output:\n" + onOffCommand.getOutput());
                notifyCommand.run();
                Logger.trace(TAG, "notifyCommand output:\n" + notifyCommand.getOutput());
            } catch (IOException | InterruptedException e) {
                Logger.error(TAG, e);
            }
            out = enabled == isEnabled();
        } else {
            out = true;
        }
        Logger.trace(TAG, "setEnabled(" + enabled + ") FINISH return res=" + out);
        return out;
    }


}
