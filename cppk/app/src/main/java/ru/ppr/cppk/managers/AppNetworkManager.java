package ru.ppr.cppk.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.ppr.core.manager.network.NetworkManager;
import ru.ppr.logger.Logger;
import ru.ppr.utils.Executors;

/**
 * Менеджер работы с состоянием сети.
 *
 * @author Aleksandr Brazhkin
 */
public class AppNetworkManager implements NetworkManager {

    private static final String TAG = Logger.makeLogTag(AppNetworkManager.class);
    /**
     * Таймаут на отложенное отключение интернета
     */
    private static final int DISABLE_DELAY = 60;

    private final Object LOCK = new Object();
    private final NetworkManager mobileNetworkManager;
    private final NetworkManager wifiNetworkManager;
    private NetworkType networkType;
    private final Map<Object, Future> delayedDisableTasks = new HashMap<>();

    public AppNetworkManager(NetworkManager mobileNetworkManager, NetworkManager wifiNetworkManager, NetworkType networkType) {
        this.mobileNetworkManager = mobileNetworkManager;
        this.wifiNetworkManager = wifiNetworkManager;
        this.networkType = networkType;
        // Синхронизируем состояние менеджера и системы
        if (networkManager() == mobileNetworkManager) {
            wifiNetworkManager.disable(null);
        } else {
            mobileNetworkManager.disable(null);
        }
    }

    /**
     * Освобождение ресурсов, связанных с объектом.
     */
    public void destroy() {
        Logger.trace(TAG, "AppNetworkManager destroyed");
    }


    @Override
    public boolean isEnabled() {
        return networkManager().isEnabled();
    }

    @Override
    public boolean enableInternetForExternalDevice(Object deviceId) {
        synchronized (LOCK) {
            Future delayedDisableTask = delayedDisableTasks.get(deviceId);
            if (delayedDisableTask != null) {
                delayedDisableTask.cancel(false);
            }
            delayedDisableTasks.put(deviceId, null);
            return networkManager().enableInternetForExternalDevice(this);
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
        networkManager().enable(stateChangedListener);
    }

    @Override
    public void disable(StateChangedListener stateChangedListener) {
        networkManager().disable(stateChangedListener);
    }

    @Override
    public boolean enableSync() {
        return networkManager().enableSync();
    }

    @Override
    public boolean disableSync() {
        return networkManager().disableSync();
    }

    public void setNetworkType(NetworkType networkType) {
        if (this.networkType.equals(networkType)) {
            Logger.trace(TAG, "networkType " + networkType + " is already set, skip it");
        }
        if (networkManager().isEnabled()) {
            // Отключаем интернет текущего типа
            networkManager().disableInternetForExternalDevice(this, true);
            // Обновляем тип интернета
            this.networkType = networkType;
            // Включаем интернет нового типа
            networkManager().enableInternetForExternalDevice(this);
        } else {
            // Обновляем тип интернета
            this.networkType = networkType;
        }
    }

    private NetworkManager networkManager() {
        return networkType == NetworkType.MOBILE ? mobileNetworkManager : wifiNetworkManager;
    }

    private boolean disableRightNow(Object deviceId) {
        synchronized (LOCK) {
            delayedDisableTasks.remove(deviceId);
            if (delayedDisableTasks.isEmpty()) {
                Logger.trace(TAG, "Отложенное выключение AppNetworkManager, deviceId = " + deviceId);
                networkManager().disableInternetForExternalDevice(this, true);
                return !isEnabled();
            } else {
                Logger.trace(TAG, "AppNetworkManager используется другим устройством, deviceId != " + deviceId);
                return false;
            }
        }
    }

    /**
     * {@link Executor}  для запуска операций в отдельном потоке.
     */
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
            1, r -> new Thread(r, "AppNetworkManager")) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            Executors.logErrorAfterExecute(TAG, r, t);
        }
    };

    public enum NetworkType {
        WI_FI,
        MOBILE
    }
}
