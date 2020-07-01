package ru.ppr.chit.backup;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 * Создаёт резервную копию логов
 *
 * @author Dmitry Nevolin
 */
public class LogsBackupCreator extends BaseBackupCreator {

    private static final String TAG = Logger.makeLogTag(LogsBackupCreator.class);

    private static final String BACKUP_TYPE = "logs";
    private static final String ARCHIVE_FILE_LIST_DIR_NAME = "logs";

    private final FilePathProvider filePathProvider;
    private final BackupNameGenerator backupNameGenerator;
    private final AppPropertiesRepository appPropertiesRepository;

    @Inject
    LogsBackupCreator(BackupArchiveCreator backupArchiveCreator,
                      FilePathProvider filePathProvider,
                      BackupNameGenerator backupNameGenerator,
                      AppPropertiesRepository appPropertiesRepository) {
        super(backupArchiveCreator);
        this.filePathProvider = filePathProvider;
        this.backupNameGenerator = backupNameGenerator;
        this.appPropertiesRepository = appPropertiesRepository;
    }

    /**
     * Начинает создание резервной копии логов
     *
     * @return true если успешно, false в противном случае
     */
    public boolean start() {
        try {
            Config config = new Config.Builder()
                    .setArchiveFile(provideArchiveFile())
                    .setArchiveFileListMap(provideArchiveFileListMap())
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
                        return Completable.error(new RuntimeException("creating logs backup is failed"));
                    }
                });
    }

    private File provideArchiveFile() {
        AppProperties appProperties = appPropertiesRepository.load();
        String deviceId = String.valueOf(appProperties.getDeviceId());
        return new File(filePathProvider.getCreateBackupDir(), backupNameGenerator.generateTyped(BACKUP_TYPE, deviceId));
    }

    private Map<String, List<File>> provideArchiveFileListMap() {
        return Collections.singletonMap(ARCHIVE_FILE_LIST_DIR_NAME, Collections.singletonList(filePathProvider.getLogsDir()));
    }

}
