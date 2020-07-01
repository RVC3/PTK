package ru.ppr.cppk.backup;

import android.util.Pair;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import ru.ppr.core.backup.BackupArchiveCreator;
import ru.ppr.core.backup.BackupNameGenerator;
import ru.ppr.core.backup.BaseBackupCreator;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.logger.Logger;
import ru.ppr.utils.MtpUtils;

/**
 * Создаёт резервную копию файлов лога.
 * Использует новый механизм создания, с шаблонами.
 *
 * @author Grigoriy Kashka
 */
public class LogBackupCreator extends BaseBackupCreator {

    private static final String TAG = Logger.makeLogTag(LogBackupCreator.class);

    private static final String BACKUP_TYPE = "log";

    private static final String LOGS_ARCHIVE_FILE_LIST_DIR_NAME = "Log";

    protected final FilePathProvider filePathProvider;
    protected final BackupNameGenerator backupNameGenerator;
    private final Globals globals;

    private PrivateSettingsHolder privateSettingsHolder;

    @Inject
    LogBackupCreator(BackupArchiveCreator backupArchiveCreator,
                     FilePathProvider filePathProvider,
                     BackupNameGenerator backupNameGenerator,
                     Globals globals,
                     PrivateSettingsHolder privateSettingsHolder) {
        super(backupArchiveCreator);
        this.filePathProvider = filePathProvider;
        this.backupNameGenerator = backupNameGenerator;
        this.globals = globals;
        this.privateSettingsHolder = privateSettingsHolder;
    }

    /**
     * Начинает создание полной резервной копии
     *
     * @return true если успешно, false в противном случае
     */
    public Pair<Boolean, String> start() {
        File archiveFile = provideArchiveFile();
        try {
            Config config = new Config.Builder()
                    .setArchiveFile(archiveFile)
                    .setArchiveFileListMap(provideArchiveFileListMap())
                    .build();
            create(config);
            // Иначе windows не увидит файл
            MtpUtils.notifyFileCreated(globals, archiveFile);
            return Pair.create(true, archiveFile.getAbsolutePath());
        } catch (Exception exception) {
            Logger.error(TAG, exception);
            return Pair.create(false, archiveFile.getAbsolutePath());
        }
    }

    private Map<String, List<File>> provideArchiveFileListMap() {
        Map<String, List<File>> archiveFileListMap = new HashMap<>();
        archiveFileListMap.put(LOGS_ARCHIVE_FILE_LIST_DIR_NAME, Collections.singletonList(new File(PathsConstants.LOG)));
        return archiveFileListMap;
    }

    private File provideArchiveFile() {
        String terminalNumber = String.valueOf(privateSettingsHolder.get().getTerminalNumber());
        return new File(filePathProvider.getBackupsDir(), backupNameGenerator.generateTyped(BACKUP_TYPE, terminalNumber));
    }
}
