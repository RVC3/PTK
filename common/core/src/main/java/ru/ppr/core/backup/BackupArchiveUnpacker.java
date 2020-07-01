package ru.ppr.core.backup;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Рахорхиатор файлов бекапа.
 *
 * @author Dmitry Nevolin
 */
public class BackupArchiveUnpacker {

    private static final String TAG = Logger.makeLogTag(BackupArchiveUnpacker.class);

    @Inject
    BackupArchiveUnpacker() {

    }

    /**
     * Распаковывает архив с указанной конфигурацией.
     *
     * @param config Конфигурация с данными для работы
     * @return Директорию с распакованным содержимым архива.
     */
    @NonNull
    public File unpack(@NonNull Config config) throws IOException {
        if (!config.archiveFile.exists()) {
            throw new IllegalArgumentException("config.archiveFile does not exist");
        }
        if (config.unpackDir != null && config.unpackDir.exists() && !config.unpackDir.isDirectory()) {
            throw new IllegalArgumentException("config.unpackDir must be a directory");
        }
        Logger.trace(TAG, "start archive unpack");
        Logger.trace(TAG, "config = " + config);
        // Если unpackDir не указан или не существует, используем папку где лежит config.archiveFile
        File unpackDir = config.unpackDir;
        if (config.unpackDir == null || !config.unpackDir.exists()) {
            Logger.trace(TAG, "config.unpackDir is null or does not exists, use config.archiveFile parent dir");
            unpackDir = config.archiveFile.getParentFile();
        }
        // Создаём папку, куда будет происходить непосредственно разархивация
        unpackDir = new File(unpackDir, removeExtension(config.archiveFile));
        Logger.trace(TAG, "prepared unpackDir: " + unpackDir.getAbsolutePath());
        if (unpackDir.exists() && !FileUtils2.deleteDir(unpackDir, null)) {
            throw new IOException("failed to delete old dir in unpackDir");
        }
        if (!unpackDir.mkdirs()) {
            throw new IOException("failed to create dir in unpackDir");
        }
        Logger.trace(TAG, "start unpack files");
        unpackZip(config.archiveFile, unpackDir);
        Logger.trace(TAG, "start unpack templates");
        for (String templateFileName : config.templateFileNameList) {
            File templateFile = new File(unpackDir, templateFileName);
            // Если файла не существует, возможно бекап сделан по-старинке. Не должно прерывать работу.
            if (templateFile.exists()) {
                unpackZip(templateFile, templateFile.getParentFile());
            } else {
                Logger.error(TAG, "templateFile does not exists: " + templateFileName +
                        " maybe archive was created with old mechanism, skipping");
            }
        }
        Logger.trace(TAG, "complete archive unpack");
        return unpackDir;
    }

    /**
     * Распаковывает архив в указанную папку
     *
     * @param src    архив
     * @param dstDir папка для распаковки
     */
    private void unpackZip(File src, File dstDir) throws IOException {
        ZipFile srcZip = new ZipFile(src);
        Enumeration<ZipArchiveEntry> srcEntries = srcZip.getEntries();
        Logger.trace(TAG, "unpack: " + src.getAbsolutePath() + " to: " + dstDir.getAbsolutePath());
        try {
            while (srcEntries.hasMoreElements()) {
                ZipArchiveEntry srcEntry = srcEntries.nextElement();
                File dst = new File(dstDir, srcEntry.getName());
                if (!dst.getParentFile().exists() && !dst.getParentFile().mkdirs()) {
                    throw new IllegalStateException("failed to create parent dir for dst: " + dst.getParentFile().getAbsolutePath());
                }
                BufferedInputStream srcEntryInputStream = new BufferedInputStream(srcZip.getInputStream(srcEntry));
                BufferedOutputStream dstOutputStream = new BufferedOutputStream(new FileOutputStream(dst));
                Logger.trace(TAG, "unpack entry: " + srcEntry.getName() + " -> " + dst.getAbsolutePath());
                try {
                    IOUtils.copy(srcEntryInputStream, dstOutputStream);
                } catch (Exception e) {
                    Logger.error(TAG, e);
                    throw e;
                } finally {
                    srcEntryInputStream.close();
                    dstOutputStream.close();
                }
            }
        } finally {
            srcZip.close();
        }
    }

    /**
     * Вычисляет имя файла без расширения.
     * Если его не было, возвращает имя файла как есть.
     * Наличие расширения определяется по наличию точки в имени файла.
     *
     * @param file файл, у которого нужно убрать расширение.
     * @return имя файла без расширения.
     */
    private String removeExtension(File file) {
        String fileName = file.getName();
        if (!fileName.contains(".")) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /**
     * Конфигурация, содержит все необходимые данные для работы.
     */
    public static class Config {
        /**
         * Файл архива который нужно распаковать.
         */
        @NonNull
        private final File archiveFile;
        /**
         * Папка, в которую будет распакован архив.
         * По-умолчанию будет использована папка, где лежит archiveFile.
         * Если указан null или указанный файл не существует, будет использовано значение по-умолчанию.
         * В данной папке будет создана папка с названием архива без расширения.
         */
        @Nullable
        private final File unpackDir;
        /**
         * Список имен файлов-шаблонов в архиве для распаковки.
         * После распаковки архива, каждый файл-шаблон будет распакован в папку, в которой находится сам.
         * Имена должны содержать относительный путь от корня архива, с / начинаться НЕ должны.
         * Подробнее про шаблоны см. {@link BackupArchiveCreator.Config#archiveTemplateMap}
         */
        @NonNull
        private final List<String> templateFileNameList;

        public Config(@NonNull File archiveFile,
                      @Nullable File unpackDir,
                      @NonNull List<String> templateFileNameList) {
            this.archiveFile = archiveFile;
            this.unpackDir = unpackDir;
            this.templateFileNameList = templateFileNameList;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "archiveFile = " + archiveFile.getAbsolutePath() +
                    ", unpackDir = " + (unpackDir == null ? null : unpackDir.getAbsolutePath()) +
                    ", templateFileNameList.size = " + templateFileNameList.size() +
                    '}';
        }

        public static class Builder {
            private File archiveFile;
            private File unpackDir;
            private List<String> templateFileNameList = Collections.emptyList();

            public Builder setArchiveFile(@NonNull File archiveFile) {
                this.archiveFile = archiveFile;
                return this;
            }

            public Builder setUnpackDir(@Nullable File unpackDir) {
                this.unpackDir = unpackDir;
                return this;
            }

            public Builder setTemplateFileNameList(@NonNull List<String> templateFileNameList) {
                this.templateFileNameList = templateFileNameList;
                return this;
            }

            @NonNull
            public Config build() {
                Preconditions.checkNotNull(archiveFile);
                Preconditions.checkNotNull(templateFileNameList);
                return new Config(archiveFile, unpackDir, templateFileNameList);
            }
        }


    }

}
