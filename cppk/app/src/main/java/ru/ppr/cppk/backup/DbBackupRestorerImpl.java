package ru.ppr.cppk.backup;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import ru.ppr.core.backup.BackupArchiveUnpacker;
import ru.ppr.core.backup.BaseBackupRestorer;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Базовый класс, восстанавливающий базу данных.
 *
 * @author Grigoriy Kashka
 */
abstract class DbBackupRestorerImpl extends BaseBackupRestorer implements DbBackupRestorer {

    private static final String TAG = Logger.makeLogTag(DbBackupRestorerImpl.class);

    private final FilePathProvider filePathProvider;

    DbBackupRestorerImpl(BackupArchiveUnpacker backupArchiveUnpacker, FilePathProvider filePathProvider) {
        super(backupArchiveUnpacker);
        this.filePathProvider = filePathProvider;
    }

    /**
     * Начинает восстановление полной резервной копии.
     *
     * @return true если успешно, false в противном случае.
     */
    public Pair<Boolean, String> start() {
        File backupDir = filePathProvider.getBackupsRestoreDir();
        try {
            boolean restoreDb;
            boolean clearGarbage;
            try {
                // Восстанавливаем НСИ
                Config config = new Config.Builder()
                        .setBackupDir(backupDir)
                        .setUnpackDir(filePathProvider.getBackupsTempDir())
                        .setTemplateFileNameList(provideTemplateFileNameList())
                        .build();
                File restoredBackup = restore(config);
                restoreDb = restore(restoredBackup);
                // Удаляем более ненужную папку с бекапом
                clearGarbage = FileUtils2.deleteDir(restoredBackup, null);
            } catch (ZipException zipException) {
                Logger.error(TAG, zipException);
                throw zipException;
            }
            boolean result = restoreDb &&
                    clearGarbage;
            return Pair.create(result, getLastUnpackArchiveFile() != null ?
                    getLastUnpackArchiveFile().getAbsolutePath() : backupDir.getAbsolutePath());
        } catch (Exception exception) {
            Logger.error(TAG, exception);
            return Pair.create(false, getLastUnpackArchiveFile() != null ?
                    getLastUnpackArchiveFile().getAbsolutePath() : backupDir.getAbsolutePath());
        }
    }

    private List<String> provideTemplateFileNameList() {
        List<String> templateFileNameList = new ArrayList<>();
        templateFileNameList.add(getDbTemplateFileName());
        return templateFileNameList;
    }

    /**
     * Возвращает наименование файла шаблона БД?
     */
    abstract String getDbTemplateFileName();

    /**
     * Производит восстановление бекапа БД.
     *
     * @param backupDir - директория с бекапом
     */
    abstract boolean restore(@NonNull File backupDir);

    /**
     * Производит подмену файла БД.
     *
     * @param dbDir - директория с файлом БД
     */
    abstract Pair<Boolean, String> replace(@NonNull File dbDir);
}
