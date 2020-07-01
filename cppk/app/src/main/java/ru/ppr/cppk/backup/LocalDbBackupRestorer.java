package ru.ppr.cppk.backup;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.core.backup.BackupArchiveUnpacker;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.managers.db.LocalDbManager;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Восстанавливает резервную копию локальной базы.
 * Поддерживает новый механизм восстановления, с шаблонами.
 *
 * @author Grigoriy Kashka
 */
public class LocalDbBackupRestorer extends DbBackupRestorerImpl {

    private static final String TAG = Logger.makeLogTag(LocalDbBackupRestorer.class);

    /**
     * Имя файла для восстановления бекапа локальной базы
     */
    private static final String LOCAL_FILE_NAME = "databases/cppkdb.sqlite";
    private static final String TEMPLATE_FILE_NAME = "databases/local_backup_template.zip";

    private final LocalDbManager localDbManager;
    private final Globals app;

    @Inject
    LocalDbBackupRestorer(BackupArchiveUnpacker backupArchiveUnpacker,
                          LocalDbManager localDbManager,
                          FilePathProvider filePathProvider,
                          Globals app) {
        super(backupArchiveUnpacker, filePathProvider);
        this.localDbManager = localDbManager;
        this.app = app;
    }

    @Override
    public String getDbTemplateFileName() {
        return TEMPLATE_FILE_NAME;
    }

    /**
     * Восстанавливает из папки с бекапом локальную базу.
     *
     * @param backupDir папка с бекапом.
     * @return true в случае успеха, false в противном случае.
     */
    @Override
    public boolean restore(@NonNull File backupDir) {
        File local = new File(backupDir, LOCAL_FILE_NAME);
        Logger.trace(TAG, "start restore local: " + local.getAbsolutePath());
        if (!local.exists()) {
            throw new IllegalStateException("file not found in restored backup: " + local.getAbsolutePath());
        }
        try {
            localDbManager.closeConnection();
            FileUtils2.copyFile(local, app.getDatabasePath(LocalDbManager.DB_NAME), null);
            localDbManager.resetDaoSession();
            // Обновляем время обновления общих настроек
            SharedPreferencesUtils.setCommonSettingsLastUpdate(app, new Date());
            Logger.trace(TAG, "complete restore local");
            return true;
        } catch (IOException exception) {
            Logger.error(TAG, exception);
            return false;
        }
    }

    @Override
    public Pair<Boolean, String> replace(@NonNull File dbDir) {
        if (!dbDir.exists()) {
            Logger.error(TAG, "dbDir can't be null or non-existent");
            return Pair.create(false, dbDir.getAbsolutePath());
        }
        File[] dbDirListFiles = dbDir.listFiles();
        if (dbDirListFiles != null && dbDirListFiles.length > 0) {
            File file = dbDirListFiles[0];
            Logger.trace(TAG, "start replace local: " + file.getAbsolutePath());
            if (!file.exists()) {
                Logger.error(TAG, "file not found: " + file.getAbsolutePath());
                return Pair.create(false, file.getAbsolutePath());
            }
            try {
                localDbManager.closeConnection();
                FileUtils2.copyFile(file, app.getDatabasePath(LocalDbManager.DB_NAME), null);
                localDbManager.resetDaoSession();
                Logger.trace(TAG, "complete replace local");
                return Pair.create(true, file.getAbsolutePath());
            } catch (IOException exception) {
                Logger.error(TAG, exception);
                return Pair.create(false, file.getAbsolutePath());
            }
        } else {
            Logger.error(TAG, "dbDir contains no files");
            return Pair.create(false, dbDir.getAbsolutePath());
        }
    }

}