package ru.ppr.cppk.helpers;

import android.content.Context;

import javax.inject.Inject;

import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.localdb.repository.UpdateEventRepository;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.logger.Logger;

/**
 * Регистратор факта обновления версии ПО.
 * Создается и запускается один раз при запуске приложения.
 *
 * @author Aleksandr Brazhkin
 */
public class AppVersionUpdateRegister {

    protected static final String TAG = Logger.makeLogTag(AppVersionUpdateRegister.class);

    private final LocalDaoSession localDaoSession;
    private final CommonSettingsStorage commonSettingsStorage;
    private final CommonSettingsTempStorage commonSettingsTempStorage;
    private final Context context;
    private final UpdateEventRepository updateEventRepository;

    @Inject
    AppVersionUpdateRegister(LocalDaoSession localDaoSession,
                             CommonSettingsStorage commonSettingsStorage,
                             CommonSettingsTempStorage commonSettingsTempStorage,
                             Context context,
                             UpdateEventRepository updateEventRepository) {
        this.localDaoSession = localDaoSession;
        this.commonSettingsStorage = commonSettingsStorage;
        this.commonSettingsTempStorage = commonSettingsTempStorage;
        this.context = context;
        this.updateEventRepository = updateEventRepository;
    }

    /**
     * Проверяет факт обновления ПО и выполняет необходимые при этом действия.
     */
    public void checkForUpdate() {
        Logger.info(TAG, "checkForUpdate start");

        String currentVersion = BuildConfig.VERSION_NAME;
        UpdateEvent lastUpdateEvent = updateEventRepository.getLastUpdateEvent(UpdateEventType.SW, false);
        String lastVersion = lastUpdateEvent == null ? null : lastUpdateEvent.getVersion();

        Logger.info(TAG, "lastVersion = " + lastVersion + ", currentVersion = " + currentVersion);
        if (!currentVersion.equals(lastVersion)) {
            // Пишем в БД новое событие обновления ПО
            Dagger.appComponent().updateEventCreator().setType(UpdateEventType.SW).create();
            // Устанавливаем флаг факта обновления
            syncSoftwareUpdatedFlag();
            // Подтягиваем общие настройки
            syncCommonSettings();
        }
        Logger.info(TAG, "checkForUpdate end");
    }

    /**
     * Устанавливает флаг необходимости создания ответа об обновлении ПО для кассы.
     */
    private void syncSoftwareUpdatedFlag() {
        SharedPreferencesUtils.setSoftwareUpdatedFlag(context, true);
    }

    /**
     * Применяет общие настройки, полученные ПТК при синхронизации до обновления версии ПО.
     */
    private void syncCommonSettings() {
        CommonSettings tempCommonSettings = commonSettingsTempStorage.load();
        Logger.trace(TAG, " tempCommonSettings = " + tempCommonSettings);
        if (tempCommonSettings == null) {
            return;
        }
        commonSettingsStorage.update(tempCommonSettings);
        commonSettingsTempStorage.clear();
    }
}
