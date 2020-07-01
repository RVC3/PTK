package ru.ppr.cppk.ui.activity.privateSettingsManagement;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.managers.AppNetworkManager;
import ru.ppr.cppk.ui.activity.base.settings.SettingsManagementActivity;
import ru.ppr.logger.Logger;
import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Dmitry Nevolin
 */
public class PrivateSettingsManagementActivity extends SettingsManagementActivity {

    private static final String TAG = Logger.makeLogTag(PrivateSettingsManagementActivity.class);

    private PrivateSettingsManagementDi di;

    private PrivateSettings privateSettings;

    @NonNull
    @Override
    protected Map<String, Boolean> providedInitialSettingsMap() {
        Map<String, Boolean> initialSettingsMap = new HashMap<>();

        initialSettingsMap.put(PrivateSettings.Entities.IS_MOBILE_CASH_REGISTER, privateSettings.isMobileCashRegister());
        initialSettingsMap.put(PrivateSettings.Entities.IS_OUTPUT_MODE, privateSettings.isOutputMode());
        initialSettingsMap.put(PrivateSettings.Entities.IS_TIME_SYNC_ENABLED, privateSettings.isTimeSyncEnabled());
        initialSettingsMap.put(PrivateSettings.Entities.IS_AUTO_TIME_SYNC_ENABLED, privateSettings.isAutoTimeSyncEnabled());
        initialSettingsMap.put(PrivateSettings.Entities.IS_POS_ENABLED, privateSettings.isPosEnabled());
        initialSettingsMap.put(PrivateSettings.Entities.IS_USE_MOBILE_DATA, privateSettings.isUseMobileDataEnabled());
        initialSettingsMap.put(PrivateSettings.Entities.IS_SALE_ENABLED, privateSettings.isSaleEnabled());
        initialSettingsMap.put(PrivateSettings.Entities.IS_OUTSIDE_PRODUCTION_SECTION_SALE_ENABLED, privateSettings.isOutsideProductionSectionSaleEnabled());
        initialSettingsMap.put(PrivateSettings.Entities.IS_TRANSFER_SALE_ENABLED, privateSettings.isTransferSaleEnabled());

        return initialSettingsMap;
    }

    @NonNull
    @Override
    protected Map<String, String> providedSettingsNameMap() {
        Map<String, String> settingsNameMap = new HashMap<>();

        settingsNameMap.put(PrivateSettings.Entities.IS_MOBILE_CASH_REGISTER,
                "Работает ли ПТК в режиме «Мобильной кассы»?");
        settingsNameMap.put(PrivateSettings.Entities.IS_OUTPUT_MODE,
                "Работает ли ПТК в режиме «Мобильной кассы» на выход?");
        settingsNameMap.put(PrivateSettings.Entities.IS_TIME_SYNC_ENABLED,
                "Включена ли синхронизации времени?");
        settingsNameMap.put(PrivateSettings.Entities.IS_AUTO_TIME_SYNC_ENABLED,
                "Включено ли автоматическое изменение времени превышающего допустимый период изменения текущего времени?");
        settingsNameMap.put(PrivateSettings.Entities.IS_POS_ENABLED,
                "Использовать ли POS терминал?");
        settingsNameMap.put(PrivateSettings.Entities.IS_USE_MOBILE_DATA,
                "Использовать ли мобильные данные?");
        settingsNameMap.put(PrivateSettings.Entities.IS_SALE_ENABLED,
                "Разрешена ли продажа на ПТК");
        settingsNameMap.put(PrivateSettings.Entities.IS_OUTSIDE_PRODUCTION_SECTION_SALE_ENABLED,
                "Разрешение на оформление ПД вне привязанного участка");
        settingsNameMap.put(PrivateSettings.Entities.IS_TRANSFER_SALE_ENABLED,
                "Разрешена ли продажа трансфера на ПТК");

        return settingsNameMap;
    }

    @Override
    protected void onSettingChanged(@NonNull String name, @NonNull Boolean value) {
        privateSettings.getSettings().put(name, String.valueOf(value));
    }

    @NonNull
    @Override
    protected String providedSettingsTitle() {
        return getString(R.string.private_settings_management_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_private_settings_management);

        di = new PrivateSettingsManagementDi(Di.INSTANCE);
        privateSettings = di.privateSettings();

        initialize();
    }

    @Override
    protected void applySettingsAndExit() {
        showApplySettingsProgress();
        Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);
        Completable
                .fromAction(() -> di.networkManager().setNetworkType(privateSettings.isUseMobileDataEnabled() ?
                        AppNetworkManager.NetworkType.MOBILE :
                        AppNetworkManager.NetworkType.WI_FI))
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            Logger.trace(TAG, "Change network type completed");

                            finish();
                        },
                        throwable -> {
                            Logger.error(TAG, throwable);

                            finish();
                        });
    }

}
