package ru.ppr.core.backup;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import ru.ppr.logger.Logger;
import ru.ppr.utils.MD5Utils;

/**
 * Архитатор файлов для бекапа.
 *
 * @author Dmitry Nevolin
 */
public class BackupArchiveCreator {

    private static final String TAG = Logger.makeLogTag(BackupArchiveCreator.class);

    /**
     * Если файл размером меньше указанного (1 мб), то распараллеливание не использовать.
     * Своего рода костыль для файлов sft, которых очень много, но они мелкие.
     * Нужно это во избежание open failed: EMFILE (Too many open files).
     */
    private static final long MIN_PARALLEL_FILE_SIZE = 1024 * 1024;

    @Inject
    BackupArchiveCreator() {

    }

    /**
     * Создаёт архив с указанной конфигурацией.
     *
     * @param config Конфигурация с данными для работы
     */
    public void create(@NonNull Config config) throws InterruptedException, ExecutionException, IOException {
        Logger.trace(TAG, "start archive create");
        Logger.trace(TAG, "config = " + config);
        ZipArchiveOutputStream archiveFileOutputStream = null;
        ParallelScatterZipCreator archiveCreator = new ParallelScatterZipCreator();
        try {
            archiveFileOutputStream = new ZipArchiveOutputStream(new FileOutputStream(config.archiveFile));
            // Упаковываем шаблоны
            Logger.trace(TAG, "add templates:");
            for (Map.Entry<String, File> entry : config.archiveTemplateMap.entrySet()) {
                File archiveFile = entry.getValue();
                if (archiveFile == null) {
                    throw new IllegalStateException("config.archiveTemplateMap contains null file for mapping: " + entry.getKey());
                }
                if (!archiveFile.exists()) {
                    Logger.warning(TAG, "config.archiveTemplateMap contains non-existent file: " + archiveFile.getAbsolutePath());
                    continue;
                }
                if (archiveFile.isDirectory()) {
                    throw new IllegalStateException("config.archiveTemplateMap contains directory: " + archiveFile.getAbsolutePath());
                }
                ZipArchiveEntry archiveFileEntry = new ZipArchiveEntry(entry.getKey());
                archiveFileEntry.setMethod(ZipArchiveEntry.STORED);
                Logger.trace(TAG, "add template to archive: " + archiveFile.getAbsolutePath());
                //проглотим ошибку если возник Exception при упаковке одного файла
                try {
                    FileInputStream archiveFileInputStream = new FileInputStream(archiveFile);
                    archiveCreator.addArchiveEntry(archiveFileEntry, () -> archiveFileInputStream);
                } catch (Exception e) {
                    Logger.error(TAG, e);
                }
            }
            // Упаковываем всё остальное
            Logger.trace(TAG, "add files:");
            for (Map.Entry<String, File> entry : config.archiveFileMap.entrySet()) {
                File archiveFile = entry.getValue();
                if (archiveFile == null) {
                    throw new IllegalStateException("config.archiveFileMap contains null file for mapping: " + entry.getKey());
                }
                if (!archiveFile.exists()) {
                    throw new IllegalStateException("config.archiveFileMap contains non-existent file: " + archiveFile.getAbsolutePath());
                }
                String archiveFileEntryName = entry.getKey();
                // ZipArchiveEntry требует, чтобы у папок в конце стоял /
                if (archiveFile.isDirectory()) {
                    archiveFileEntryName += File.separator;
                }
                ZipArchiveEntry archiveFileEntry = new ZipArchiveEntry(archiveFileEntryName);
                archiveFileEntry.setMethod(ZipArchiveEntry.DEFLATED);
                // Для папок считать хеш бессмысленно
                if (config.computeHash && !archiveFile.isDirectory()) {
                    Logger.trace(TAG, "compute hash for file: " + archiveFile.getAbsolutePath());
                    //проглотим ошибку для одного файла и перейдем к следующему
                    try {
                        archiveFileEntry.setExtra(MD5Utils.from(archiveFile));
                    } catch (Exception e) {
                        Logger.error(TAG, e);
                        continue;
                    }
                }
                Logger.trace(TAG, "add file to archive: " + archiveFile.getAbsolutePath() + " -> " + archiveFileEntry.getName());
                // Папки помечаем флагом и добавляем сразу, т.к. inputStream на них не открыть.
                // А пустой стрим смотится как костыль.
                if (archiveFile.isDirectory()) {
                    try {
                        archiveFileOutputStream.putArchiveEntry(archiveFileEntry);
                    } catch (Exception e) {
                        Logger.error(TAG, e);
                    } finally {
                        try {
                            archiveFileOutputStream.closeArchiveEntry();
                        } catch (Exception e) {
                            Logger.error(TAG, e);
                        }
                    }
                } else {
                    if (archiveFile.length() < MIN_PARALLEL_FILE_SIZE) {
                        // был прецедент, когда падало на этой строчке, для упаковки файлов SFT:
                        // java.io.FileNotFoundException: open failed: EACCES (Permission denied)
                        // невозможность добавить в бекап один файл не должна блокировать процесс, поэтому обернем в try/catch
                        FileInputStream archiveFileInputStream = null;
                        try {
                            archiveFileOutputStream.putArchiveEntry(archiveFileEntry);
                            archiveFileInputStream = new FileInputStream(archiveFile);
                            //на этом этапе тоже могло что-то пойти не так, поэтому также добавим try/catch/finally
                            IOUtils.copy(archiveFileInputStream, archiveFileOutputStream);
                        } catch (Exception e) {
                            Logger.error(TAG, e);
                        } finally {
                            if (archiveFileInputStream != null) {
                                try {
                                    archiveFileInputStream.close();
                                } catch (Exception e) {
                                    Logger.error(TAG, e);
                                }
                            }
                            try {
                                archiveFileOutputStream.closeArchiveEntry();
                            } catch (Exception e) {
                                Logger.error(TAG, e);
                            }
                        }
                    } else {
                        //проглотим ошибку если возник Exception при упаковке одного файла
                        try {
                            FileInputStream archiveFileInputStream = new FileInputStream(archiveFile);
                            archiveCreator.addArchiveEntry(archiveFileEntry, () -> archiveFileInputStream);
                        } catch (Exception e) {
                            Logger.error(TAG, e);
                        }
                    }
                }
            }
            // Фактическая запись параллельных файлов в архив начинается тут
            Logger.trace(TAG, "start archive write");
            archiveCreator.writeTo(archiveFileOutputStream);
            Logger.trace(TAG, "complete archive create");
        } catch (Exception e) {
            //залогируем stacktrace и бросим исключение
            Logger.error(TAG, e);
            throw e;
        } finally {
            if (archiveFileOutputStream != null) {
                try {
                    archiveFileOutputStream.close();
                } catch (Exception e) {
                    Logger.error(TAG, e);
                }
            }
        }
    }

