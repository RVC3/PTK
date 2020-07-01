package ru.ppr.cppk.backup;

import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
 * Создаёт резервную копию для синхронизации.
 * Использует новый механизм создания, с шаблонами.
 *
 * @author Grigoriy Kashka
 */
public class SyncBackupCreator extends PtkBackupCreator {

    private static final String TAG = Logger.makeLogTag(SyncBackupCreator.class);

    private static final String BACKUP_TYPE = "sync";
    private static final String SHARED_PREFS_DIR_NAME = "shared_prefs";

    private static final String DBS_ARCHIVE_FILE_LIST_DIR_NAME = "databases";
    private static final String FEEDBACK_ARCHIVE_FILE_LIST_DIR_NAME = "Feedback";
    private static final String LOGS_ARCHIVE_FILE_LIST_DIR_NAME = "Log";
    private static final String SHARED_PREFS_ARCHIVE_FILE_LIST_DIR_NAME = "shared_prefs";
    private static final String WORKING_PREFS_ARCHIVE_FILE_LIST_DIR_NAME = "working";

    private final BackupTemplateCreator backupTemplateCreator;
    private final EdsManager edsManager;
    private final Globals globals;

    @Inject
    SyncBackupCreator(BackupArchiveCreator backupArchiveCreator,
                      FilePathProvider filePathProvider,
                      BackupNameGenerator backupNameGenerator,
                      BackupTemplateCreator backupTemplateCreator,
                      EdsManager edsManager,
                      Globals globals,
                      PrivateSettingsHolder privateSettingsHolder) {
        super(backupArchiveCreator, filePathProvider, backupNameGenerator, privateSettingsHolder);
        this.backupTemplateCreator = backupTemplateCreator;
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
                    .setArchiveTemplateListMap(provideArchiveTemplateMap())
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

    @Override
    protected String getBackupType() {
        return BACKUP_TYPE;
    }

    private Map<String, List<File>> provideArchiveFileListMap() {
        Map<String, List<File>> archiveFileListMap = new HashMap<>();
        File target = new File(PathsConstants.FEEDBACK);
        if (target.exists()) {
            archiveFileListMap.put(FEEDBACK_ARCHIVE_FILE_LIST_DIR_NAME,
                    Collections.singletonList(target));
        }
        archiveFileListMap.put(LOGS_ARCHIVE_FILE_LIST_DIR_NAME,
                Collections.singletonList(new File(PathsConstants.LOG)));
        archiveFileListMap.put(SHARED_PREFS_ARCHIVE_FILE_LIST_DIR_NAME,
                Collections.singletonList(new File(globals.getApplicationInfo().dataDir, SHARED_PREFS_DIR_NAME)));
        archiveFileListMap.put(WORKING_PREFS_ARCHIVE_FILE_LIST_DIR_NAME,
                Collections.singletonList(edsManager.getEdsDirs().getEdsWorkingDir()));
        return archiveFileListMap;
    }

    private Map<String, List<File>> provideArchiveTemplateMap() throws InterruptedException, ExecutionException, NoSuchAlgorithmException, IOException {
        List<File> archiveFileList = new ArrayList<>();
        File localDbTemplate = backupTemplateCreator.provideLocalDbTemplate();
        if (localDbTemplate != null) {
            archiveFileList.add(localDbTemplate);
        }
        return Collections.singletonMap(DBS_ARCHIVE_FILE_LIST_DIR_NAME, archiveFileList);
    }

}
