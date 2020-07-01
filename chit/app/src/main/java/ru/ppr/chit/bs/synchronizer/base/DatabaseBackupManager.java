package ru.ppr.chit.bs.synchronizer.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

import ru.ppr.chit.bs.synchronizer.operation.ClearDirOperation;
import ru.ppr.chit.bs.synchronizer.operation.MoveFileOperation;
import ru.ppr.chit.data.db.AbstractDbManager;
import ru.ppr.utils.FileUtils2;

/**
 * Класс с реализацией backup/restore данных, основанных на базах данных
 *
 * @author m.sidorov
 */
public class DatabaseBackupManager implements BackupManager {

    private final AbstractDbManager dbManager;
    private final File backupDir;
    private final String processTitle;
    private final Notifier<String> notifier;

    @Nullable
    private File backupData = null;

    public DatabaseBackupManager(@NonNull AbstractDbManager dbManager,
                                 @NonNull File backupDir,
                                 @NonNull String processTitle,
                                 @NonNull Notifier<String> notifier) {
        this.dbManager = dbManager;
        this.backupDir = backupDir;
        this.processTitle = processTitle;
        this.notifier = notifier;
    }

    @Override
    public void backup() throws SynchronizeException {
        notifier.notify(processTitle + ": создание резервной копии");
        backupData = null;
        try {
            // Очищаем директорию бекапа
            FileUtils2.clearDir(backupDir, null);
            // Получаем путь к файлу БД
            File databaseFile = dbManager.getDatabasePath();
            // Получаем путь к файлу бекапа
            File backupFile = new File(backupDir, databaseFile.getName());
            if (databaseFile.exists()) {
                // Создаем бекап, если файл БД существует
                FileUtils2.copyFile(databaseFile, backupFile, null);
                backupData = backupFile;
            }
        } catch (Exception e) {
            throw new SynchronizeException(processTitle + ": ошибка создания резервной копии", e);
        }
    }

    @Override
    public void restore() throws SynchronizeException {
        if (hasBackup()) {
            notifier.notify(processTitle + ": восстановление резервной копии");
            extractFile(backupData, backupDir);
        }
    }

    @Override
    public boolean hasBackup() {
        // Файл бекапа не существует
        // Нормальная ситуация, когда изначально не было файла БД в ПО
        return backupData != null;
    }

    public void extractFile(File extractedFile, File extractedDir) throws SynchronizeException {
        try {
            dbManager.closeConnection();
            try {
                new MoveFileOperation(new MoveFileOperation.Params(extractedFile, dbManager.getDatabasePath().getParentFile()).setOverrideExistentFile(true)).start();
                new ClearDirOperation(new ClearDirOperation.Params(extractedDir)).start();
            } finally {
                dbManager.openConnection();
            }
        } catch (Exception e) {
            throw new SynchronizeException(processTitle + ": ошибка восстановления базы данных", e);
        }
    }

}
