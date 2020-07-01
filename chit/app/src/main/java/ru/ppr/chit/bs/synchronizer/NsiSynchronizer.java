package ru.ppr.chit.bs.synchronizer;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.ppr.chit.api.request.PreparePacketRdsRequest;
import ru.ppr.chit.api.response.BasePrepareSyncResponse;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManager;
import ru.ppr.chit.bs.synchronizer.base.FileRequestSynchronizer;
import ru.ppr.chit.bs.synchronizer.base.DatabaseBackupManager;
import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.domain.provider.NsiVersionProvider;
import ru.ppr.chit.helpers.FilePathProvider;

/**
 * Класс синхронизации НСИ
 *
 * @author Dmitry Nevolin
 */
public class NsiSynchronizer extends FileRequestSynchronizer {

    private static final String EXTRACTED_FILE_NAME = "nsiDb.db";

    private final ApiManager apiManager;
    private final NsiDbManager nsiDbManager;
    private final DatabaseBackupManager backupManager;
    private final NsiVersionProvider nsiVersionProvider;

    @Inject
    NsiSynchronizer(ApiManager apiManager,
                    FilePathProvider filePathProvider,
                    NsiVersionProvider nsiVersionProvider,
                    NsiDbManager nsiDbManager,
                    SynchronizerInformer synchronizerInformer) {
        super(apiManager,
                new FileRequestSynchronizer.Config(filePathProvider.getNsiSyncDir())
                        .setBackupFileDir(filePathProvider.getNsiSyncBackupDir())
                        .setExtractedFileName(EXTRACTED_FILE_NAME),
                synchronizerInformer);

        this.apiManager = apiManager;
        this.nsiDbManager = nsiDbManager;
        this.nsiVersionProvider = nsiVersionProvider;
        this.backupManager = new DatabaseBackupManager(nsiDbManager, config.getBackupFileDir(), getTitle(), this);
    }

    @Override
    protected Single<BasePrepareSyncResponse> prepareNewRequest() {
        return Single
                .fromCallable(() -> {
                    PreparePacketRdsRequest request = new PreparePacketRdsRequest();
                    request.setCurrentRdsVersion(nsiVersionProvider.getCurrentNsiVersion());
                    notify(String.format("Запрос [%s]: отправка запроса", getTitle()));
                    return request;
                })
                .flatMap(apiManager.api()::preparePacketRds);
    }

    @Override
    protected Completable doApply(File dataFile) {
        return Completable.fromAction(() -> backupManager.extractFile(dataFile, config.getPacketFileDir()));
    }

    @Override
    public BackupManager getBackupManager() {
        return backupManager;
    }

    @Override
    public SynchronizeType getType() {
        return SynchronizeType.SYNC_NSI;
    }

    @Override
    public String getTitle() {
        return "База НСИ";
    }

}
