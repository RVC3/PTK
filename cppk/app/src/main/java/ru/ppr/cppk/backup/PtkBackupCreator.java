package ru.ppr.cppk.backup;

import java.io.File;

import ru.ppr.core.backup.BackupArchiveCreator;
import ru.ppr.core.backup.BackupNameGenerator;
import ru.ppr.core.backup.BaseBackupCreator;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;

/**
 * Базовый класс для создания бекапов ПТК.
 *
 * @author Grigoriy Kashka
 */
abstract class PtkBackupCreator extends BaseBackupCreator {

    protected final FilePathProvider filePathProvider;
    protected final BackupNameGenerator backupNameGenerator;
    protected final PrivateSettingsHolder privateSettingsHolder;

    public PtkBackupCreator(BackupArchiveCreator backupArchiveCreator,
                            FilePathProvider filePathProvider,
                            BackupNameGenerator backupNameGenerator,
                            PrivateSettingsHolder privateSettingsHolder) {
        super(backupArchiveCreator);
        this.filePathProvider = filePathProvider;
        this.backupNameGenerator = backupNameGenerator;
        this.privateSettingsHolder = privateSettingsHolder;
    }

    protected File provideArchiveFile() {
        String terminalNumber = String.valueOf(privateSettingsHolder.get().getTerminalNumber());
        return new File(filePathProvider.getBackupsDir(), backupNameGenerator.generateTyped(getBackupType(), terminalNumber));
    }

    /**
     * Возвращает тип бекапа (текст, идущий в наименование файла бекапа)
     */
    protected abstract String getBackupType();


}
