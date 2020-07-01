package ru.ppr.cppk.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.utils.CommonSettingsUtils;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Временное хранилище для общих настроек.
 * http://agile.srvdev.ru/browse/CPPKPP-37774
 *
 * @author Aleksandr Brazhkin
 */
public class CommonSettingsTempStorage {

    private static final String TAG = Logger.makeLogTag(CommonSettingsTempStorage.class);

    private static final String TEMP_SETTINGS_FILE_NAME = "common_settings.temp";

    private final File commonSettingsTempFile;

    @Inject
    CommonSettingsTempStorage(FilePathProvider filePathProvider) {
        this.commonSettingsTempFile = new File(filePathProvider.getCommonSettingsDir(), TEMP_SETTINGS_FILE_NAME);
    }

    /**
     * Выполняет сохранение общих настроек во временное хранилище.
     *
     * @param commonSettingsFile xml файл с общими настройками
     * @return {@code true} в случае успешного сохранения, {@code false} иначе
     */
    public boolean save(@NonNull File commonSettingsFile) {
        try {
            return FileUtils2.copyFile(commonSettingsFile, commonSettingsTempFile, null);
        } catch (IOException e) {
            Logger.error(TAG, e);
            return false;
        }
    }

    /**
     * Возвращает общие настройки из временного хранилища.
     *
     * @return Общие настройки или {@code null} в случае их отсутствия или ошибки загрузки
     */
    @Nullable
    public CommonSettings load() {
        try {
            if (!commonSettingsTempFile.exists()) {
                Logger.trace(TAG, "Temp file doesn't exist: " + commonSettingsTempFile.getAbsolutePath());
                return null;
            }
            return CommonSettingsUtils.loadCommonSettingsFromXmlFile(commonSettingsTempFile);
        } catch (Exception e) {
            Logger.error(TAG, e);
            return null;
        }
    }

    /**
     * Очищает хранилище.
     */
    public void clear() {
        if (!commonSettingsTempFile.delete()) {
            Logger.error(TAG, "Could not delete file: " + commonSettingsTempFile.getAbsolutePath());
        }
    }


}
