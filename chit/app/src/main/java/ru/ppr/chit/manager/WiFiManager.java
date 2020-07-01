package ru.ppr.chit.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import ru.ppr.logger.Logger;

/**
 * Менеджер WiFi
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class WiFiManager {

    private static final String TAG = Logger.makeLogTag(WiFiManager.class);

    private final WifiManager wifiManager;
    private final BehaviorSubject<Boolean> wifiAvailablePublisher = BehaviorSubject.create();

    @Inject
    WiFiManager(Context context) {
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        context.registerReceiver(this.WifiStateChangedReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
    }

    public Observable<Boolean> getWifiAvailablePublisher() {
        return wifiAvailablePublisher;
    }

    public boolean isPointAvailable() {
        return getWifiInfo() != null;
    }

    @Nullable
    public String getPointSsid() {
        WifiInfo wifiInfo = getWifiInfo();
        return wifiInfo != null ? wifiInfo.getSSID() : null;
    }

    @Nullable
    private WifiInfo getWifiInfo() {
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                return wifiInfo;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private BroadcastReceiver WifiStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            boolean wifiEnabled = extraWifiState == WifiManager.WIFI_STATE_ENABLED;
            wifiAvailablePublisher.onNext(wifiEnabled);
        }
    };

}