    /**
     * Конфигурация, содержит все необходимые данные для работы.
     */
    public static class Config {
        /**
         * Файл архива который нужно создать.
         */
        @NonNull
        private final File archiveFile;
        /**
         * Соответствие абсолютных имен файлов в архиве к файлам для архивации.
         */
        @NonNull
        private final Map<String, File> archiveFileMap;
        /**
         * Соответствие абсолютных имен файлов в архиве к файлам-шаблонам.
         * Шаблоны будут помещены в архив без сжатия, к тому же для них не будет рассчитан хеш,
         * даже если стоит флаг {@link #computeHash}, т.к. смысла это не имеет.
         * Файлы-шаблоны не могут быть папками.
         */
        @NonNull
        private final Map<String, File> archiveTemplateMap;
        /**
         * Флаг, указывающий на необходимость расчёта хеша для архивируемых файлов. По-умолчанию отключен.
         */
        private final boolean computeHash;

        Config(@NonNull File archiveFile,
               @NonNull Map<String, File> archiveFileMap,
               @NonNull Map<String, File> archiveTemplateMap,
               boolean computeHash) {
            this.archiveFile = archiveFile;
            this.archiveFileMap = archiveFileMap;
            this.archiveTemplateMap = archiveTemplateMap;
            this.computeHash = computeHash;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "archiveFile = " + archiveFile.getAbsolutePath() +
                    ", archiveFileMap.size = " + archiveFileMap.size() +
                    ", archiveTemplateMap.size = " + archiveTemplateMap.size() +
                    ", computeHash = " + computeHash +
                    '}';
        }

        public static class Builder {
            private File archiveFile;
            private Map<String, File> archiveFileMap;
            private Map<String, File> archiveTemplateMap = Collections.emptyMap();
            private boolean computeHash;

            public Builder setArchiveFile(@NonNull File archiveFile) {
                this.archiveFile = archiveFile;
                return this;
            }

            public Builder setArchiveFileMap(@NonNull Map<String, File> archiveFileMap) {
                this.archiveFileMap = archiveFileMap;
                return this;
            }

            public Builder setArchiveTemplateMap(@NonNull Map<String, File> archiveTemplateMap) {
                this.archiveTemplateMap = archiveTemplateMap;
                return this;
            }

            public Builder setComputeHash(boolean computeHash) {
                this.computeHash = computeHash;
                return this;
            }

            @NonNull
            public Config build() {
                Preconditions.checkNotNull(archiveFile);
                Preconditions.checkNotNull(archiveFileMap);
                Preconditions.checkNotNull(archiveTemplateMap);
                return new Config(archiveFile, archiveFileMap, archiveTemplateMap, computeHash);
            }
        }
    }
}
