package ru.ppr.chit.bs.synchronizer;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.UUID;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.ppr.chit.api.entity.PacketStatusEntity;
import ru.ppr.chit.api.request.PreparePacketSftLicenseRequest;
import ru.ppr.chit.api.response.BasePrepareSyncResponse;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManagerStub;
import ru.ppr.chit.bs.synchronizer.base.FileRequestSynchronizer;
import ru.ppr.chit.bs.synchronizer.base.SynchronizeException;
import ru.ppr.chit.bs.synchronizer.operation.CheckUpdatesOperation;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.helpers.FileMapper;
import ru.ppr.chit.helpers.FilePathProvider;
import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.logger.Logger;

/**
 * Класс синхронизации лицензий СФТ
 *
 * @author Dmitry Nevolin
 */
public class SftLicenseSynchronizer extends FileRequestSynchronizer {

    private static final String TAG = Logger.makeLogTag(SftLicenseSynchronizer.class);
    private static final String LAST_LICENSE_REQUEST_ID = "LAST_LICENSE_REQUEST_ID";
    private static final String EXTRACTED_FILE_NAME = "SftLicense";
    private static final long GET_PACKET_STATUS_TIMEOUT = 2; // timeOut запроса лицензий в минутах

    private final ApiManager apiManager;
    private final EdsManagerWrapper edsManager;
    private final BackupManagerStub backupManager = new BackupManagerStub();
    private final AppPropertiesRepository appPropertiesRepository;

    @Inject
    SftLicenseSynchronizer(ApiManager apiManager,
                           FilePathProvider filePathProvider,
                           EdsManagerWrapper edsManager,
                           AppPropertiesRepository appPropertiesRepository,
                           SynchronizerInformer synchronizerInformer) {
        super(apiManager, new Config(filePathProvider.getSftLicenseSyncDir())
                        .setBackupFileDir(filePathProvider.getSftLicenseBackupDir())
                        .setExtractedFileName(EXTRACTED_FILE_NAME)
                        .setExtractToDir(true)
                        .setGetPacketStatusTimeout(GET_PACKET_STATUS_TIMEOUT),
                synchronizerInformer);
        this.apiManager = apiManager;
        this.edsManager = edsManager;
        this.appPropertiesRepository = appPropertiesRepository;
    }

    @Override
    protected Single<UUID> getRequestId() {
        return Single.fromCallable(() -> uuidParse(appPropertiesRepository.readKeyValue(LAST_LICENSE_REQUEST_ID)));
    }

    @Override
    protected Single<BasePrepareSyncResponse> prepareNewRequest() {
        return Single
                .fromCallable(() -> {
                    notify("Запрос лицензии: подготовка файлов");
                    File dirWithLicRequest = edsManager.getEdsDirs().getEdsUtilDstDir();
                    File[] filesInDir = dirWithLicRequest.listFiles();
                    if (filesInDir == null || filesInDir.length == 0) {
                        throw new SynchronizeException("Запрос лицензии: отсутствует файлы запроса лицензии");
                    } else {
                        for (File file : filesInDir) {
                            Logger.trace(TAG, "File in dir with liq request: " + file.getAbsolutePath());
                        }
                        if (filesInDir.length != 1) {
                            throw new SynchronizeException("Запрос лицензии: Надено более 1 файла запроса лицензии");
                        }
                        return filesInDir[0];
                    }
                })
                .map(FileMapper::javaToModel)
                .map(fileEntity -> {
                    notify("Запрос лицензии: формирование запроса");
                    PreparePacketSftLicenseRequest request = new PreparePacketSftLicenseRequest();
                    request.setLicenseRequest(fileEntity);
                    notify("Запрос лицензии: отправка запроса");
                    return request;
                })
                .flatMap(apiManager.api()::preparePacketSftLicense);
    }

    @Override
    protected Completable doApply(File dirWithLicences) {
        return Single
                .fromCallable(() -> {
                    notify("Распаковка файлов лицензии");
                    // Предполагаем, что в архиве в корне лежит файл лицензии
                    // В теории, он там должен быть один
                    String[] filesInDir = dirWithLicences.list();
                    if (filesInDir == null || filesInDir.length == 0) {
                        Logger.trace(TAG, "Licenses not exist");
                        // здесь было бы уместно создать исключение, но даже если сервер пришлет пустой пакет, в конце операции все равно будет проверка лицензии и обработка ошибки
                        notify("Распаковка файлов лицензии: в полученном пакете отсутствуют файлы лицензий");
                        return false;
                    } else {
                        for (String file : filesInDir) {
                            Logger.trace(TAG, "File in dir with licenses: " + file);
                        }
                    }

                    notify("Подстановка файлов лицензии");
                    Logger.trace(TAG, "Подхватываем файлы лицензий из папки " + dirWithLicences.getAbsolutePath());
                    final boolean takeResult = edsManager.takeLicensesBlocking(dirWithLicences);
                    Logger.trace(TAG, "Результат применения лицензий: " + takeResult);

                    return takeResult;
                })
                // Если новые файлы лицензии не были получены, завершаем операцию
                .filter(Boolean.TRUE::equals)
                .flatMapCompletable(licencesTaken -> Completable.fromAction(() -> {
                    // Если лицензии поменялись, то переинициализируем SFT
                    if (licencesTaken) {
                        notify("Инициализация sft");
                        Logger.trace(TAG, "Проверяем состояние sft");
                        boolean pullResult = edsManager.pullEdsCheckerBlocking();
                        Logger.trace(TAG, "Результат проверки состояния sft: " + pullResult);
                    }
                }));
    }

    @Override
    public BackupManager getBackupManager() {
        return backupManager;
    }

    @Override
    @NonNull
    protected BasePrepareSyncResponse onRequestResponse(BasePrepareSyncResponse prepareResponse) throws SynchronizeException {
        BasePrepareSyncResponse result = super.onRequestResponse(prepareResponse);

        // Если запрос отправлен и прошел проверку, то сохраняем ID запроса лицензий
        appPropertiesRepository.writeKeyValue(LAST_LICENSE_REQUEST_ID, result.getRequestId().toString());
        return result;
    }

    @Override
    @NonNull
    protected CheckUpdatesOperation.Result onRequestResult(CheckUpdatesOperation.Result requestResult) throws SynchronizeException {
        // Если запрос больше не в обработке, то удаляем сохраненный запрос лицензий
        // (!!! Это надо делать до стандарьтной обработки статуса ответа)
        if (requestResult.getPacketStatus() != PacketStatusEntity.PROCESS && requestResult.getPacketStatus() != PacketStatusEntity.PENDING_DATA) {
            // Удаляем запрос
            appPropertiesRepository.deleteKeyValue(LAST_LICENSE_REQUEST_ID);
        }
        return super.onRequestResult(requestResult);
    }

    private UUID uuidParse(String value) {
        if (value != null) {
            try {
                return UUID.fromString(value);
            } catch (Exception e) {
                Logger.error(TAG, e);
            }
        }
        return EMPTY_REQUEST_ID;
    }

    @Override
    public SynchronizeType getType() {
        return SynchronizeType.SYNC_SFT_LICENSE;
    }

    @Override
    public String getTitle() {
        return "Лицензии";
    }

}
