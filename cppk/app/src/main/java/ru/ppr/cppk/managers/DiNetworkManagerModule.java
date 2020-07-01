package ru.ppr.cppk.managers;

import android.content.Context;

import ru.ppr.core.manager.network.AirplaneModeManager;
import ru.ppr.core.manager.network.MobileNetworkManager;
import ru.ppr.core.manager.network.NetworkManager;
import ru.ppr.core.manager.network.WiFiNetworkManager;
import ru.ppr.cppk.entity.settings.PrivateSettings;

/**
 * DI-модуль для менеджеров работы с сетью.
 *
 * @author Aleksandr Brazhkin
 */
public class DiNetworkManagerModule {

    public AppNetworkManager.NetworkType networkType(PrivateSettings privateSettings) {
        boolean isMobileDataEnabled = privateSettings.isUseMobileDataEnabled();
        return isMobileDataEnabled ? AppNetworkManager.NetworkType.MOBILE : AppNetworkManager.NetworkType.WI_FI;
    }

    public NetworkManager mobileNetworkManager(Context context, AirplaneModeManager airplaneModeManager) {
        return new MobileNetworkManager(context, airplaneModeManager);
    }

    public NetworkManager wifiNetworkManager(Context context, AirplaneModeManager airplaneModeManager) {
        return new WiFiNetworkManager(context, airplaneModeManager);
    }

    public AppNetworkManager networkManager(NetworkManager mobileNetworkManager, NetworkManager wifiNetworkManager, AppNetworkManager.NetworkType networkType) {
        return new AppNetworkManager(mobileNetworkManager, wifiNetworkManager, networkType);
    }
}
