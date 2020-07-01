package ru.ppr.cppk.managers.handler;

import android.support.annotation.NonNull;

import ru.ppr.core.manager.IBluetoothManager;
import ru.ppr.ingenico.core.IngenicoTerminal;

public class IngenicoBluetoothHandler implements IngenicoTerminal.BluetoothManager {

    private final IBluetoothManager mBluetoothManager;

    public IngenicoBluetoothHandler(@NonNull final IBluetoothManager bluetoothManager) {
        mBluetoothManager = bluetoothManager;
    }

    @Override
    public boolean enable() {
        return mBluetoothManager.enableBluetoothForExternalDevice(this);
    }

    @Override
    public boolean disable() {
        return mBluetoothManager.disableBluetoothForExternalDevice(this, false);
    }

    @Override
    public boolean isEnabled() {
        return mBluetoothManager.isEnabled();
    }
}