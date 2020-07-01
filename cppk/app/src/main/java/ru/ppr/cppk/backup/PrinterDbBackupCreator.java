package ru.ppr.cppk.backup;

import android.content.Context;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import ru.ppr.core.backup.BackupArchiveCreator;
import ru.ppr.core.backup.BackupNameGenerator;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;

/**
 * Создаёт резервную копию базы Принтера.
 * Использует новый механизм создания, с шаблонами.
 *
 * @author Grigorii Kashka
 */
public class PrinterDbBackupCreator extends DbBackupCreatorImpl {

    private static final String BACKUP_TYPE = "printer";

    private final BackupTemplateCreator backupTemplateCreator;

    @Inject
    PrinterDbBackupCreator(BackupArchiveCreator backupArchiveCreator,
                           BackupNameGenerator backupNameGenerator,
                           BackupTemplateCreator backupTemplateCreator,
                           FilePathProvider filePathProvider,
                           Context context,
                           PrivateSettingsHolder privateSettingsHolder) {
        super(backupArchiveCreator, backupNameGenerator, filePathProvider, context, privateSettingsHolder);
        this.backupTemplateCreator = backupTemplateCreator;
    }

    @Override
    public String getBackupType() {
        return BACKUP_TYPE;
    }

    @Override
    @Nullable
    public File provideTemplate() throws InterruptedException, ExecutionException, NoSuchAlgorithmException, IOException {
        return backupTemplateCreator.provideFilePrinterDbTemplate();
    }

}
