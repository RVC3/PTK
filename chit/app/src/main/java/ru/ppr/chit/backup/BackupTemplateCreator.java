package ru.ppr.chit.backup;

import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.db.SecurityDbManager;
import ru.ppr.chit.helpers.FilePathProvider;
import ru.ppr.core.backup.BackupArchiveCreator;
import ru.ppr.core.backup.BackupTemplateFileRelevanceChecker;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Создаёт шаблоны резервных копий.
 *
 * @author Dmitry Nevolin
 */
public class BackupTemplateCreator {

    private static final String TAG = Logger.makeLogTag(BackupTemplateCreator.class);

    private static final String LOCAL_TEMPLATE_FILE_NAME = "local_backup_template.zip";
    private static final String NSI_TEMPLATE_FILE_NAME = "nsi_backup_template.zip";
    private static final String SECURITY_TEMPLATE_FILE_NAME = "security_backup_template.zip";

    private final FilePathProvider filePathProvider;
    private final BackupArchiveCreator backupArchiveCreator;
    private final LocalDbManager localDbManager;
    private final NsiDbManager nsiDbManager;
    private final SecurityDbManager securityDbManager;
    private final BackupTemplateFileRelevanceChecker backupTemplateFileRelevanceChecker;

    @Inject
    BackupTemplateCreator(FilePathProvider filePathProvider,
                          BackupArchiveCreator backupArchiveCreator,
                          LocalDbManager localDbManager,
                          NsiDbManager nsiDbManager,
                          SecurityDbManager securityDbManager,
                          BackupTemplateFileRelevanceChecker backupTemplateFileRelevanceChecker) {
        this.filePathProvider = filePathProvider;
        this.backupArchiveCreator = backupArchiveCreator;
        this.localDbManager = localDbManager;
        this.nsiDbManager = nsiDbManager;
        this.securityDbManager = securityDbManager;
        this.backupTemplateFileRelevanceChecker = backupTemplateFileRelevanceChecker;
    }

