package ru.ppr.chit.helpers;

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
    private static final String APP_DIR_NAME = "CHIT";
    /**
     * Папка с образами для виртуальных ридеров
     */
    private static final String IMAGE_DIR_NAME = "Image";
    /**
     * Образы для {@link ru.ppr.barcode.file.BarcodeReaderFile}
     */
    private static final String BARCODE_IMAGE_SCANNER_DIR_NAME = "Barcode";
    /**
     * Образы для {@link ru.ppr.rfid.image.RfidImage}
     */
    private static final String RFID_IMAGE_READER_DIR_NAME = "Rfid";
    /**
     * Папка с логами
     */
    private static final String LOGS_DIR_NAME = "Logs";
    /**
     * Папка с логами приложения
     */
    private static final String APP_LOGS_DIR_NAME = "AppLogs";
    /**
     * Папка с логами Infotecs
     */
    private static final String INFOTECS_LOGS_DIR_NAME = "InfotecsLogs";
    /**
     * Папка с логами крашей приложения
     */
    private static final String CRASH_LOGS_DIR_NAME = "CrashLogs";
    /**
     * Папка резервных копий
     */
    private static final String BACKUP_DIR_NAME = "Backup";
    /**
     * Папка для создания резервных копий
     */
    private static final String CREATE_BACKUP_DIR_NAME = "Create";
    /**
     * Папка для восстановления резервных копий
     */
    private static final String RESTORE_BACKUP_DIR_NAME = "Restore";
    /**
     * Папка для заранее подготовленных резервных копий
     */
    private static final String TEMPLATE_BACKUP_DIR_NAME = "Template";
    /**
     * Папка для временных файлов при работе с бекапами
     */
    private static final String TEMP_BACKUP_DIR_NAME = "Temp";
    /**
     * Папка синхронизации
     */
    private static final String SYNC_DIR_NAME = "Sync";
    /**
     * Папка синхронизации НСИ
     */
    private static final String NSI_SYNC_DIR_NAME = "Nsi";
    /**
     * Папка для бекапов синхронизации НСИ
     */
    private static final String NSI_SYNC_BACKUP_DIR_NAME = "NsiBackup";
    /**
     * Папка синхронизации базы безопасности
     */
    private static final String SECURITY_SYNC_DIR_NAME = "Security";
    /**
     * Папка для бекапов синхронизации базы безопасности
     */
    private static final String SECURITY_SYNC_BACKUP_DIR_NAME = "SecurityBackup";
    /**
     * Папка синхронизации ПО
     */
    private static final String SOFTWARE_SYNC_DIR_NAME = "Software";
    /**
     * Папка синхронизации лицензий СФТ
     */
    private static final String SFT_LICENSE_SYNC_DIR_NAME = "SftLicense";
    /**
     * Папка для бекапов синхронизации лицензий СФТ
     */
    private static final String SFT_LICENSE_SYNC_BACKUP_DIR_NAME = "SftLicenseBackup";
    /**
     * Папка синхронизации конфигов и ключей СФТ
     */
    private static final String SFT_DATA_SYNC_DIR_NAME = "SftData";
    /**
     * Папка для бекапов синхронизации конфигов и ключей СФТ
     */
    private static final String SFT_DATA_SYNC_BACKUP_DIR_NAME = "SftDataBackup";
    /**
     * Папка для СФТ
     */
    private static final String SFT_DIR_NAME = "sft";
    /**
     * Папка для утилиты СФТ
     */
    private static final String SFT_UTIL_DIR_NAME = "sft_util";
    /**
     * Контекст
     */
    private final Context context;
    /**
     * Внешнее хранилище
     */
    private final File externalStorageDir;
    /**
     * Папка приложения
     */
    private final File appDir;

    @Inject
    FilePathProvider(Context context) {
        this.context = context;
        this.externalStorageDir = Environment.getExternalStorageDirectory();
        this.appDir = new File(getExternalStorageDir(), APP_DIR_NAME);
    }

    /**
     * Возвращает внешнее хранилище
     *
     * @return Внешнее хранилище
     */
    public File getExternalStorageDir() {
        return externalStorageDir;
    }

    /**
     * Возвращает папку приложения
     *
     * @return Папка приложения
     */
    public File getAppDir() {
        if (!appDir.exists()) {
            if (!appDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return appDir;
    }

    public File getImageDir() {
        File imageDir = new File(getAppDir(), IMAGE_DIR_NAME);
        if (!imageDir.exists()) {
            if (!imageDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return imageDir;
    }

    /**
     * Возвращает папку с образами для {@link ru.ppr.barcode.file.BarcodeReaderFile}
     *
     * @return Папка с образами
     */
    public File getBarcodeImageDir() {
        File barcodeImageDir = new File(getImageDir(), BARCODE_IMAGE_SCANNER_DIR_NAME);
        if (!barcodeImageDir.exists()) {
            if (!barcodeImageDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return barcodeImageDir;
    }

    /**
     * Возвращает папку с образами для {@link ru.ppr.rfid.image.RfidImage}
     *
     * @return Папка с образами
     */
    public File getRfidImageDir() {
        File rfidImageDir = new File(getImageDir(), RFID_IMAGE_READER_DIR_NAME);
        if (!rfidImageDir.exists()) {
            if (!rfidImageDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return rfidImageDir;
    }

    /**
     * Возвращает папку c логами.
     *
     * @return Папка с логами
     */
    public File getLogsDir() {
        File logsDir = new File(getAppDir(), LOGS_DIR_NAME);
        if (!logsDir.exists()) {
            if (!logsDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return logsDir;
    }

    /**
     * Возвращает папку c логами приложения.
     *
     * @return Папка с логами приложения
     */
    public File getAppLogsDir() {
        File logsDir = new File(getLogsDir(), APP_LOGS_DIR_NAME);
        if (!logsDir.exists()) {
            if (!logsDir.mkdirs()) {
                throw new RuntimeException();
            }
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
            if (!infotecsLogsDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return infotecsLogsDir;
    }

    /**
     * Возвращает папку с логами крашей приложения.
     *
     * @return Папка с логами крашей приложения
     */
    public File getCrashLogsDir() {
        File crashLogsDir = new File(getLogsDir(), CRASH_LOGS_DIR_NAME);
        if (!crashLogsDir.exists()) {
            if (!crashLogsDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return crashLogsDir;
    }

    /**
     * Возвращает папку СФТ.
     *
     * @return Папка СФТ
     */
    public File getSftDir() {
        File sftDir = new File(context.getFilesDir(), SFT_DIR_NAME);
        if (!sftDir.exists()) {
            if (!sftDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return sftDir;
    }

    /**
     * Возвращает папку для утилиты СФТ.
     *
     * @return Папка с утилитой СФТ
     */
    public File getSftUtilDir() {
        File sftUtilDir = new File(getSftDir(), SFT_UTIL_DIR_NAME);
        if (!sftUtilDir.exists()) {
            if (!sftUtilDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return sftUtilDir;
    }

    /**
     * Возвращает папку резервных копий.
     *
     * @return Папка резервных копий
     */
    public File getBackupDir() {
        File backupDir = new File(getAppDir(), BACKUP_DIR_NAME);
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return backupDir;
    }

    /**
     * Возвращает папку для создания резервных копий.
     *
     * @return Папка для создания резервных копий
     */
    public File getCreateBackupDir() {
        File createBackupDir = new File(getBackupDir(), CREATE_BACKUP_DIR_NAME);
        if (!createBackupDir.exists()) {
            if (!createBackupDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return createBackupDir;
    }

    /**
     * Возвращает папку для восстановления резервных копий.
     *
     * @return Папка для восстановления резервных копий
     */
    public File getRestoreBackupDir() {
        File restoreBackupDir = new File(getBackupDir(), RESTORE_BACKUP_DIR_NAME);
        if (!restoreBackupDir.exists()) {
            if (!restoreBackupDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return restoreBackupDir;
    }

    /**
     * Возвращает папку для заранее подготовленных резервных копий.
     *
     * @return Папка для заранее подготовленных резервных копий
     */
    public File getTemplateBackupDir() {
        File templateBackupDir = new File(getBackupDir(), TEMPLATE_BACKUP_DIR_NAME);
        if (!templateBackupDir.exists()) {
            if (!templateBackupDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return templateBackupDir;
    }

    /**
     * Возвращает папку для временных файлов при работе с бекапами.
     *
     * @return Папка для временных файлов при работе с бекапами
     */
    public File getTempBackupDir() {
        File tempBackupDir = new File(getBackupDir(), TEMP_BACKUP_DIR_NAME);
        if (!tempBackupDir.exists()) {
            if (!tempBackupDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return tempBackupDir;
    }

    /**
     * Возвращает папку синхронизации.
     *
     * @return Папка синхронизации
     */
    public File getSyncDir() {
        File syncDir = new File(getAppDir(), SYNC_DIR_NAME);
        if (!syncDir.exists()) {
            if (!syncDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return syncDir;
    }

    /**
     * Возвращает папку синхронизации НСИ.
     *
     * @return Папка синхронизации НСИ
     */
    public File getNsiSyncDir() {
        File nsiSyncDir = new File(getSyncDir(), NSI_SYNC_DIR_NAME);
        if (!nsiSyncDir.exists()) {
            if (!nsiSyncDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return nsiSyncDir;
    }

    /**
     * Возвращает папку для бекапов синхронизации НСИ.
     *
     * @return Папка для бекапов синхронизации НСИ
     */
    public File getNsiSyncBackupDir() {
        File nsiSyncDir = new File(getSyncDir(), NSI_SYNC_BACKUP_DIR_NAME);
        if (!nsiSyncDir.exists()) {
            if (!nsiSyncDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return nsiSyncDir;
    }

    /**
     * Возвращает папку синхронизации базы безопасности.
     *
     * @return Папка синхронизации базы безопасности
     */
    public File getSecuritySyncDir() {
        File securitySyncDir = new File(getSyncDir(), SECURITY_SYNC_DIR_NAME);
        if (!securitySyncDir.exists()) {
            if (!securitySyncDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return securitySyncDir;
    }

    /**
     * Возвращает папку для бекапов  синхронизации базы безопасности.
     *
     * @return Папка для бекапов синхронизации базы безопасности
     */
    public File getSecuritySyncBackupDir() {
        File securitySyncDir = new File(getSyncDir(), SECURITY_SYNC_BACKUP_DIR_NAME);
        if (!securitySyncDir.exists()) {
            if (!securitySyncDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return securitySyncDir;
    }

    /**
     * Возвращает папку синхронизации ПО.
     *
     * @return Папка синхронизации ПО
     */
    public File getSoftwareSyncDir() {
        File softwareSyncDir = new File(getSyncDir(), SOFTWARE_SYNC_DIR_NAME);
        if (!softwareSyncDir.exists()) {
            if (!softwareSyncDir.mkdirs()) {
                throw new RuntimeException();
            }
        }
        return softwareSyncDir;
    }

    /**
     * Возвращает папку синхронизации лицензий СФТ.
     *
     * @return Папка синхронизации лицензий СФТ
     */
    public File getSftLicenseSyncDir() {
        File sftLicenseSyncDir = new File(getSyncDir(), SFT_LICENSE_SYNC_DIR_NAME);
        if (!sftLicenseSyncDir.exists()) {
            if (!sftLicenseSyncDir.mkdirs()) {
                Logger.error(TAG, "Dir could not be created: " + sftLicenseSyncDir.getAbsolutePath());
            }
        }
        return sftLicenseSyncDir;
    }

    /**
     * Возвращает папку для бекапов синхронизации лицензий СФТ.
     *
     * @return Папка для бекапов синхронизации лицензий СФТ
     */
    public File getSftLicenseBackupDir() {
        File backupDir = new File(getSyncDir(), SFT_LICENSE_SYNC_BACKUP_DIR_NAME);
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                Logger.error(TAG, "Dir could not be created: " + backupDir.getAbsolutePath());
            }
        }
        return backupDir;
    }

    /**
     * Возвращает папку синхронизации конфигов и ключей СФТ.
     *
     * @return Папка синхронизации конфигов и ключей СФТ
     */
    public File getSftDataSyncDir() {
        File sftDataSyncDir = new File(getSyncDir(), SFT_DATA_SYNC_DIR_NAME);
        if (!sftDataSyncDir.exists()) {
            if (!sftDataSyncDir.mkdirs()) {
                Logger.error(TAG, "Dir could not be created: " + sftDataSyncDir.getAbsolutePath());
            }
        }
        return sftDataSyncDir;
    }

    /**
     * Возвращает папку для бекапов синхронизации конфигов и ключей СФТ.
     *
     * @return Папка для бекапов синхронизации конфигов и ключей СФТ
     */
    public File getSftDataBackupDir() {
        File backupDir = new File(getSyncDir(), SFT_DATA_SYNC_BACKUP_DIR_NAME);
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                Logger.error(TAG, "Dir could not be created: " + backupDir.getAbsolutePath());
            }
        }
        return backupDir;
    }

}
