package ru.ppr.cppk.backup;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import ru.ppr.core.backup.BackupArchiveUnpacker;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.ikkm.file.db.PrinterSQLiteHelper;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Восстанавливает резервную копию базы принтера.
 * Поддерживает новый механизм восстановления, с шаблонами.
 *
 * @author Grigoriy Kashka
 */
public class PrinterDbBackupRestorer extends DbBackupRestorerImpl {

    private static final String TAG = Logger.makeLogTag(PrinterDbBackupRestorer.class);

    /**
     * У базы принтера имя файла для поиска в бекапе совпадает с оригинальным, тут нет нужды поддерживать старое имя.
     */
    private static final String PRINTER_STATE_TEMPLATE_FILE_NAME = "databases/" + PrinterSQLiteHelper.DB_NAME;

    private static final String TEMPLATE_FILE_NAME = "databases/printer_state_backup_template.zip";

    private final PrinterManager printerManager;
    private final Globals globals;

    @Inject
    PrinterDbBackupRestorer(BackupArchiveUnpacker backupArchiveUnpacker,
                            PrinterManager printerManager,
                            FilePathProvider filePathProvider,
                            Globals globals) {
        super(backupArchiveUnpacker, filePathProvider);
        this.printerManager = printerManager;
        this.globals = globals;
    }

    @Override
    public String getDbTemplateFileName() {
        return TEMPLATE_FILE_NAME;
    }

    /**
     * Восстанавливает из папки с бекапом базу состояния принтера.
     *
     * @param backupDir папка с бекапом
     * @return true в случае успеха, false в противном случае
     */
    @Override
    public boolean restore(@NonNull File backupDir) {
        File printerState = new File(backupDir, PRINTER_STATE_TEMPLATE_FILE_NAME);
        Logger.trace(TAG, "start restore printer state: " + printerState.getAbsolutePath());
        // В случае с базой состояния принтера ничего не делаем, т.к. её могло не быть на ПТК в момент создания бекапа.
        if (!printerState.exists()) {
            return true;
        }
        try {
            printerManager.getPrinter().disconnect();
            FileUtils2.copyFile(printerState, globals.getDatabasePath(PrinterSQLiteHelper.DB_NAME), null);
            printerManager.updatePrinter();
            Logger.trace(TAG, "complete restore printer state");
            return true;
        } catch (IOException | PrinterException exception) {
            Logger.error(TAG, exception);
            return false;
        }
    }

    @Override
    public Pair<Boolean, String> replace(@NonNull File dbDir) {
        // Восстановление БД Файлового принтера не реализовано
        return Pair.create(false, dbDir.getAbsolutePath());
    }

}
