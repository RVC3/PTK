package ru.ppr.core.manager;

import android.bluetooth.BluetoothDevice;

/**
 * Мененджер работы с Bluetooth.
 *
 * @author Aleksandr Brazhkin
 */

public interface IBluetoothManager {

    boolean isEnabled();

    boolean enableBluetoothForExternalDevice(Object deviceId);

    boolean disableBluetoothForExternalDevice(Object deviceId, boolean rightNow);

    void pair(BluetoothDevice target);

    void enable(StateChangedListener stateChangedListener);

    void disable(StateChangedListener stateChangedListener);

    boolean enableSync();

    boolean disableSync();

    interface StateChangedListener {
        void onStateChanged(boolean enabled);
    }
}
