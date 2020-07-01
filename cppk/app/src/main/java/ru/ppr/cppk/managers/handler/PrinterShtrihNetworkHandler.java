package ru.ppr.cppk.managers.handler;

import android.support.annotation.NonNull;

import ru.ppr.core.manager.network.NetworkManager;
import ru.ppr.shtrih.PrinterShtrih;

/**
 * Класс для работы с сетью для Штриха.
 */
public class PrinterShtrihNetworkHandler implements PrinterShtrih.InternetManager {

    private final NetworkManager networkManager;

    public PrinterShtrihNetworkHandler(@NonNull NetworkManager networkManager) {
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