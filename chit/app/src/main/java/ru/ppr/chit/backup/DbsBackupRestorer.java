package ru.ppr.chit.backup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.ppr.chit.helpers.FilePathProvider;
import ru.ppr.core.backup.BackupArchiveUnpacker;
import ru.ppr.core.backup.BaseBackupRestorer;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Восстанавливает резервную копию баз.
 *
 * @author Dmitry Nevolin
 */
public class DbsBackupRestorer extends BaseBackupRestorer {

    private static final String TAG = Logger.makeLogTag(DbsBackupRestorer.class);

    private static final String LOCAL_TEMPLATE_FILE_NAME = "databases/local_backup_template.zip";
    private static final String NSI_TEMPLATE_FILE_NAME = "databases/nsi_backup_template.zip";
    private static final String SECURITY_TEMPLATE_FILE_NAME = "databases/security_backup_template.zip";

    private final FilePathProvider filePathProvider;
    private final RestoreHelper restoreHelper;

    @Inject
    DbsBackupRestorer(BackupArchiveUnpacker backupArchiveUnpacker,
                      FilePathProvider filePathProvider, RestoreHelper restoreHelper) {
        super(backupArchiveUnpacker);
        this.filePathProvider = filePathProvider;
        this.restoreHelper = restoreHelper;
    }

    /**
     * Начинает восстановление резервной копии баз.
     *
     * @return true если успешно, false в противном случае.
     */
    public boolean start() {
        try {
            Config config = new Config.Builder()
                    .setBackupDir(filePathProvider.getRestoreBackupDir())
                    .setUnpackDir(filePathProvider.getTempBackupDir())
                    .setTemplateFileNameList(provideTemplateFileNameList())
                    .build();
            File restoredBackup = restore(config);
            // Восстанавливаем базы
            boolean restoreLocal = restoreHelper.restoreLocal(restoredBackup);
            boolean restoreNsi = restoreHelper.restoreNsi(restoredBackup);
            boolean restoreSecurity = restoreHelper.restoreSecurity(restoredBackup);
            // Удаляем более ненужную папку с бекапом
            boolean clearGarbage = FileUtils2.deleteDir(restoredBackup, null);
            return restoreLocal && restoreNsi && restoreSecurity && clearGarbage;
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
                        return Completable.error(new RuntimeException("restoring dbs backup is failed"));
                    }
                });
    }

    private List<String> provideTemplateFileNameList() {
        List<String> templateFileNameList = new ArrayList<>();
        templateFileNameList.add(LOCAL_TEMPLATE_FILE_NAME);
        templateFileNameList.add(NSI_TEMPLATE_FILE_NAME);
        templateFileNameList.add(SECURITY_TEMPLATE_FILE_NAME);
        return templateFileNameList;
    }

}
