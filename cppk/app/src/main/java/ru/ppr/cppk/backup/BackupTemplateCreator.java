package ru.ppr.cppk.backup;

import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import ru.ppr.core.backup.BackupArchiveCreator;
import ru.ppr.core.backup.BackupTemplateFileRelevanceChecker;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.managers.db.LocalDbManager;
import ru.ppr.cppk.managers.db.NsiDbManager;
import ru.ppr.cppk.managers.db.SecurityDbManager;
import ru.ppr.ikkm.file.db.PrinterSQLiteHelper;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Создатель шаблонов резервных копий.
 *
 * @author Dmitry Nevolin
 */
public class BackupTemplateCreator {

    private static final String TAG = Logger.makeLogTag(BackupTemplateCreator.class);

    private static final String LOCAL_TEMPLATE_FILE_NAME = "local_backup_template.zip";
    private static final String NSI_TEMPLATE_FILE_NAME = "nsi_backup_template.zip";
    private static final String SECURITY_TEMPLATE_FILE_NAME = "security_backup_template.zip";
    private static final String PRINTER_STATE_TEMPLATE_FILE_NAME = "printer_state_backup_template.zip";

    private final BackupArchiveCreator backupArchiveCreator;
    private final BackupTemplateFileRelevanceChecker backupTemplateFileRelevanceChecker;
    private final FilePathProvider filePathProvider;
    private final Globals globals;

    @Inject
    BackupTemplateCreator(BackupArchiveCreator backupArchiveCreator,
                          BackupTemplateFileRelevanceChecker backupTemplateFileRelevanceChecker,
                          FilePathProvider filePathProvider,
                          Globals globals) {
        this.backupArchiveCreator = backupArchiveCreator;
        this.backupTemplateFileRelevanceChecker = backupTemplateFileRelevanceChecker;
        this.filePathProvider = filePathProvider;
        this.globals = globals;
    }

