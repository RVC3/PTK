package ru.ppr.cppk.managers.handler;

import android.support.annotation.NonNull;

import ru.ppr.core.manager.network.NetworkManager;
import ru.ppr.ingenico.core.IngenicoTerminal;

/**
 * Класс для работы с сетью Ingenico терминала.
 */
public class IngenicoNetworkHandler implements IngenicoTerminal.InternetManager {

    private final NetworkManager networkManager;

    public IngenicoNetworkHandler(@NonNull NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public boolean enable() {
        return networkManager.enableInternetForExternalDevice(this);
    }

    @Override
    public boolean disable() {
        return networkManager.disableInternetForExternalDevice(this, false);
    }

}