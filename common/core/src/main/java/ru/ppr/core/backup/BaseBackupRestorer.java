package ru.ppr.core.backup;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ru.ppr.logger.Logger;

/**
 * Базовый класс для восстановления резервных копий. Не претендует на абсолютную универсальность,
 * просто объединяет в себе часто нужный функционал при восстановлении резервных копий.
 *
 * @author Dmitry Nevolin
 */
public abstract class BaseBackupRestorer {

    private static final String TAG = Logger.makeLogTag(BaseBackupRestorer.class);

    private final BackupArchiveUnpacker backupArchiveUnpacker;
    /**
     * Файл, который мы пытались распаковать последний раз.
     * Нужен для вывода в случае ошибки.
     */
    private File lastUnpackArchiveFile;

    protected BaseBackupRestorer(BackupArchiveUnpacker backupArchiveUnpacker) {
        this.backupArchiveUnpacker = backupArchiveUnpacker;
    }

    /**
     * Распаковывает резервную копию с указанной конфигурацией.
     *
     * @param config не должен быть null,
     *               config.backupDir не должна быть null и должна существовать
     *               config.templateFileNameList не должен быть null
     * @return папку с распакованной резервной копией.
     */
    @NonNull
    protected File restore(@NonNull Config config) throws IOException {
        if (!config.backupDir.exists()) {
            throw new IllegalStateException("config.backupDir can't be null or non-existent");
        }
        Logger.trace(TAG, "start unpack backup");
        Logger.trace(TAG, "config = " + config);
        File[] backupDirListFiles = config.backupDir.listFiles();
        if (backupDirListFiles != null && backupDirListFiles.length > 0) {
            lastUnpackArchiveFile = backupDirListFiles[0];
            BackupArchiveUnpacker.Config unpackConfig = new BackupArchiveUnpacker.Config.Builder()
                    .setArchiveFile(lastUnpackArchiveFile)
                    .setUnpackDir(config.unpackDir)
                    .setTemplateFileNameList(config.templateFileNameList)
                    .build();
            File backup = backupArchiveUnpacker.unpack(unpackConfig);
            Logger.trace(TAG, "complete unpack backup");
            return backup;
        } else {
            throw new IllegalStateException("config.backupDir contains no files");
        }
    }

    protected File getLastUnpackArchiveFile() {
        return lastUnpackArchiveFile;
    }

    /**
     * Конфигурация, содержит все необходимые данные для работы.
     */
    public static class Config {
        /**
         * Папка, в которой следует искать резеравную копию.
         * Если файлов в папке много, берется первый попавшийся.
         */
        @NonNull
        private final File backupDir;
        /**
         * Папка, в которую будет распакован архив.
         * Подробнее см. {@link BackupArchiveUnpacker.Config#unpackDir}
         */
        private final File unpackDir;
        /**
         * Список имен файлов-шаблонов в архиве для распаковки.
         * Подробнее см. {@link BackupArchiveUnpacker.Config#templateFileNameList}
         */
        @NonNull
        private final List<String> templateFileNameList;

        public Config(@NonNull File backupDir,
                      @Nullable File unpackDir,
                      @NonNull List<String> templateFileNameList) {
            this.backupDir = backupDir;
            this.unpackDir = unpackDir;
            this.templateFileNameList = templateFileNameList;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "backupDir = " + backupDir.getAbsolutePath() +
                    ", unpackDir = " + (unpackDir == null ? null : unpackDir.getAbsolutePath()) +
                    ", templateFileNameList.size = " + templateFileNameList.size() +
                    '}';
        }

        public static class Builder {
            private File backupDir;
            private File unpackDir;
            private List<String> templateFileNameList = Collections.emptyList();

            public Builder setBackupDir(@NonNull File backupDir) {
                this.backupDir = backupDir;
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
                Preconditions.checkNotNull(backupDir);
                Preconditions.checkNotNull(templateFileNameList);
                return new Config(backupDir, unpackDir, templateFileNameList);
            }
        }

    }

}
