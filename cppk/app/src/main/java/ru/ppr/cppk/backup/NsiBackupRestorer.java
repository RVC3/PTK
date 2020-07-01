package ru.ppr.cppk.backup;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import ru.ppr.core.backup.BackupArchiveUnpacker;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.managers.db.NsiDbManager;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Восстанавливает резервную копию НСИ.
 * Поддерживает новый механизм восстановления, с шаблонами.
 *
 * @author Dmitry Nevolin
 */
public class NsiBackupRestorer extends DbBackupRestorerImpl {

    private static final String TAG = Logger.makeLogTag(NsiBackupRestorer.class);

    /**
     * Имя файла для восстановления бекапа базы НСИ
     */
    private static final String NSI_FILE_NAME = "databases/_ReferenceDatabase.db";

    private static final String TEMPLATE_FILE_NAME = "databases/nsi_backup_template.zip";

    private final NsiDbManager nsiDbManager;
    private final Globals globals;

    @Inject
    NsiBackupRestorer(BackupArchiveUnpacker backupArchiveUnpacker,
                      NsiDbManager nsiDbManager,
                      FilePathProvider filePathProvider,
                      Globals globals) {
        super(backupArchiveUnpacker, filePathProvider);
        this.nsiDbManager = nsiDbManager;
        this.globals = globals;
    }

    @Override
    public String getDbTemplateFileName() {
        return TEMPLATE_FILE_NAME;
    }

    /**
     * Восстанавливает из папки с бекапом НСИ.
     *
     * @param backupDir папка с бекапом.
     * @return true в случае успеха, false в противном случае.
     */
    @Override
    public boolean restore(@NonNull File backupDir) {
        File nsi = new File(backupDir, NSI_FILE_NAME);
        Logger.trace(TAG, "start restore nsi: " + nsi.getAbsolutePath());
        if (!nsi.exists()) {
            throw new IllegalStateException("file not found in restored backup: " + nsi.getAbsolutePath());
        }
        try {
            nsiDbManager.closeConnection();
            FileUtils2.copyFile(nsi, globals.getDatabasePath(NsiDbManager.DB_NAME), null);
            nsiDbManager.resetDaoSession();
            Logger.trace(TAG, "complete restore nsi");
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
            Logger.trace(TAG, "start replace nsi: " + file.getAbsolutePath());
            if (!file.exists()) {
                Logger.error(TAG, "file not found: " + file.getAbsolutePath());
                return Pair.create(false, file.getAbsolutePath());
            }
            try {
                nsiDbManager.closeConnection();
                FileUtils2.copyFile(file, globals.getDatabasePath(NsiDbManager.DB_NAME), null);
                nsiDbManager.resetDaoSession();
                Logger.trace(TAG, "complete replace nsi");
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
