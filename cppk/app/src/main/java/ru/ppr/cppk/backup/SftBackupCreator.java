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
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.logger.Logger;
import ru.ppr.utils.MtpUtils;

/**
 * Создаёт резервную копию файлов SFT.
 * Использует новый механизм создания, с шаблонами.
 *
 * @author Grigoriy Kashka
 */
public class SftBackupCreator extends PtkBackupCreator {

    private static final String TAG = Logger.makeLogTag(SftBackupCreator.class);

    private static final String BACKUP_TYPE = "sft";

    private static final String IN_ARCHIVE_FILE_LIST_DIR_NAME = "in";
    private static final String LOGS_ARCHIVE_FILE_LIST_DIR_NAME = "Log";
    private static final String OUT_ARCHIVE_FILE_LIST_DIR_NAME = "out";
    private static final String WORKING_PREFS_ARCHIVE_FILE_LIST_DIR_NAME = "working";

    private final EdsManager edsManager;
    private final Globals globals;

    @Inject
    SftBackupCreator(BackupArchiveCreator backupArchiveCreator,
                     FilePathProvider filePathProvider,
                     BackupNameGenerator backupNameGenerator,
                     EdsManager edsManager,
                     Globals globals,
                     PrivateSettingsHolder privateSettingsHolder) {
        super(backupArchiveCreator, filePathProvider, backupNameGenerator, privateSettingsHolder);
        this.edsManager = edsManager;
        this.globals = globals;
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
        archiveFileListMap.put(IN_ARCHIVE_FILE_LIST_DIR_NAME,
                Collections.singletonList(edsManager.getEdsDirs().getEdsTransportInDir()));
        archiveFileListMap.put(LOGS_ARCHIVE_FILE_LIST_DIR_NAME,
                Collections.singletonList(new File(PathsConstants.LOG)));
        archiveFileListMap.put(OUT_ARCHIVE_FILE_LIST_DIR_NAME,
                Collections.singletonList(edsManager.getEdsDirs().getEdsTransportOutDir()));
        archiveFileListMap.put(WORKING_PREFS_ARCHIVE_FILE_LIST_DIR_NAME,
                Collections.singletonList(edsManager.getEdsDirs().getEdsWorkingDir()));
        return archiveFileListMap;
    }

    @Override
    protected String getBackupType() {
        return BACKUP_TYPE;
    }
}
