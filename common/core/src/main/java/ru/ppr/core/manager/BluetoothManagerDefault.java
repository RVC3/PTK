package ru.ppr.core.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.core.manager.network.AirplaneModeManager;
import ru.ppr.logger.Logger;

/**
 * Мененджер работы с Bluetooth.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public final class BluetoothManagerDefault implements IBluetoothManager {

    private static final String TAG = Logger.makeLogTag(BluetoothManagerDefault.class);

    private static final int DELAY_FOR_DISABLE_BLUETOOTH_IN_SECONDS = 60;

    private final Context context;
    private final AirplaneModeManager airplaneModeManager;
    private final BluetoothAdapter bluetoothAdapter;
    private final Timer timer;
    private final Map<Object, TimerTask> timerTasksForChangeBluetoothState;

    @Inject
    public BluetoothManagerDefault(Context context, AirplaneModeManager airplaneModeManager) {
        this.context = context.getApplicationContext();
        this.airplaneModeManager = airplaneModeManager;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        context.registerReceiver(bluetoothBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        timer = new Timer("Bluetooth manager");
        timerTasksForChangeBluetoothState = new HashMap<>();
    }

    private BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            if (state == BluetoothAdapter.STATE_ON) {
                Logger.trace(TAG, "onReceive() STATE_ON");
            } else if (state == BluetoothAdapter.STATE_OFF) {
                Logger.trace(TAG, "onReceive() STATE_OFF");
            }
        }
    };

    public void destroy() {
        context.unregisterReceiver(bluetoothBroadcastReceiver);
    }

    @Override
    public boolean isEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    @Override
    public void enable(StateChangedListener stateChangedListener) {
        Logger.trace(TAG, "enable() START");
        //выключим режим в самолете. Но если не получится - ничего страшного, bluetooth все-равно должен работать.
        airplaneModeManager.setEnabled(false);
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Logger.trace(TAG, "enable() registerReceiver");
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                        Logger.trace(TAG, "enable() onReceive, state = " + state);
                        if (state == BluetoothAdapter.STATE_ON) {
                            if (stateChangedListener != null) {
                                stateChangedListener.onStateChanged(true);
                                context.unregisterReceiver(this);
                            }
                        } else if (state == BluetoothAdapter.STATE_OFF) {
                            if (stateChangedListener != null) {
                                stateChangedListener.onStateChanged(false);
                                context.unregisterReceiver(this);
                            }
                        }
                    }
                }, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

                bluetoothAdapter.enable();

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
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                Logger.trace(TAG, "disable() registerReceiver");
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                        Logger.trace(TAG, "disable() onReceive, state = " + state);
                        if (state == BluetoothAdapter.STATE_ON) {
                            if (stateChangedListener != null) {
                                stateChangedListener.onStateChanged(true);
                                context.unregisterReceiver(this);
                            }
                        } else if (state == BluetoothAdapter.STATE_OFF) {
                            if (stateChangedListener != null) {
                                stateChangedListener.onStateChanged(false);
                                context.unregisterReceiver(this);
                            }
                        }
                    }
                }, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

                bluetoothAdapter.disable();

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

    @Override
    public void pair(BluetoothDevice target) {
        Logger.trace(TAG, "pair()");

        if (target == null)
            return;

        try {
            Method method = target.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(target, (Object[]) null);
        } catch (Exception e) {
            Logger.error(TAG, e);
        }
    }

    @Override
    public boolean enableBluetoothForExternalDevice(Object deviceId) {
        synchronized (this) {
            TimerTask timerTask = timerTasksForChangeBluetoothState.get(deviceId);
            if (timerTask != null) {
                timerTask.cancel();
                timer.purge();
            }
            timerTasksForChangeBluetoothState.put(deviceId, null);
            return enableSync();
        }
    }

    @Override
    public boolean disableBluetoothForExternalDevice(Object deviceId, boolean rightNow) {
        synchronized (this) {
            TimerTask timerTask = timerTasksForChangeBluetoothState.get(deviceId);
            if (timerTask != null) {
                timerTask.cancel();
                timer.purge();
            }
            if (rightNow) {
                return disableBluetoothForExternalDeviceRightNow(deviceId);
            } else {
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (BluetoothManagerDefault.this) {
                            timerTasksForChangeBluetoothState.remove(deviceId);
                            if (timerTasksForChangeBluetoothState.isEmpty()) {
                                Logger.trace(TAG, "Отложенное выключение BluetoothHandler, deviceId = " + deviceId);
                                disableSync();
                            } else {
                                Logger.trace(TAG, "BluetoothHandler используется другим устройством, deviceId != " + deviceId);
                            }
                        }
                    }
                };
                timerTasksForChangeBluetoothState.put(deviceId, timerTask);
                timer.schedule(timerTask, DELAY_FOR_DISABLE_BLUETOOTH_IN_SECONDS * 1000);
                return true;
            }
        }
    }

    private boolean disableBluetoothForExternalDeviceRightNow(Object deviceId) {
        synchronized (BluetoothManagerDefault.this) {
            timerTasksForChangeBluetoothState.remove(deviceId);
            if (timerTasksForChangeBluetoothState.isEmpty()) {
                Logger.trace(TAG, "Отложенное выключение BluetoothHandler, deviceId = " + deviceId);
                disableSync();
                return !isEnabled();
            } else {
                Logger.trace(TAG, "BluetoothHandler используется другим устройством, deviceId != " + deviceId);
                return false;
            }
        }
    }
}
