package ru.ppr.cppk.settings.AdditionalSettingsFragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.security.entity.PermissionDvc;

/**
 * Фрагмент настроек "Прочее".
 */
public class SettingOtherFragment extends FragmentParent {

    private ViewGroup wifiConfiguratorLayout;
    private View wifiConfiguratorArrowView;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.additional_setting_other_fragment, null);

        final ViewGroup userInfo = (ViewGroup) view.findViewById(R.id.settingsUserData);
        userInfo.setOnClickListener(v -> showUserData());

        wifiConfiguratorLayout = (ViewGroup) view.findViewById(R.id.start_wifi_configurator);
        wifiConfiguratorLayout.setOnClickListener(v -> changeWiFiSettings());
        wifiConfiguratorArrowView = view.findViewById(R.id.start_wifi_configurator_arrow);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setWifiSettings();
    }

    /**
     * Метод для отображения данных о пользователе.
     */
    private void showUserData() {
        Navigator.navigateToUserInfoActivity(getActivity());
    }

    /**
     * Метод для обновления настроек WiFi.
     */
    private void setWifiSettings() {
        final boolean isCanConfigWiFi = hasPermission(PermissionDvc.ConfigWiFi);

        wifiConfiguratorLayout.setEnabled(isCanConfigWiFi);
        wifiConfiguratorLayout.setBackgroundResource(isCanConfigWiFi ? R.color.white : R.color.gray_inactive);
        wifiConfiguratorArrowView.setVisibility(isCanConfigWiFi ? View.VISIBLE : View.GONE);
    }

    /**
     * Метод для изменения настроек WiFi.
     */
    private void changeWiFiSettings() {
        Navigator.navigateToChangeWiFiSettings(getActivity());
    }

}
