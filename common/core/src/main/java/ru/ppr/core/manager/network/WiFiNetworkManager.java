package ru.ppr.core.manager.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.ppr.logger.Logger;
import ru.ppr.utils.Executors;

/**
 * Мененджер работы с WiFi.
 *
 * @author Aleksandr Brazhkin
 */
public class WiFiNetworkManager implements NetworkManager {

    private static final String TAG = Logger.makeLogTag(WiFiNetworkManager.class);
    /**
     * Таймаут на отложенное отключение интернета
     */
    private static final int DISABLE_DELAY = 60;

    private final Object LOCK = new Object();
    private final Context context;
    private final AirplaneModeManager airplaneModeManager;
    private final WifiManager wifiManager;
    private final Map<Object, Future> delayedDisableTasks = new HashMap<>();

    public WiFiNetworkManager(Context context, AirplaneModeManager airplaneModeManager) {
        this.context = context.getApplicationContext();
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.airplaneModeManager = airplaneModeManager;
        Logger.trace(TAG, "WiFiNetworkManager initialized");
    }

    /**
     * Освобождение ресурсов, связанных с объектом.
     */
    public void destroy() {
        Logger.trace(TAG, "WiFiNetworkManager destroyed");
    }

    @Override
    public boolean isEnabled() {
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    @Override
    public boolean enableInternetForExternalDevice(Object deviceId) {
        synchronized (LOCK) {
            Future delayedDisableTask = delayedDisableTasks.get(deviceId);
            if (delayedDisableTask != null) {
                delayedDisableTask.cancel(false);
            }
            delayedDisableTasks.put(deviceId, null);
            return enableSync();
        }
    }

    @Override
    public boolean disableInternetForExternalDevice(Object deviceId, boolean rightNow) {
        synchronized (LOCK) {
            if (!delayedDisableTasks.containsKey(deviceId)) {
                return true;
            }
            Future delayedDisableTask = delayedDisableTasks.get(deviceId);
            if (delayedDisableTask != null) {
                delayedDisableTask.cancel(false);
            }
            if (rightNow) {
                return disableRightNow(deviceId);
            } else {
                delayedDisableTask = scheduledExecutorService.schedule(() -> disableRightNow(deviceId), DISABLE_DELAY, TimeUnit.SECONDS);
                delayedDisableTasks.put(deviceId, delayedDisableTask);
                return true;
            }
        }
    }

    @Override
    public void enable(StateChangedListener stateChangedListener) {
        Logger.trace(TAG, "enable() START");
        //выключим режим в рамолете. Если попытка будет неудачной, все равно попробуем включить wi-fi - должно работать
        airplaneModeManager.setEnabled(false);
        if (wifiManager != null) {
            if (!wifiManager.isWifiEnabled()) {
                Logger.trace(TAG, "enable() registerReceiver");
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        final int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                        Logger.trace(TAG, "enable() onReceive, state = " + state);
                        if (state == WifiManager.WIFI_STATE_ENABLED) {
                            if (stateChangedListener != null) {
                                stateChangedListener.onStateChanged(true);
                                context.unregisterReceiver(this);
                            }
                        }
                    }
                }, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

                wifiManager.setWifiEnabled(true);

            } else {
                if (stateChangedListener != null) {
                    stateChangedListener.onStateChanged(true);
                }
            }
        } else {
            if (stateChangedListener != null) {
                stateChangedListener.onStateChanged(false);
            }
        }
        Logger.trace(TAG, "enable() FINISH");
    }

    @Override
    public void disable(StateChangedListener stateChangedListener) {
        Logger.trace(TAG, "disable() START");
        if (wifiManager != null) {
            if (wifiManager.isWifiEnabled()) {
                Logger.trace(TAG, "disable() registerReceiver");
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        final int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                        Logger.trace(TAG, "disable() onReceive, state = " + state);
                        if (state == WifiManager.WIFI_STATE_DISABLED) {
                            if (stateChangedListener != null) {
                                stateChangedListener.onStateChanged(false);
                                context.unregisterReceiver(this);
                            }
                        }
                    }
                }, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

//                wifiManager.setWifiEnabled(false);

            } else {
                if (stateChangedListener != null) {
                    stateChangedListener.onStateChanged(false);
                }
            }
        } else {
            if (stateChangedListener != null) {
                stateChangedListener.onStateChanged(false);
            }
        }
        Logger.trace(TAG, "disable() FINISH");
    }

    @Override
    public boolean enableSync() {
        Logger.trace(TAG, "enableSync() START");
        checkThread();
        boolean[] enabledArray = new boolean[]{false};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        enable(enabled -> {
            enabledArray[0] = enabled;
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Logger.trace(TAG, "enableSync() FINISH return: " + enabledArray[0]);
        return enabledArray[0];
    }

    @Override
    public boolean disableSync() {
        Logger.trace(TAG, "disableSync() START");
        checkThread();
        boolean[] disabledArray = new boolean[]{false};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        disable(enabled -> {
            disabledArray[0] = !enabled;
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Logger.trace(TAG, "disableSync() FINISH return: " + disabledArray[0]);
        return disabledArray[0];
    }

    private boolean disableRightNow(Object deviceId) {
        synchronized (LOCK) {
            delayedDisableTasks.remove(deviceId);
            if (delayedDisableTasks.isEmpty()) {
                Logger.trace(TAG, "Отложенное выключение WiFiNetworkManager, deviceId = " + deviceId);
                disableSync();
                return !isEnabled();
            } else {
                Logger.trace(TAG, "WiFiNetworkManager используется другим устройством, deviceId != " + deviceId);
                return false;
            }
        }
    }

    private void checkThread() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new UnsupportedOperationException("Method call on main thread is not allowed");
        }
    }

    /**
     * {@link Executor}  для запуска операций в отдельном потоке.
     */
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
            1, r -> new Thread(r, "WiFiNetworkManagerExecutor")) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            Executors.logErrorAfterExecute(TAG, r, t);
        }
    };
}
