package ru.ppr.cppk.managers.handler;

import android.support.annotation.NonNull;

import ru.ppr.core.manager.IBluetoothManager;
import ru.ppr.inpas.terminal.InpasTerminal;

public class InpasBluetoothHandler implements InpasTerminal.IBluetoothManager {

    private final IBluetoothManager mBluetoothManager;

    public InpasBluetoothHandler(@NonNull final IBluetoothManager bluetoothManager) {
        mBluetoothManager = bluetoothManager;
    }

    @Override
    public void enable() {
        mBluetoothManager.enableBluetoothForExternalDevice(this);
    }

    @Override
    public void disable() {
        mBluetoothManager.disableBluetoothForExternalDevice(this, false);
    }

    @Override
    public boolean isEnabled() {
        return mBluetoothManager.isEnabled();
    }

}
