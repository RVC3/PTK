package ru.ppr.cppk.backup;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import ru.ppr.core.backup.BackupArchiveUnpacker;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.managers.db.SecurityDbManager;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Восстанавливает резервную копию Базы Безопасности.
 * Поддерживает нвоый механизм восстановления, с шаблонами.
 *
 * @author Grigoriy Kashka
 */
public class SecurityBackupRestorer extends DbBackupRestorerImpl {

    private static final String TAG = Logger.makeLogTag(SecurityBackupRestorer.class);

    private static final String SECURITY_TEMPLATE_FILE_NAME = "databases/_SecurityDatabase.db";
    private static final String TEMPLATE_FILE_NAME = "databases/security_backup_template.zip";

    private final SecurityDbManager securityDbManager;
    private final Globals globals;

    @Inject
    SecurityBackupRestorer(BackupArchiveUnpacker backupArchiveUnpacker,
                           SecurityDbManager securityDbManager,
                           FilePathProvider filePathProvider,
                           Globals globals) {
        super(backupArchiveUnpacker, filePathProvider);
        this.securityDbManager = securityDbManager;
        this.globals = globals;
    }

    @Override
    public String getDbTemplateFileName() {
        return TEMPLATE_FILE_NAME;
    }

    /**
     * Восстанавливает из папки с бекапом базу безопасности.
     *
     * @param backupDir папка с бекапом.
     * @return true в случае успеха, false в противном случае.
     */
    @Override
    public boolean restore(@NonNull File backupDir) {
        File security = new File(backupDir, SECURITY_TEMPLATE_FILE_NAME);
        Logger.trace(TAG, "start restore security: " + security.getAbsolutePath());
        if (!security.exists()) {
            throw new IllegalStateException("file not found in restored backup: " + security.getAbsolutePath());
        }
        try {
            securityDbManager.closeConnection();
            FileUtils2.copyFile(security, globals.getDatabasePath(SecurityDbManager.DB_NAME), null);
            securityDbManager.resetDaoSession();
            Logger.trace(TAG, "complete restore security");
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
            Logger.trace(TAG, "start replace security: " + file.getAbsolutePath());
            if (!file.exists()) {
                Logger.error(TAG, "file not found: " + file.getAbsolutePath());
                return Pair.create(false, file.getAbsolutePath());
            }
            try {
                securityDbManager.closeConnection();
                FileUtils2.copyFile(file, globals.getDatabasePath(SecurityDbManager.DB_NAME), null);
                securityDbManager.resetDaoSession();
                Logger.trace(TAG, "complete replace security");
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
