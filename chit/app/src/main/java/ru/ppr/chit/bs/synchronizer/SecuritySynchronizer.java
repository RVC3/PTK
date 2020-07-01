package ru.ppr.chit.bs.synchronizer;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.ppr.chit.api.request.PreparePacketSecurityRequest;
import ru.ppr.chit.api.response.BasePrepareSyncResponse;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManager;
import ru.ppr.chit.bs.synchronizer.base.DatabaseBackupManager;
import ru.ppr.chit.bs.synchronizer.base.FileRequestSynchronizer;
import ru.ppr.chit.data.db.SecurityDbManager;
import ru.ppr.chit.domain.model.security.SecurityStopListVersion;
import ru.ppr.chit.domain.provider.SecurityVersionProvider;
import ru.ppr.chit.helpers.FilePathProvider;

/**
 * Класс синхронизации базы безопасности
 *
 * @author Dmitry Nevolin
 */
public class SecuritySynchronizer extends FileRequestSynchronizer {

    private static final String EXTRACTED_FILE_NAME = "securityDb.db";

    private final ApiManager apiManager;
    private final SecurityVersionProvider securityVersionProvider;
    private final DatabaseBackupManager backupManager;

    @Inject
    SecuritySynchronizer(ApiManager apiManager,
                         FilePathProvider filePathProvider,
                         SecurityVersionProvider securityVersionProvider,
                         SecurityDbManager securityDbManager,
                         SynchronizerInformer synchronizerInformer) {
        super(apiManager, new FileRequestSynchronizer.Config(filePathProvider.getSecuritySyncDir())
                        .setBackupFileDir(filePathProvider.getSecuritySyncBackupDir())
                        .setExtractedFileName(EXTRACTED_FILE_NAME),
                synchronizerInformer);

        this.apiManager = apiManager;
        this.securityVersionProvider = securityVersionProvider;
        this.backupManager = new DatabaseBackupManager(securityDbManager, config.getBackupFileDir(), getTitle(), this);
    }

    @Override
    public BackupManager getBackupManager() {
        return backupManager;
    }

    @Override
    protected Single<BasePrepareSyncResponse> prepareNewRequest() {
        return Single
                .fromCallable(() -> {
                    SecurityStopListVersion securityStopListVersion = securityVersionProvider.getSecurityStopListVersion();
                    String ticketWhitelistItemVersion = securityStopListVersion != null ? securityVersionProvider.formatUtcDate(securityStopListVersion.getTicketWhitelistItemVersion()) : null;

                    PreparePacketSecurityRequest request = new PreparePacketSecurityRequest(
                            securityVersionProvider.getCurrentSecurityVersion(), ticketWhitelistItemVersion);
                    notify(String.format("Запрос [%s]: отправка запроса", getTitle()));
                    return request;
                })
                .flatMap(apiManager.api()::preparePacketSecurity);
    }

    @Override
    protected Completable doApply(File dataFile) {
        return Completable.fromAction(() -> backupManager.extractFile(dataFile, config.getPacketFileDir()));
    }

    @Override
    public SynchronizeType getType() {
        return SynchronizeType.SYNC_SECURE;
    }

    @Override
    public String getTitle() {
        return "База секьюрити";
    }

}
