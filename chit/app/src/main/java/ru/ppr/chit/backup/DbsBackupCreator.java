package ru.ppr.chit.backup;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.ppr.chit.domain.model.local.AppProperties;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.helpers.FilePathProvider;
import ru.ppr.core.backup.BackupArchiveCreator;
import ru.ppr.core.backup.BackupNameGenerator;
import ru.ppr.core.backup.BaseBackupCreator;
import ru.ppr.logger.Logger;

/**
 * Создаёт резервную копию баз.
 *
 * @author Dmitry Nevolin
 */
public class DbsBackupCreator extends BaseBackupCreator {

    private static final String TAG = Logger.makeLogTag(DbsBackupCreator.class);

    private static final String BACKUP_TYPE = "dbs";
    private static final String ARCHIVE_FILE_LIST_DIR_NAME = "databases";

    private final FilePathProvider filePathProvider;
    private final BackupNameGenerator backupNameGenerator;
    private final BackupTemplateCreator backupTemplateCreator;
    private final AppPropertiesRepository appPropertiesRepository;

    @Inject
    DbsBackupCreator(BackupArchiveCreator backupArchiveCreator,
                     FilePathProvider filePathProvider,
                     BackupNameGenerator backupNameGenerator,
                     BackupTemplateCreator backupTemplateCreator,
                     AppPropertiesRepository appPropertiesRepository) {
        super(backupArchiveCreator);
        this.filePathProvider = filePathProvider;
        this.backupNameGenerator = backupNameGenerator;
        this.backupTemplateCreator = backupTemplateCreator;
        this.appPropertiesRepository = appPropertiesRepository;
    }

    /**
     * Начинает создание резервной копии баз.
     *
     * @return true если успешно, false в противном случае.
     */
    public boolean start() {
        try {
            Config config = new Config.Builder()
                    .setArchiveFile(provideArchiveFile())
                    .setArchiveFileListMap(provideArchiveFileListMap())
                    .setArchiveTemplateListMap(provideArchiveTemplateMap())
                    .build();
            create(config);
            return true;
        } catch (Exception exception) {
            Logger.error(TAG, exception);
            return false;
        }
    }

    public Completable rxStart() {
        return Single
                .fromCallable(this::start)
                .flatMapCompletable(result -> {
                    if (result) {
                        return Completable.complete();
                    } else {
                        return Completable.error(new RuntimeException("creating dbs backup is failed"));
                    }
                });
    }

    private File provideArchiveFile() {
        AppProperties appProperties = appPropertiesRepository.load();
        String deviceId = String.valueOf(appProperties.getDeviceId());
        return new File(filePathProvider.getCreateBackupDir(), backupNameGenerator.generateTyped(BACKUP_TYPE, deviceId));
    }

    private Map<String, List<File>> provideArchiveFileListMap() {
        return Collections.emptyMap();
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

        return Collections.singletonMap(ARCHIVE_FILE_LIST_DIR_NAME, archiveFileList);
    }

}
