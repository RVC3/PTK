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
import ru.ppr.cppk.export.Exchange;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.logger.Logger;
import ru.ppr.utils.MtpUtils;

/**
 * Создаёт полную резервную копию.
 * Использует новый механизм создания, с шаблонами.
 *
 * @author Dmitry Nevolin
 */
public class FullBackupCreator extends PtkBackupCreator {

    private static final String TAG = Logger.makeLogTag(FullBackupCreator.class);

    private static final String BACKUP_TYPE = "full";
    private static final String SHARED_PREFS_DIR_NAME = "shared_prefs";

    private static final String CPPK_CONNECT_ARCHIVE_FILE_LIST_DIR_NAME = "#CPPKConnect";
    private static final String DBS_ARCHIVE_FILE_LIST_DIR_NAME = "databases";
    private static final String FEEDBACK_ARCHIVE_FILE_LIST_DIR_NAME = "Feedback";
    private static final String IN_ARCHIVE_FILE_LIST_DIR_NAME = "in";
    private static final String LOGS_ARCHIVE_FILE_LIST_DIR_NAME = "Log";
    private static final String OUT_ARCHIVE_FILE_LIST_DIR_NAME = "out";
    private static final String SHARED_PREFS_ARCHIVE_FILE_LIST_DIR_NAME = "shared_prefs";
    private static final String WORKING_PREFS_ARCHIVE_FILE_LIST_DIR_NAME = "working";

    private final BackupTemplateCreator backupTemplateCreator;
    private final EdsManager edsManager;
    private final Globals globals;

    @Inject
    FullBackupCreator(BackupArchiveCreator backupArchiveCreator,
                      BackupNameGenerator backupNameGenerator,
                      BackupTemplateCreator backupTemplateCreator,
                      EdsManager edsManager,
                      FilePathProvider filePathProvider,
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

    private Map<String, List<File>> provideArchiveFileListMap() {
        Map<String, List<File>> archiveFileListMap = new HashMap<>();
        File target = new File(Exchange.DIR);
        // Некоторые папки могут существовать, в этом случе добавлять их не надо
        if (target.exists()) {
            archiveFileListMap.put(CPPK_CONNECT_ARCHIVE_FILE_LIST_DIR_NAME,
                    Collections.singletonList(target));
        }
        target = new File(PathsConstants.FEEDBACK);
        if (target.exists()) {
            archiveFileListMap.put(FEEDBACK_ARCHIVE_FILE_LIST_DIR_NAME,
                    Collections.singletonList(target));
        }
        archiveFileListMap.put(IN_ARCHIVE_FILE_LIST_DIR_NAME,
                Collections.singletonList(edsManager.getEdsDirs().getEdsTransportInDir()));
        archiveFileListMap.put(LOGS_ARCHIVE_FILE_LIST_DIR_NAME,
                Collections.singletonList(new File(PathsConstants.LOG)));
        archiveFileListMap.put(OUT_ARCHIVE_FILE_LIST_DIR_NAME,
                Collections.singletonList(edsManager.getEdsDirs().getEdsTransportOutDir()));
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
        File nsiDbTemplate = backupTemplateCreator.provideNsiDbTemplate();
        if (nsiDbTemplate != null) {
            archiveFileList.add(nsiDbTemplate);
        }
        File securityDbTemplate = backupTemplateCreator.provideSecurityDbTemplate();
        if (securityDbTemplate != null) {
            archiveFileList.add(securityDbTemplate);
        }
        File filePrinterDbTemplate = backupTemplateCreator.provideFilePrinterDbTemplate();
        if (filePrinterDbTemplate != null) {
            archiveFileList.add(filePrinterDbTemplate);
        }

        return Collections.singletonMap(DBS_ARCHIVE_FILE_LIST_DIR_NAME, archiveFileList);
    }

    @Override
    protected String getBackupType() {
        return BACKUP_TYPE;
    }
}
