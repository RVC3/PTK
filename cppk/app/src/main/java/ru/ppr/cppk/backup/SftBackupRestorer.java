package ru.ppr.cppk.backup;

import android.util.Pair;

import java.io.File;

import javax.inject.Inject;

import ru.ppr.core.backup.BackupArchiveUnpacker;
import ru.ppr.core.backup.BaseBackupRestorer;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Восстанавливает резервную копию SFT.
 * Поддерживает нвоый механизм восстановления, с шаблонами.
 *
 * @author Grigorii Kashka
 */
public class SftBackupRestorer extends BaseBackupRestorer {

    private static final String TAG = Logger.makeLogTag(SftBackupRestorer.class);

    private final SftRestoreHelper sftRestoreHelper;
    private final FilePathProvider filePathProvider;

    @Inject
    SftBackupRestorer(BackupArchiveUnpacker backupArchiveUnpacker,
                      SftRestoreHelper sftRestoreHelper, FilePathProvider filePathProvider) {
        super(backupArchiveUnpacker);
        this.sftRestoreHelper = sftRestoreHelper;
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
            Config config = new Config.Builder()
                    .setBackupDir(filePathProvider.getBackupsRestoreDir())
                    .setUnpackDir(filePathProvider.getBackupsTempDir())
                    .build();
            File restoredBackup = restore(config);
            Logger.trace(TAG, "instead of restore #CPPKConnect just delete current");
            // Восстанавливаем
            boolean restoreSftIn = sftRestoreHelper.restoreSftIn(restoredBackup);
            Logger.trace(TAG, "skip restore Log");
            Logger.trace(TAG, "skip restore sft OUT");
            boolean restoreSftWorking = sftRestoreHelper.restoreSftWorking(restoredBackup);
            // Удаляем более ненужную папку с бекапом
            boolean clearGarbage = FileUtils2.deleteDir(restoredBackup, null);
            boolean result =
                    restoreSftIn &&
                            restoreSftWorking &&
                            clearGarbage;
            return Pair.create(result, getLastUnpackArchiveFile().getAbsolutePath());
        } catch (Exception exception) {
            Logger.error(TAG, exception);
            return Pair.create(false, getLastUnpackArchiveFile() != null ?
                    getLastUnpackArchiveFile().getAbsolutePath() : backupDir.getAbsolutePath());
        }
    }


}
