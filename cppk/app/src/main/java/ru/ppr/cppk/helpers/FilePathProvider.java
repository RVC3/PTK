package ru.ppr.cppk.helpers;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.logger.Logger;

/**
 * Провайдер путей для рабочих папок/файлов приложения.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class FilePathProvider {

    private static String TAG = Logger.makeLogTag(FilePathProvider.class);

    /**
     * Корневая папка приложения
     */
    private static final String APP_DIR_NAME = "CPPKInternal";
    /**
     * Папка с бекапами баз
     */
    private static final String BACKUPS_DIR_NAME = "Backup";
    /**
     * Папка для временных файлов при создании/разворачивании бекапа
     */
    private static final String BACKUPS_TEMP_DIR_NAME = "Temp";
    /**
     * Папка с шаблонами бекапов
     */
    private static final String BACKUP_TEMPLATES_DIR_NAME = "Template";
    /**
     * Папка для восстанавливаемых бекапов
     */
    private static final String BACKUPS_RESTORE_DIR_NAME = "Restore";
    /**
     * Папка для подменяемых файлов БД
     */
    private static final String BACKUPS_REPLACE_DIR_NAME = "Replace";
    /**
     * Папка с логами приложения
     */
    private static final String LOGS_DIR_NAME = "Log";
    /**
     * Папка с логами Infotecs
     */
    private static final String INFOTECS_LOGS_DIR_NAME = "InfotecsLog";
    /**
     * Папка с логами Штриха
     */
    private static final String SHTRIH_LOGS_DIR_NAME = "ShtrihLog";
    /**
     * Рабочая папка для Штриха
     */
    private static final String SHTRIH_WORKING_DIR_NAME = "Shtrih";
    /**
     * Папка для SFT
     */
    private static final String SFT_DIR_NAME = "sft";
    /**
     * Папка для утилиты SFT
     */
    private static final String SFT_UTIL_DIR_NAME = "sft_util";
    /**
     * Папка для общих настроек
     */
    private static final String COMMON_SETTINGS_DIR_NAME = "common_settings";

    /**
     * Контекст
     */
    private final Context context;
    /**
     * Внешнее хранилище
     */
    private File externalStoragePath;
    /**
     * Папка приложения
     */
    private File appDir;

    @Inject
    public FilePathProvider(Context context) {
        this.context = context.getApplicationContext();
        externalStoragePath = Environment.getExternalStorageDirectory();
        appDir = new File(getSystemExternalStorageDir(), APP_DIR_NAME);
    }

    /**
     * Возвращает системную директорию внешнего хранилища.
     */
    public File getSystemExternalStorageDir() {
        return externalStoragePath;
    }

    /**
     * Возвращает системную директорию изображений.
     */
    public File getSystemExternalPicturesDir() {
        return new File(externalStoragePath, Environment.DIRECTORY_PICTURES);
    }

    /**
     * Возвращает папку приложения
     *
     * @return Папка приложения
     */
    public File getAppDir() {
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        return appDir;
    }

    /**
     * Возвращает папку c бекапами приложения.
     *
     * @return Папка с бекапами
     */
    public File getBackupsDir() {
        File dir = new File(getAppDir(), BACKUPS_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Возвращает папку для временных файлов при создании/разворачивании бекапа
     *
     * @return Папка
     */
    public File getBackupsTempDir() {
        File dir = new File(getBackupsDir(), BACKUPS_TEMP_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Возвращает папку с шаблонами бекапов
     *
     * @return Папка
     */
    public File getBackupsTemplatesDir() {
        File dir = new File(getBackupsDir(), BACKUP_TEMPLATES_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Возвращает папку для восстанавливаемых бекапов
     *
     * @return Папка
     */
    public File getBackupsRestoreDir() {
        File dir = new File(getBackupsDir(), BACKUPS_RESTORE_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Возвращает папку для подменяемых файлов БД
     *
     * @return Папка
     */
    public File getBackupsReplaceDir() {
        File dir = new File(getBackupsDir(), BACKUPS_REPLACE_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Возвращает папку c логами приложения.
     *
     * @return Папка с логами приложения
     */
    public File getLogsDir() {
        File logsDir = new File(getAppDir(), LOGS_DIR_NAME);
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        return logsDir;
    }

    /**
     * Возвращает папку c логами Infotecs.
     *
     * @return Папка с логами Infotecs
     */
    public File getInfotecsLogsDir() {
        File infotecsLogsDir = new File(getLogsDir(), INFOTECS_LOGS_DIR_NAME);
        if (!infotecsLogsDir.exists()) {
            infotecsLogsDir.mkdirs();
        }
        return infotecsLogsDir;
    }

    /**
     * Возвращает папку c логами Штриха.
     *
     * @return Папка с логами Штриха
     */
    public File getShtrihLogsDir() {
        File shtrihLogsDir = new File(getLogsDir(), SHTRIH_LOGS_DIR_NAME);
        if (!shtrihLogsDir.exists()) {
            shtrihLogsDir.mkdirs();
        }
        return shtrihLogsDir;
    }

    /**
     * Возвращает рабочую папку Штриха.
     *
     * @return Рабочая папка Штриха
     */
    public File getShtrihWorkingDir() {
        File shtrihWorkingDir = new File(getAppDir(), SHTRIH_WORKING_DIR_NAME);
        if (!shtrihWorkingDir.exists()) {
            shtrihWorkingDir.mkdirs();
        }
        return shtrihWorkingDir;
    }

    /**
     * Возвращает папку SFT.
     *
     * @return Папка SFT
     */
    public File getSftDir() {
        File sftDir = new File(context.getFilesDir(), SFT_DIR_NAME);
        if (!sftDir.exists()) {
            sftDir.mkdirs();
        }
        return sftDir;
    }

    /**
     * Возвращает папку для утилиты SFT.
     *
     * @return Папка с утилитой SFT
     */
    public File getSftUtilDir() {
        File sftDir = getSftDir();
        File sftUtilDir = new File(sftDir, SFT_UTIL_DIR_NAME);
        if (!sftUtilDir.exists()) {
            sftUtilDir.mkdirs();
        }
        return sftUtilDir;
    }

    /**
     * Возвращает папку с общими настройками.
     *
     * @return Папка с общими настройками
     */
    public File getCommonSettingsDir() {
        File commonSettingsDir = new File(context.getFilesDir(), COMMON_SETTINGS_DIR_NAME);
        if (!commonSettingsDir.exists()) {
            if (!commonSettingsDir.mkdirs()) {
                Logger.error(TAG, "Dir could not be created: " + commonSettingsDir.getAbsolutePath());
            }
        }
        return commonSettingsDir;
    }

}
