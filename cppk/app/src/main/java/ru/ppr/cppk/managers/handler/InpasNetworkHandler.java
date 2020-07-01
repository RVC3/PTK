package ru.ppr.cppk.managers.handler;

import android.support.annotation.NonNull;

import ru.ppr.core.manager.network.NetworkManager;
import ru.ppr.inpas.terminal.InpasTerminal;

/**
 * Класс для работы с сетью Inpas терминала.
 */
public class InpasNetworkHandler implements InpasTerminal.INetworkManager {

    private final NetworkManager networkManager;

    public InpasNetworkHandler(@NonNull NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public void enable() {
        networkManager.enableInternetForExternalDevice(this);
    }

    @Override
    public void disable() {
        networkManager.disableInternetForExternalDevice(this, false);
    }

    @Override
    public boolean isEnabled() {
        return networkManager.isEnabled();
    }
}