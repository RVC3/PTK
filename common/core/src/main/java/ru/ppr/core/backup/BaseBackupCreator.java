package ru.ppr.core.backup;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ru.ppr.logger.Logger;

/**
 * Базовый класс для создания резервных копий. Не претендует на абсолютную универсальность,
 * просто объединяет в себе часто нужный функционал при создании резервных копий.
 *
 * @author Dmitry Nevolin
 */
public abstract class BaseBackupCreator {

    private static final String TAG = Logger.makeLogTag(BaseBackupCreator.class);

    private final BackupArchiveCreator backupArchiveCreator;

    protected BaseBackupCreator(BackupArchiveCreator backupArchiveCreator) {
        this.backupArchiveCreator = backupArchiveCreator;
    }

    /**
     * Начинает создание резервной копии с переданной конфигурацией
     *
     * @param config Конфигурация с данными для работы
     */
    protected void create(@NonNull Config config) throws IOException, ExecutionException, InterruptedException {
        Logger.trace(TAG, "start create backup");
        Logger.trace(TAG, "config = " + config);
        Map<String, File> archiveFileMap = new HashMap<>();
        Map<String, File> archiveTemplateMap = new HashMap<>();
        // Заполняем данные для BackupArchiveCreator
        for (Map.Entry<String, List<File>> entry : config.archiveFileListMap.entrySet()) {
            fillFileMap(archiveFileMap, entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, List<File>> entry : config.archiveTemplateListMap.entrySet()) {
            fillFileMap(archiveTemplateMap, entry.getKey(), entry.getValue());
        }
        BackupArchiveCreator.Config backupArchiveConfig = new BackupArchiveCreator.Config.Builder()
                .setArchiveFile(config.archiveFile)
                .setArchiveFileMap(archiveFileMap)
                .setArchiveTemplateMap(archiveTemplateMap)
                .build();
        backupArchiveCreator.create(backupArchiveConfig);
        Logger.trace(TAG, "complete create backup");
    }

    /**
     * Заполняет соответствие абсолютных имен файлов в архиве к файлам для архивации.
     * Предназначен для списка файлов.
     */
    private void fillFileMap(@NonNull Map<String, File> fileMap, @NonNull String archiveDirName, @NonNull List<File> fileList) {
        for (File file : fileList) {
            if (!file.exists()) {
                throw new IllegalArgumentException("config.archiveFileListMap contains non-existent file: " + file.getAbsolutePath());
            }
            fillFileMap(fileMap, archiveDirName, file);
        }
    }

    /**
     * Заполняет соответствие абсолютных имен файлов в архиве к файлаи для архивации.
     * Предназначен для одного файла.
     */
    private void fillFileMap(@NonNull Map<String, File> fileMap, @NonNull String archiveDirName, @NonNull File file) {
        if (!file.isDirectory()) {
            fileMap.put(appendDirAndFile(archiveDirName, file), file);
        } else {
            File[] dirFileList = file.listFiles();
            if (dirFileList == null || dirFileList.length == 0) {
                fileMap.put(archiveDirName, file);
                return;
            }
            for (File dirFile : dirFileList) {
                if (dirFile.isDirectory()) {
                    fillFileMap(fileMap, appendDirAndFile(archiveDirName, dirFile), dirFile);
                } else {
                    fileMap.put(appendDirAndFile(archiveDirName, dirFile), dirFile);
                }
            }
        }
    }

    /**
     * Склеивает имя директории и файл.
     * Если директория не указана или пустая, вернёт просто имя файла (файл в корне).
     */
    private String appendDirAndFile(String dirName, File file) {
        if (dirName == null || dirName.isEmpty()) {
            return file.getName();
        }
        return dirName + File.separator + file.getName();
    }

    /**
     * Конфигурация, содержит все необходимые данные для работы.
     */
    protected static class Config {
        /**
         * Файл архива, в который будет упакована резервная копия.
         */
        @NonNull
        private final File archiveFile;
        /**
         * Соответствие имен папок, в которые надо упаковать файлы в архиве, к спискам файлов для упаковки.
         */
        @NonNull
        private final Map<String, List<File>> archiveFileListMap;
        /**
         * Соответствие имен папок, в которые надо упаковать файлы в архиве, к спискам файлов-шаблонов для упаковки.
         * Подробнее про шаблоны см. {@link BackupArchiveCreator.Config#archiveTemplateMap}
         */
        @NonNull
        private final Map<String, List<File>> archiveTemplateListMap;

        Config(@NonNull File archiveFile,
               @NonNull Map<String, List<File>> archiveFileListMap,
               @NonNull Map<String, List<File>> archiveTemplateListMap) {
            this.archiveFile = archiveFile;
            this.archiveFileListMap = archiveFileListMap;
            this.archiveTemplateListMap = archiveTemplateListMap;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "archiveFile = " + archiveFile.getAbsolutePath() +
                    ", archiveFileListMap.size =" + archiveFileListMap.size() +
                    ", archiveTemplateListMap.size =" + archiveTemplateListMap.size() +
                    '}';
        }

        public static class Builder {
            private File archiveFile;
            private Map<String, List<File>> archiveFileListMap;
            private Map<String, List<File>> archiveTemplateListMap = Collections.emptyMap();

            public Builder setArchiveFile(@NonNull File archiveFile) {
                this.archiveFile = archiveFile;
                return this;
            }

            public Builder setArchiveFileListMap(@NonNull Map<String, List<File>> archiveFileListMap) {
                this.archiveFileListMap = archiveFileListMap;
                return this;
            }

            public Builder setArchiveTemplateListMap(@NonNull Map<String, List<File>> archiveTemplateListMap) {
                this.archiveTemplateListMap = archiveTemplateListMap;
                return this;
            }

            @NonNull
            public Config build() {
                Preconditions.checkNotNull(archiveFile);
                Preconditions.checkNotNull(archiveFileListMap);
                Preconditions.checkNotNull(archiveTemplateListMap);
                return new Config(archiveFile, archiveFileListMap, archiveTemplateListMap);
            }
        }
    }
}
