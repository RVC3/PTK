package ru.ppr.core.manager.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Looper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.ppr.logger.Logger;
import ru.ppr.utils.Executors;

/**
 * Менеджер работы с состоянием мобильных данных.
 *
 * @author Aleksandr Brazhkin
 */
public class MobileNetworkManager implements NetworkManager {

    private static final String TAG = Logger.makeLogTag(MobileNetworkManager.class);
    /**
     * Таймаут ожидания срабатывания ресивера включения мобильных данных
     */
    private static final int RECEIVER_WAIT_TIMEOUT = 5000;
    /**
     * Таймаут на отложенное отключение интернета
     */
    private static final int DISABLE_DELAY = 60;

    private final Object LOCK = new Object();
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final AirplaneModeManager airplaneModeManager;
    private final Map<Object, Future> delayedDisableTasks = new HashMap<>();

    public MobileNetworkManager(Context context, AirplaneModeManager airplaneModeManager) {
        this.context = context.getApplicationContext();
        this.airplaneModeManager = airplaneModeManager;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.context.registerReceiver(mobileDataBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        Logger.trace(TAG, "MobileNetworkManager initialized");
    }

    /**
     * Освобождение ресурсов, связанных с объектом.
     */
    public void destroy() {
        context.unregisterReceiver(mobileDataBroadcastReceiver);
        Logger.trace(TAG, "MobileNetworkManager destroyed");
    }

    @Override
    public boolean isEnabled() {
        return isEnabledWithPrivateApi();
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
        if (airplaneModeManager.setEnabled(false)) { //в режиме в самолете невозможно активировать мобильные данные
            if (!isEnabledWithPrivateApi()) {
                BroadcastReceiver[] localBroadcastReceiver = {null};
                Future[] waitingFuture = {null};
                AtomicBoolean callbackExecuted = new AtomicBoolean(false);
                localBroadcastReceiver[0] = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
//                    int networkType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);
//                    boolean typeMobile = networkType == ConnectivityManager.TYPE_MOBILE;
//                    NetworkInfo networkInfo = connectivityManager.getNetworkInfo(networkType);
//                    boolean connected = networkInfo.isConnected();
                        boolean connected = isEnabledWithPrivateApi();
                        Logger.trace(TAG, "enable() onReceive, connected = " + connected);
                        if (!connected) {
                            return;
                        }
                        if (callbackExecuted.getAndSet(true)) {
                            return;
                        }
                        context.unregisterReceiver(localBroadcastReceiver[0]);
                        waitingFuture[0].cancel(true);
                        if (stateChangedListener != null) {
                            stateChangedListener.onStateChanged(connected);
                        }
                    }
                };
                Logger.trace(TAG, "enable() start waitingFuture");
                waitingFuture[0] = scheduledExecutorService.schedule(() -> {
                    if (callbackExecuted.getAndSet(true)) {
                        return;
                    }
                    boolean connected = isEnabledWithPrivateApi();
                    Logger.trace(TAG, "enable() onTimeout, connected = " + connected);
                    context.unregisterReceiver(localBroadcastReceiver[0]);
                    if (stateChangedListener != null) {
                        stateChangedListener.onStateChanged(connected);
                    }
                }, RECEIVER_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);

                Logger.trace(TAG, "enable() registerReceiver");
                context.registerReceiver(localBroadcastReceiver[0], new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

                setEnabledWithPrivateApi(true);

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
        if (isEnabledWithPrivateApi()) {
            BroadcastReceiver[] localBroadcastReceiver = {null};
            Future[] waitingFuture = {null};
            AtomicBoolean callbackExecuted = new AtomicBoolean(false);
            localBroadcastReceiver[0] = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
//                    int networkType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);
//                    boolean typeMobile = networkType == ConnectivityManager.TYPE_MOBILE;
//                    NetworkInfo networkInfo = connectivityManager.getNetworkInfo(networkType);
//                    boolean connected = networkInfo.isConnected();
                    boolean connected = isEnabledWithPrivateApi();
                    Logger.trace(TAG, "disable() onReceive, connected = " + connected);
                    if (connected) {
                        return;
                    }
                    if (callbackExecuted.getAndSet(true)) {
                        return;
                    }
                    context.unregisterReceiver(localBroadcastReceiver[0]);
                    waitingFuture[0].cancel(true);
                    if (stateChangedListener != null) {
                        stateChangedListener.onStateChanged(connected);
                    }
                }
            };
            Logger.trace(TAG, "disable() start waitingFuture");
            waitingFuture[0] = scheduledExecutorService.schedule(() -> {
                synchronized (MobileNetworkManager.this) {
                    if (callbackExecuted.getAndSet(true)) {
                        return;
                    }
                    boolean connected = isEnabledWithPrivateApi();
                    Logger.trace(TAG, "disable() onTimeout, connected = " + connected);
                    context.unregisterReceiver(localBroadcastReceiver[0]);
                    if (stateChangedListener != null) {
                        stateChangedListener.onStateChanged(connected);
                    }
                }
            }, RECEIVER_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);

            Logger.trace(TAG, "disable() registerReceiver");
            context.registerReceiver(localBroadcastReceiver[0], new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

            setEnabledWithPrivateApi(false);

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
                Logger.trace(TAG, "Отложенное выключение MobileNetworkManager, deviceId = " + deviceId);
                disableSync();
                return !isEnabled();
            } else {
                Logger.trace(TAG, "MobileNetworkManager используется другим устройством, deviceId != " + deviceId);
                return false;
            }
        }
    }

    private boolean isEnabledWithPrivateApi() {
        try {
            final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
            final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
            getMobileDataEnabledMethod.setAccessible(true);
            return (boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
        } catch (Exception e) {
            Logger.error(TAG, e);
        }
        return false;
    }

    private void setEnabledWithPrivateApi(boolean enabled) {
        try {
            final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(connectivityManager, enabled);
        } catch (Exception e) {
            Logger.error(TAG, e);
        }
    }

    private void checkThread() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new UnsupportedOperationException("Method call on main thread is not allowed");
        }
    }

    /**
     * Вечно* живущий {@link BroadcastReceiver} для отслеживания изменения состояния мобильных данных извне.
     * Вечно - в рамках жизненного цикла {@link MobileNetworkManager}
     */
    private BroadcastReceiver mobileDataBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.trace(TAG, "mobileDataBroadcastReceiver.onReceive()");
        }
    };

    /**
     * {@link Executor}  для запуска операций в отдельном потоке.
     */
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
            1, r -> new Thread(r, "MobileNetworkManagerExecutor")) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            Executors.logErrorAfterExecute(TAG, r, t);
        }
    };
}
