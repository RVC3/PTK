package ru.ppr.cppk.helpers;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.local.repository.CommonSettingsRepository;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.settings.SharedPreferencesUtils;

/**
 * Хранилище общих настроек.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class CommonSettingsStorage {

    private final Object LOCK = new Object();
    private final CommonSettingsRepository commonSettingsRepository;
    private final Globals app;

    private CommonSettings cachedValue;

    @Inject
    public CommonSettingsStorage(CommonSettingsRepository commonSettingsRepository, Globals app) {
        this.commonSettingsRepository = commonSettingsRepository;
        this.app = app;
    }

    public CommonSettings get() {
        CommonSettings local = cachedValue;
        if (local == null) {
            synchronized (LOCK) {
                if (cachedValue == null) {
                    cachedValue = commonSettingsRepository.load();
                }
                local = cachedValue;
            }
        }
        // Возвращаем новый инстанс для избежания несанкционированных изменений объекта
        CommonSettings commonSettings = new CommonSettings();
        commonSettings.setSettings(local.getSettings());
        return commonSettings;
    }

    public void update(CommonSettings commonSettings) {
        synchronized (LOCK) {
            // Обновляем данные в БД
            commonSettingsRepository.update(commonSettings);
            // Обновляем время обновления общих настроек
            SharedPreferencesUtils.setCommonSettingsLastUpdate(app, new Date());
            // Очищаем кеш
            clearCache();
        }
    }

    public void clearCache() {
        synchronized (LOCK) {
            cachedValue = null;
        }
    }
}