    /**
     * Предоставляет актуальный файл шаблона резервной копии локальной базы.
     *
     * @return файл шаблона локальной базы.
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
     * Предоставляет актуальный файл шаблона резервной копии НСИ.
     *
     * @return файл шаблона НСИ.
     */
    @Nullable
    public File provideNsiDbTemplate() throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        File archiveFile = provideNsiTemplateFile();
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
                return createNsiDbTemplate();
            }
        } else {
            return createNsiDbTemplate();
        }
    }

    /**
     * Предоставляет актуальный файл шаблона резервной копии базы безопасности.
     *
     * @return файл шаблона базы безопасности.
     */
    @Nullable
    public File provideSecurityDbTemplate() throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        File archiveFile = provideSecurityTemplateFile();
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
                return createSecurityDbTemplate();
            }
        } else {
            return createSecurityDbTemplate();
        }
    }

    /**
     * Предоставляет актуальный файл шаблона резервной копии базы состояния принтера.
     *
     * @return файл шаблона состояния принтера.
     */
    @Nullable
    public File provideFilePrinterDbTemplate() throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        File archiveFile = providePrinterStateTemplateFile();
        if (archiveFile.exists()) {
            Map<String, File> archiveFileMap = new HashMap<>();
            fillPrinterStateArchiveFileMap(archiveFileMap);
            // Проверяем соответствие текущего файла базы состояния принтера к файлу из шаблона
            Logger.trace(TAG, "start checkTemplateFileRelevance for printer state");
            if (backupTemplateFileRelevanceChecker.check(archiveFile, archiveFileMap)) {
                Logger.trace(TAG, "printer state relevant, use it");
                return archiveFile;
            } else {
                Logger.trace(TAG, "printer state NOT relevant, create a new template");
                return createFilePrinterDbTemplate();
            }
        } else {
            return createFilePrinterDbTemplate();
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

        File localDb = globals.getDatabasePath(NsiDbManager.DB_NAME);
        if (!localDb.exists()) {
            // Файла БД нет, удаляем шаблон и возвращаем null
            if (archiveFile.exists() && !FileUtils2.deleteFile(archiveFile, null)) {
                Logger.error(TAG,"can't delete localDb template file: " + archiveFile);
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
    private File createNsiDbTemplate() throws IOException, ExecutionException, InterruptedException {
        File archiveFile = provideNsiTemplateFile();
        Map<String, File> archiveFileMap = new HashMap<>();
        fillNsiArchiveFileMap(archiveFileMap);

        File nsiDb = globals.getDatabasePath(NsiDbManager.DB_NAME);
        if (!nsiDb.exists()) {
            // Файла БД нет, удаляем шаблон и возвращаем null
            if (archiveFile.exists() && !FileUtils2.deleteFile(archiveFile, null)) {
                Logger.error(TAG,"can't delete nsiDb template file: " + archiveFile);
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
    private File createSecurityDbTemplate() throws IOException, ExecutionException, InterruptedException {
        File archiveFile = provideSecurityTemplateFile();
        Map<String, File> archiveFileMap = new HashMap<>();
        fillSecurityArchiveFileMap(archiveFileMap);

        File securityDb = globals.getDatabasePath(SecurityDbManager.DB_NAME);
        if (!securityDb.exists()) {
            // Файла БД нет, удаляем шаблон и возвращаем null
            if (archiveFile.exists() && !FileUtils2.deleteFile(archiveFile, null)) {
                Logger.error(TAG,"can't delete securityDb template file: " + archiveFile);
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
     * Создаёт новый шаблон резервной копии базы состояния принтера.
     * Перезаписывает уже имеющийся, если есть.
     *
     * @return созданный файл шаблона базы состояния принтера.
     */
    @Nullable
    private File createFilePrinterDbTemplate() throws IOException, ExecutionException, InterruptedException {
        File archiveFile = providePrinterStateTemplateFile();
        Map<String, File> archiveFileMap = new HashMap<>();
        fillPrinterStateArchiveFileMap(archiveFileMap);

        File filePrinterDb = globals.getDatabasePath(PrinterSQLiteHelper.DB_NAME);
        if (!filePrinterDb.exists()) {
            // Файла БД нет, удаляем шаблон и возвращаем null
            if (archiveFile.exists() && !FileUtils2.deleteFile(archiveFile, null)) {
                Logger.error(TAG,"can't delete filePrinterDb template file: " + archiveFile);
            }
            return null;
        }

        Logger.trace(TAG, "start create printer filePrinterDb template: " + archiveFile.getAbsolutePath());
        BackupArchiveCreator.Config backupArchiveConfig = new BackupArchiveCreator.Config.Builder()
                .setArchiveFile(archiveFile)
                .setArchiveFileMap(archiveFileMap)
                .setComputeHash(true)
                .build();
        backupArchiveCreator.create(backupArchiveConfig);
        Logger.trace(TAG, "complete create printer filePrinterDb template: " + archiveFile.getAbsolutePath());
        return archiveFile;
    }

    /**
     * Заполняет соответствие абсолютных имен файлов в архиве к абсолютным именам файлов для архивации.
     * Предназначен для локальной базы.
     */
    private void fillLocalArchiveFileMap(Map<String, File> archiveFileMap) {
        File local = globals.getDatabasePath(LocalDbManager.DB_NAME);
        archiveFileMap.put(local.getName(), local);
    }

    /**
     * Заполняет соответствие абсолютных имен файлов в архиве к абсолютным именам файлов для архивации.
     * Предназначен для НСИ.
     */
    private void fillNsiArchiveFileMap(Map<String, File> archiveFileMap) {
        File nsi = globals.getDatabasePath(NsiDbManager.DB_NAME);
        archiveFileMap.put(nsi.getName(), nsi);
    }

    /**
     * Заполняет соответствие абсолютных имен файлов в архиве к абсолютным именам файлов для архивации.
     * Предназначен для базы безопасности.
     */
    private void fillSecurityArchiveFileMap(Map<String, File> archiveFileMap) {
        File security = globals.getDatabasePath(SecurityDbManager.DB_NAME);
        archiveFileMap.put(security.getName(), security);
    }

    /**
     * Заполняет соответствие абсолютных имен файлов в архиве к абсолютным именам файлов для архивации.
     * Предназначен для базы состояния принтера.
     */
    private void fillPrinterStateArchiveFileMap(Map<String, File> archiveFileMap) {
        File printerState = globals.getDatabasePath(PrinterSQLiteHelper.DB_NAME);
        archiveFileMap.put(printerState.getName(), printerState);
    }

    /**
     * Предоставляет имя шаблона резервной копии локальной базы.
     */
    private String provideLocalTemplateFileName() {
        return filePathProvider.getBackupsTemplatesDir().getAbsolutePath() + File.separator + LOCAL_TEMPLATE_FILE_NAME;
    }

    /**
     * Предоставляет файл шаблона резервной копии НСИ.
     */
    private File provideNsiTemplateFile() {
        return new File(filePathProvider.getBackupsTemplatesDir(), NSI_TEMPLATE_FILE_NAME);
    }

    /**
     * Предоставляет файл шаблона резервной копии базы безопасности.
     */
    private File provideSecurityTemplateFile() {
        return new File(filePathProvider.getBackupsTemplatesDir(), SECURITY_TEMPLATE_FILE_NAME);
    }

    /**
     * Предоставляет файл шаблона резервной копии базы состояния принтера.
     */
    private File providePrinterStateTemplateFile() {
        return new File(filePathProvider.getBackupsTemplatesDir(), PRINTER_STATE_TEMPLATE_FILE_NAME);
    }

}
