package ru.ppr.chit.bs.synchronizer;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.ppr.chit.api.request.PreparePacketSoftwareRequest;
import ru.ppr.chit.api.response.BasePrepareSyncResponse;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManager;
import ru.ppr.chit.bs.synchronizer.base.FileRequestSynchronizer;
import ru.ppr.chit.bs.synchronizer.base.SynchronizeException;
import ru.ppr.chit.bs.synchronizer.operation.ClearDirOperation;
import ru.ppr.chit.helpers.FilePathProvider;
import ru.ppr.chit.manager.SoftwareUpdateManager;
import ru.ppr.core.domain.model.ApplicationInfo;

/**
 * Выполняет синхронизацию ПО
 *
 * @author Dmitry Nevolin
 */
public class SoftwareSynchronizer extends FileRequestSynchronizer {

    private static final long GET_PACKET_STATUS_TIMEOUT = 2; // timeOut запроса в минутах

    private final ApiManager apiManager;
    private final ApplicationInfo applicationInfo;
    private final SoftwareBackupManager softwareBackupManager;

    @Inject
    SoftwareSynchronizer(ApiManager apiManager, ApplicationInfo applicationInfo, FilePathProvider filePathProvider, SoftwareUpdateManager softwareUpdateManager, SynchronizerInformer synchronizerInformer) {
        super(apiManager, new FileRequestSynchronizer.Config(filePathProvider.getSoftwareSyncDir())
                        .setExtractedFileName(softwareUpdateManager.getNewSoftwareFileName())
                        .setGetPacketStatusTimeout(GET_PACKET_STATUS_TIMEOUT),
                synchronizerInformer);

        this.apiManager = apiManager;
        this.applicationInfo = applicationInfo;
        this.softwareBackupManager = new SoftwareBackupManager();
    }

    @Override
    protected Single<BasePrepareSyncResponse> prepareNewRequest() {
        return Single
                .fromCallable(() -> {
                    PreparePacketSoftwareRequest request = new PreparePacketSoftwareRequest();
                    request.setCurrentSoftwareVersion(applicationInfo.getVersionName());
                    notify(String.format("Запрос [%s]: отправка запроса", getTitle()));
                    return request;
                })
                .flatMap(apiManager.api()::preparePacketSoftware);
    }

    @Override
    protected Completable doApply(File dataFile) {
        return Completable.complete();
    }

    @Override
    public BackupManager getBackupManager() {
        return softwareBackupManager;
    }

    @Override
    public SynchronizeType getType() {
        return SynchronizeType.SYNC_SOFTWARE;
    }

    @Override
    public String getTitle() {
        return "Обновление ПО";
    }

    public class SoftwareBackupManager implements BackupManager {

        private File backupDir = null;

        @Override
        public void backup() {
            backupDir = config.getPacketFileDir();
        }

        @Override
        public void restore() throws SynchronizeException {
            try {
                ClearDirOperation.clearDir(backupDir);
            } catch (Exception e) {
                throw new SynchronizeException(getTitle() + ": ошибка очистки каталога пакета", e);
            }
        }

        @Override
        public boolean hasBackup() {
            return backupDir != null;
        }

    }

}
