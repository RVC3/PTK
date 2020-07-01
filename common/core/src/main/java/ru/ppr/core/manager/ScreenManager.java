package ru.ppr.core.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.provider.Settings;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.logger.Logger;

/**
 * Контролирует состояние экрана.
 * <p>
 * Created by Александр on 05.10.2016.
 */
@Singleton
public final class ScreenManager {

    private static final String TAG = Logger.makeLogTag(ScreenManager.class);

    private final Context context;
    private boolean screenOn;
    private final Set<ScreenStateListener> screenStateListeners = new HashSet<>();

    @Inject
    public ScreenManager(Context context) {
        Logger.trace(TAG, "ScreenManager initialized");
        this.context = context.getApplicationContext();
        onScreenStateChanged(isScreenOnInternal());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        this.context.registerReceiver(screenStateReceiver, filter);
    }

    /**
     * Освобождает ресурсы, связанные с объектом
     */
    private void destroy() {
        this.context.unregisterReceiver(this.screenStateReceiver);
        Logger.trace(TAG, "ScreenManager destroyed");
    }

    /**
     * Регистрирует внешнего слушателя состояния экрана
     *
     * @param screenStateListener слушатель
     */
    public void addScreenStateListener(ScreenStateListener screenStateListener) {
        screenStateListeners.add(screenStateListener);
        screenStateListener.onScreenStateChanged(screenOn);
    }

    /**
     * Удаляет слушателя из списка
     *
     * @param screenStateListener слушатель
     */
    public void removeScreenStateListener(ScreenStateListener screenStateListener) {
        screenStateListeners.remove(screenStateListener);
    }

    /**
     * Проверяет, включен ли экран
     *
     * @return {@code true} если экран вклчен, {@code false} иначе.
     */
    private boolean isScreenOnInternal() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean screenOn;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            screenOn = pm.isInteractive();
        } else {
            screenOn = pm.isScreenOn();
        }
        return screenOn;
    }

    /**
     * Проверяет, включен ли экран
     *
     * @return {@code true} если экран вклчен, {@code false} иначе.
     */
    public boolean isScreenOn() {
        return screenOn;
    }

    private void onScreenStateChanged(boolean screenOn) {
        Logger.trace(TAG, "onScreenStateChanged: screenOn = " + screenOn);
        this.screenOn = screenOn;
        for (ScreenStateListener screenStateListener : screenStateListeners) {
            screenStateListener.onScreenStateChanged(this.screenOn);
        }
    }

    /**
     * Устанавливает таймаут до автоматического выключения экрана
     *
     * @param screenOffTimeout - время в миллисекундах
     */
    public void setScreenOffTimeout(int screenOffTimeout) {
        Logger.trace(TAG, "setScreenOffTimeout, screenOffTimeout = " + screenOffTimeout);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, screenOffTimeout);
    }

    private BroadcastReceiver screenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                onScreenStateChanged(false);
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                onScreenStateChanged(true);
            } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                // do other things if you need
            }
        }
    };

    public interface ScreenStateListener {
        void onScreenStateChanged(boolean screenOn);
    }
}