    /**
     * Предоставляет актуальный файл шаблона резервной копии локальной базы
     *
     * @return файл шаблона баз
     */
    @Nullable
    public File provideLocalDbTemplate() throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        File archiveFile = new File(provideLocalTemplateFileName());
        if (archiveFile.exists()) {
            Map<String, File> archiveFileMap = new HashMap<>();
            fillLocalArchiveFileMap(archiveFileMap);
            // Проверяем соответствие текущего файла локальной базы к файлу из шаблона
            Logger.trace(TAG, "start checkTemplateFileRelevance for local");
            if (backupTemplateFileRelevanceChecker.check(archiveFile, archiveFileMap)) {
                Logger.trace(TAG, "local relevant, use it");
                return archiveFile;
            } else {
                Logger.trace(TAG, "local NOT relevant, create a new template");
                return createLocalDbTemplate();
            }
        } else {
            return createLocalDbTemplate();
        }
    }

    /**
     * Предоставляет актуальный файл шаблона резервной копии НСИ
     *
     * @return файл шаблона баз
     */
    @Nullable
    public File provideNsiDbTemplate() throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        File archiveFile = new File(provideNsiTemplateFileName());
        if (archiveFile.exists()) {
            Map<String, File> archiveFileMap = new HashMap<>();
            fillNsiArchiveFileMap(archiveFileMap);
            // Проверяем соответствие текущего файла НСИ к файлу из шаблона
            Logger.trace(TAG, "start checkTemplateFileRelevance for nsi");
            if (backupTemplateFileRelevanceChecker.check(archiveFile, archiveFileMap)) {
                Logger.trace(TAG, "nsi relevant, use it");
                return archiveFile;
            } else {
                Logger.trace(TAG, "nsi NOT relevant, create a new template");
                return createNsiTemplate();
            }
        } else {
            return createNsiTemplate();
        }
    }

    /**
     * Предоставляет актуальный файл шаблона резервной копии базы безопасности
     *
     * @return файл шаблона баз
     */
    @Nullable
    public File provideSecurityDbTemplate() throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        File archiveFile = new File(provideSecurityTemplateFileName());
        if (archiveFile.exists()) {
            Map<String, File> archiveFileMap = new HashMap<>();
            fillSecurityArchiveFileMap(archiveFileMap);
            // Проверяем соответствие текущего файла базы безопасности к файлу из шаблона
            Logger.trace(TAG, "start checkTemplateFileRelevance for security");
            if (backupTemplateFileRelevanceChecker.check(archiveFile, archiveFileMap)) {
                Logger.trace(TAG, "security relevant, use it");
                return archiveFile;
            } else {
                Logger.trace(TAG, "security NOT relevant, create a new template");
                return createSecurityTemplate();
            }
        } else {
            return createSecurityTemplate();
        }
    }

    /**
     * Создаёт новый шаблон резервной копии локальной базы.
     * Перезаписывает уже имеющийся, если есть.
     *
     * @return созданный файл шаблона локальной базы.
     */
    @Nullable
    private File createLocalDbTemplate() throws IOException, ExecutionException, InterruptedException {
        File archiveFile = new File(provideLocalTemplateFileName());
        Map<String, File> archiveFileMap = new HashMap<>();
        fillLocalArchiveFileMap(archiveFileMap);

        File localDb = localDbManager.getDatabasePath();
        if (!localDb.exists()) {
            // Файла БД нет, удаляем шаблон и возвращаем null
            if (archiveFile.exists() && !FileUtils2.deleteFile(archiveFile, null)) {
                Logger.error(TAG, "can't delete localDb template file: " + archiveFile);
            }
            return null;
        }

        Logger.trace(TAG, "start create localDb template: " + archiveFile.getAbsolutePath());
        BackupArchiveCreator.Config backupArchiveConfig = new BackupArchiveCreator.Config.Builder()
                .setArchiveFile(archiveFile)
                .setArchiveFileMap(archiveFileMap)
                .setComputeHash(true)
                .build();
        backupArchiveCreator.create(backupArchiveConfig);
        Logger.trace(TAG, "complete create localDb template: " + archiveFile.getAbsolutePath());
        return archiveFile;
    }

    /**
     * Создаёт новый шаблон резервной копии НСИ.
     * Перезаписывает уже имеющийся, если есть.
     *
     * @return созданный файл шаблона НСИ.
     */
    @Nullable
    private File createNsiTemplate() throws IOException, ExecutionException, InterruptedException {
        File archiveFile = new File(provideNsiTemplateFileName());
        Map<String, File> archiveFileMap = new HashMap<>();
        fillNsiArchiveFileMap(archiveFileMap);

        File nsiDb = nsiDbManager.getDatabasePath();
        if (!nsiDb.exists()) {
            // Файла БД нет, удаляем шаблон и возвращаем null
            if (archiveFile.exists() && !FileUtils2.deleteFile(archiveFile, null)) {
                Logger.error(TAG, "can't delete nsiDb template file: " + archiveFile);
            }
            return null;
        }

        Logger.trace(TAG, "start create nsiDb template: " + archiveFile.getAbsolutePath());
        BackupArchiveCreator.Config backupArchiveConfig = new BackupArchiveCreator.Config.Builder()
                .setArchiveFile(archiveFile)
                .setArchiveFileMap(archiveFileMap)
                .setComputeHash(true)
                .build();
        backupArchiveCreator.create(backupArchiveConfig);
        Logger.trace(TAG, "complete create nsiDb template: " + archiveFile.getAbsolutePath());
        return archiveFile;
    }

    /**
     * Создаёт новый шаблон резервной копии базы безопасности.
     * Перезаписывает уже имеющийся, если есть.
     *
     * @return созданный файл шаблона базы безопасности.
     */
    @Nullable
    private File createSecurityTemplate() throws IOException, ExecutionException, InterruptedException {
        File archiveFile = new File(provideSecurityTemplateFileName());
        Map<String, File> archiveFileMap = new HashMap<>();
        fillSecurityArchiveFileMap(archiveFileMap);

        File securityDb = securityDbManager.getDatabasePath();
        if (!securityDb.exists()) {
            // Файла БД нет, удаляем шаблон и возвращаем null
            if (archiveFile.exists() && !FileUtils2.deleteFile(archiveFile, null)) {
                Logger.error(TAG, "can't delete securityDb template file: " + archiveFile);
            }
            return null;
        }

        Logger.trace(TAG, "start create securityDb template: " + archiveFile.getAbsolutePath());
        BackupArchiveCreator.Config backupArchiveConfig = new BackupArchiveCreator.Config.Builder()
                .setArchiveFile(archiveFile)
                .setArchiveFileMap(archiveFileMap)
                .setComputeHash(true)
                .build();
        backupArchiveCreator.create(backupArchiveConfig);
        Logger.trace(TAG, "complete create securityDb template: " + archiveFile.getAbsolutePath());
        return archiveFile;
    }

    /**
     * Заполняет соответствие абсолютных имен файлов в архиве к абсолютным именам файлов для архивации.
     * Предназначен для локальной базы.
     */
    private void fillLocalArchiveFileMap(Map<String, File> archiveFileMap) {
        File local = localDbManager.getDatabasePath();
        archiveFileMap.put(local.getName(), local);
    }

    /**
     * Заполняет соответствие абсолютных имен файлов в архиве к абсолютным именам файлов для архивации.
     * Предназначен для НСИ.
     */
    private void fillNsiArchiveFileMap(Map<String, File> archiveFileMap) {
        File nsi = nsiDbManager.getDatabasePath();
        archiveFileMap.put(nsi.getName(), nsi);
    }

    /**
     * Заполняет соответствие абсолютных имен файлов в архиве к абсолютным именам файлов для архивации.
     * Предназначен для базы безопасности.
     */
    private void fillSecurityArchiveFileMap(Map<String, File> archiveFileMap) {
        File security = securityDbManager.getDatabasePath();
        archiveFileMap.put(security.getName(), security);
    }

    /**
     * Предоставляет имя шаблона резервной копии локальной базы.
     */
    private String provideLocalTemplateFileName() {
        return filePathProvider.getTemplateBackupDir() + File.separator + LOCAL_TEMPLATE_FILE_NAME;
    }

    /**
     * Предоставляет имя шаблона резервной копии НСИ.
     */
    private String provideNsiTemplateFileName() {
        return filePathProvider.getTemplateBackupDir() + File.separator + NSI_TEMPLATE_FILE_NAME;
    }

    /**
     * Предоставляет имя шаблона резервной копии базы безопасности.
     */
    private String provideSecurityTemplateFileName() {
        return filePathProvider.getTemplateBackupDir() + File.separator + SECURITY_TEMPLATE_FILE_NAME;
    }

}
