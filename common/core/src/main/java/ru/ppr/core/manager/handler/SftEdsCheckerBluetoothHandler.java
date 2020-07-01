package ru.ppr.core.manager.handler;

import android.support.annotation.NonNull;

import ru.ppr.core.manager.IBluetoothManager;
import ru.ppr.edssft.real.RealSftEdsChecker;

public class SftEdsCheckerBluetoothHandler implements RealSftEdsChecker.BluetoothManager {

    private final IBluetoothManager mBluetoothManager;

    public SftEdsCheckerBluetoothHandler(@NonNull final IBluetoothManager bluetoothManager) {
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