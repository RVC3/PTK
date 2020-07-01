package ru.ppr.core.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.logger.Logger;

/**
 * Менеджер уровня заряда батареи.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class BatteryManager {

    private static final String TAG = Logger.makeLogTag(BatteryManager.class);
    private static final int LOG_INTERVAL_IN_MILLISECONDS = 5000;

    private int chargeLevel;
    private long previousChargeLevelTimestamp;
    private boolean powerConnected;
    private Context context;
    private final Set<BatteryStateListener> batteryStateListeners = new HashSet<>();


    public interface BatteryStateListener {
        void onChargeLevelChanged(int chargeLevel);

        void onPowerConnectedStateChanged(boolean powerConnected);
    }

    @Inject
    public BatteryManager(Context context) {
        Logger.trace(TAG, "BatteryManager initialized");
        this.context = context.getApplicationContext();
        onPowerConnectedStateChanged(isConnected());
        onChargeLevelChanged(getChargeLevel());
        this.context.registerReceiver(this.batteryChangedBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        this.context.registerReceiver(this.powerConnectedBroadcastReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        this.context.registerReceiver(this.powerDisconnectedBroadcastReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
    }

    private void destroy() {
        this.context.unregisterReceiver(this.batteryChangedBroadcastReceiver);
        this.context.unregisterReceiver(this.powerConnectedBroadcastReceiver);
        this.context.unregisterReceiver(this.powerDisconnectedBroadcastReceiver);
        Logger.trace(TAG, "BatteryManager destroyed");
    }

    public void addBatteryStateListener(BatteryStateListener batteryStateListener) {
        batteryStateListeners.add(batteryStateListener);
        batteryStateListener.onPowerConnectedStateChanged(powerConnected);
        batteryStateListener.onChargeLevelChanged(chargeLevel);
    }

    public void removeBatteryStateListener(BatteryStateListener batteryStateListener) {
        batteryStateListeners.remove(batteryStateListener);
    }

    private void onChargeLevelChanged(int chargeLevel) {
        long currentTimeStamp = System.currentTimeMillis();
        // Судя по логам, срабатывает каждую минуту, потому таймер данный особого смысла не имеет пока
        // Но, каждую минуту приходит по 4 события за раз, как правило. Иногда приходит 3.
//        if (this.chargeLevel == chargeLevel
//                && currentTimeStamp - previousChargeLevelTimestamp < LOG_INTERVAL_IN_MILLISECONDS
//                && currentTimeStamp > previousChargeLevelTimestamp) {
//            return;
//        }
//        Logger.trace(TAG, "onChargeLevelChanged: " + chargeLevel);
        previousChargeLevelTimestamp = currentTimeStamp;
        this.chargeLevel = chargeLevel;
        for (BatteryStateListener batteryStateListener : batteryStateListeners) {
            batteryStateListener.onChargeLevelChanged(this.chargeLevel);
        }
    }

    private void onPowerConnectedStateChanged(boolean powerConnected) {
        Logger.trace(TAG, "onPowerConnectedStateChanged: " + powerConnected);
        this.powerConnected = powerConnected;
        for (BatteryStateListener batteryStateListener : batteryStateListeners) {
            batteryStateListener.onPowerConnectedStateChanged(this.powerConnected);
        }
    }

    private BroadcastReceiver batteryChangedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, 0);
            onChargeLevelChanged(level);
        }
    };

    private BroadcastReceiver powerConnectedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onPowerConnectedStateChanged(true);
        }
    };

    private BroadcastReceiver powerDisconnectedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onPowerConnectedStateChanged(false);
        }
    };

    private boolean isConnected() {
        return isConnected1() || isConnected2();
    }

    private boolean isConnected1() {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent != null ? intent.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1) : -1;
        boolean connected = plugged == android.os.BatteryManager.BATTERY_PLUGGED_AC || plugged == android.os.BatteryManager.BATTERY_PLUGGED_USB;
        return connected;
    }

    private boolean isConnected2() {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        boolean connected = intent != null && intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) == android.os.BatteryManager.BATTERY_STATUS_CHARGING;
        return connected;
    }

    private int getChargeLevel() {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int chargeLevel = intent == null ? 0 : intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, 0);
        return chargeLevel;
    }
}
