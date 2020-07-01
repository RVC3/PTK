package ru.ppr.chit.bs.synchronizer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import ru.ppr.chit.api.entity.FileEntity;
import ru.ppr.chit.api.entity.PacketStatusEntity;
import ru.ppr.chit.api.request.PreparePacketSftDataRequest;
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
import ru.ppr.core.manager.eds.TransportOutDirExportFileFilter;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Класс синхронизации конфигов и ключей СФТ
 *
 * @author Dmitry Nevolin
 */
public class SftDataSynchronizer extends FileRequestSynchronizer {

    private static final String TAG = Logger.makeLogTag(SftDataSynchronizer.class);

    private static final String EXTRACTED_FILE_NAME = "SftData";
    private static final long GET_PACKET_STATUS_TIMEOUT = 2; // timeOut запроса ключей в минутах

    private static final String LAST_SFT_REQUEST_INFO_KEY = "LAST_SFT_REQUEST_INFO";
    private static final String DELETE_COMMAND_FILENAME_MASK = "sftfilestodelete";

    private final ApiManager apiManager;
    private final EdsManagerWrapper edsManager;
    private final BackupManagerStub backupManager = new BackupManagerStub();
    private final AppPropertiesRepository appPropertiesRepository;

    @NonNull
    private LastSftRequestInfo lastRequestInfo = LastSftRequestInfo.emptyRequest();

    @Inject
    SftDataSynchronizer(ApiManager apiManager, FilePathProvider filePathProvider, EdsManagerWrapper edsManager,
                        AppPropertiesRepository appPropertiesRepository,
                        SynchronizerInformer synchronizerInformer) {
        super(apiManager, new Config(filePathProvider.getSftDataSyncDir())
                        .setBackupFileDir(filePathProvider.getSftDataBackupDir())
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
        return Single
                .fromCallable(() -> {
                    // Проверяем есть ли активный запрос SFT
                    lastRequestInfo = LastSftRequestInfo.fromString(appPropertiesRepository.readKeyValue(LAST_SFT_REQUEST_INFO_KEY));
                    // Если есть активный запрос
                    if (lastRequestInfo.isActive()) {
                        // Если активный запрос не содержал физические файлы запросов SFT, то проверяем наличие физических запросов SFT
                        // Если есть новые запросы SFT, ТО ВСЕГДА генерируем новый запрос
                        if (!lastRequestInfo.hasRequestFiles && hasSftRequests()) {
                            return EMPTY_REQUEST_ID;
                        } else {
                            return lastRequestInfo.uuid;
                        }
                    } else {
                        return EMPTY_REQUEST_ID;
                    }
                });
    }

    @Override
    protected Single<BasePrepareSyncResponse> prepareNewRequest() {
        return rxGetFilesInTransportOutDir()
                .zipWith(getFilesInTransportInDir(), (filesInTransportOutDir, filesInTransportInDir) -> {
                    PreparePacketSftDataRequest request = new PreparePacketSftDataRequest();
                    if (filesInTransportOutDir.size() > 0) {
                        request.setFileRequestList(filesInTransportOutDir);
                    }
                    request.setKeyFileList(filesInTransportInDir);

                    // Запоминяем параметры соданного запроса
                    lastRequestInfo = LastSftRequestInfo.emptyRequest();
                    lastRequestInfo.hasRequestFiles = filesInTransportOutDir.size() > 0;

                    notify(String.format("Запрос [%s]: отправка запроса", getTitle()));
                    return request;
                })
                .flatMap(apiManager.api()::preparePacketSftData);
    }

    @Override
    protected Completable doApply(File dataFile) {
        return Single
                .fromCallable(() -> edsManager.closeBlocking())
                .flatMap(closed -> copyNewFiles(dataFile))
                .flatMap(filesCopied -> deleteOldFiles(dataFile))
                .flatMapCompletable(filesDeleted -> Completable.complete())
                .doFinally(() -> {
                    // Очищаем папку с запросом sft ключей
                    FileUtils2.clearDir(edsManager.getEdsDirs().getEdsTransportOutDir(), null);
                    // Инициализируем SFT библиотеку
                    initSft();
                });
    }

    @Nullable
    @Override
    public BackupManager getBackupManager() {
        return backupManager;
    }

    @Override
    @NonNull
    protected BasePrepareSyncResponse onRequestResponse(BasePrepareSyncResponse prepareResponse) throws SynchronizeException {
        BasePrepareSyncResponse result = super.onRequestResponse(prepareResponse);

        // Если запрос отправлен и прошел проверку, то сохраняем запрос
        lastRequestInfo.uuid = result.getRequestId();
        appPropertiesRepository.writeKeyValue(LAST_SFT_REQUEST_INFO_KEY, lastRequestInfo.toString());

        return result;
    }

    @Override
    @NonNull
    protected CheckUpdatesOperation.Result onRequestResult(CheckUpdatesOperation.Result requestResult) throws SynchronizeException {
        // Если запрос больше не в обработке, то удаляем сохраненный запрос
        // (!!! Это надо делать до стандарьтной обработки статуса ответа)
        if (requestResult.getPacketStatus() != PacketStatusEntity.PROCESS && requestResult.getPacketStatus() != PacketStatusEntity.PENDING_DATA) {
            // Удаляем запрос
            lastRequestInfo = LastSftRequestInfo.emptyRequest();
            appPropertiesRepository.deleteKeyValue(LAST_SFT_REQUEST_INFO_KEY);
        }

        return super.onRequestResult(requestResult);
    }

    /**
     * Выполняет копирование новых ключей на ПТК
     *
     * @param dirWithData Директория с данными от базовой станции
     */
    private Single<Boolean> copyNewFiles(@NonNull File dirWithData) {
        return Single.fromCallable(() -> {
            notify("Сохранение sft ключей: копирование новых ключей");
            // Предполагаем, что в архиве в корне лежат файлы ключей, которые нужно скормить в папку "in" sft
            File edsInDir = edsManager.getEdsDirs().getEdsTransportInDir();
            Logger.trace(TAG, "Копирование файлов из папки " + dirWithData.getAbsolutePath() + " в папку " + edsInDir.getAbsolutePath());
            boolean copyFilesResult = FileUtils2.copyDir(dirWithData, edsInDir, null);

            // удаляем из директории с ключами SFT временные файлы, содержащие список файлов для удаления (их не надо копировать в конечный каталог)
            File[] files = edsInDir.listFiles((dir, name) -> name.startsWith(DELETE_COMMAND_FILENAME_MASK));
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }

            Logger.trace(TAG, "Результат копирования файлов: " + copyFilesResult);
            return copyFilesResult;
        });
    }

    /**
     * Выполняет удаление лишних ключей на ПТК
     *
     * @param dirWithData Директория с данными от базовой станции
     */
    private Single<Boolean> deleteOldFiles(@NonNull File dirWithData) {
        // Читаем содержимое файла в байтах
        return Single
                // Если в архиве в корне лежит json-файл "sftfilestodelete_8898978979.bin",
                // содерщащий список файлов, которые нужно удалить из папки sft "in",
                // то удаляем, если его нет - пропускаем шаг
                .fromCallable(() -> {
                    File file = findFirstFile(dirWithData, (dir, name) -> name.startsWith(DELETE_COMMAND_FILENAME_MASK));
                    if (file != null && file.exists()) {
                        // считываем содержимое файла в JsonArray
                        byte[] data = FileUtils2.readFileContent(file);
                        return new JSONArray(new String(data, "UTF-8"));
                    } else {
                        return new JSONArray();
                    }
                })
                // Перегоняем json-массив в список файлов
                .map(jsonArray -> {
                    File edsInDir = edsManager.getEdsDirs().getEdsTransportInDir();
                    List<File> filesToDelete = new ArrayList<>(jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        File fileToDelete = new File(edsInDir, jsonArray.getString(i));
                        filesToDelete.add(fileToDelete);
                    }
                    if (filesToDelete.size() > 0) {
                        notify("Сохранение sft ключей: удаление старых ключей");
                    }
                    return filesToDelete;
                })
                // Формируем поток файлов на удаление
                .flatMapObservable(Observable::fromIterable)
                // Удаляем файл
                .flatMapSingle(file -> {
                    return Single.fromCallable(() -> {
                        Logger.trace(TAG, "Удаление файла " + file.getAbsolutePath());
                        boolean deleteResult = FileUtils2.deleteFile(file, null);
                        Logger.trace(TAG, "Результат удаления файла: " + deleteResult);
                        return deleteResult;
                    });
                })
                // Склеиваем успешность удаления файла в общий результат выполнения операции
                .reduce(true, (previousFilesDeleted, fileDeleted) -> {
                    return previousFilesDeleted & fileDeleted;
                });
    }

    /**
     * Собирает список файлов в рабочей папке "out" sft для передачи на базовую станцию.
     */
    private List<FileEntity> getFilesInTransportOutDir() throws IOException {
        File edsTransportOutDir = edsManager.getEdsDirs().getEdsTransportOutDir();
        File[] filesInEdsTransportOutDir = edsTransportOutDir.listFiles(new TransportOutDirExportFileFilter());
        if (filesInEdsTransportOutDir == null || filesInEdsTransportOutDir.length == 0) {
            return Collections.emptyList();
        }
        List<FileEntity> filesInTransportOutDir = new ArrayList<>();
        for (File file : filesInEdsTransportOutDir) {
            filesInTransportOutDir.add(FileMapper.javaToModel(file));
        }
        return filesInTransportOutDir;
    }

    private Single<List<FileEntity>> rxGetFilesInTransportOutDir() {
        return Single
                .fromCallable(() -> {
                    notify(String.format("Запрос [%s]: формирование запроса", getTitle()));
                    File edsTransportOutDir = edsManager.getEdsDirs().getEdsTransportOutDir();
                    File[] filesInEdsTransportOutDir = edsTransportOutDir.listFiles(new TransportOutDirExportFileFilter());
                    return filesInEdsTransportOutDir == null ? new File[0] : filesInEdsTransportOutDir;
                })
                .flatMapObservable(Observable::fromArray)
                .map(FileMapper::javaToModel)
                .toList();
    }

    /**
     * Собирает список файлов в рабочей папке "in" sft для передачи на базовую станцию.
     */
    private Single<List<String>> getFilesInTransportInDir() {
        return Single
                .fromCallable(() -> {
                    File edsTransportInDir = edsManager.getEdsDirs().getEdsTransportInDir();
                    String[] filesInEdsTransportInDir = edsTransportInDir.list();
                    return filesInEdsTransportInDir == null ? new String[0] : filesInEdsTransportInDir;
                })
                .map(Arrays::asList);
    }

    // инициализация SFT
    private boolean initSft() {
        notify("Инициализация sft");

        Logger.trace(TAG, "Проверяем состояние sft");
        boolean pullResult = edsManager.pullEdsCheckerBlocking();
        Logger.trace(TAG, "Результат проверки состояния sft: " + pullResult);

        if (pullResult) {
            notify("Инициализация sft: успешно");
        } else {
            notify("Инициализация sft: ошибка");
        }

        return pullResult;
    }

    // Запрашивает состояние sft и проверяет наличие запросов в папке sft
    private boolean hasSftRequests() throws IOException {
        notify("Проверка запросов sft");

        // проверяем состояние (при этом могут быть созданы файлы для запроса недостающих ключей)
        edsManager.getStateBlocking();

        boolean hasRequest = getFilesInTransportOutDir().size() > 0;
        if (hasRequest) {
            notify("Проверка запросов sft: найдены новые запросы");
        } else {
            notify("Проверка запросов sft: успешно");
        }
        // проверяем, что в папке запросов SFT появились новые файлы
        return hasRequest;
    }

    private File findFirstFile(File parent, FilenameFilter filter) {
        File[] files = parent.listFiles(filter);
        return files == null || files.length == 0 ? null : files[0];
    }

    @Override
    public SynchronizeType getType() {
        return SynchronizeType.SYNC_SFT_DATA;
    }

    @Override
    public String getTitle() {
        return "Ключи sft";
    }

    static class LastSftRequestInfo {

        public @NonNull
        UUID uuid;
        public boolean hasRequestFiles;

        public boolean isActive() {
            return !uuid.equals(EMPTY_REQUEST_ID);
        }

        private LastSftRequestInfo(@NonNull UUID uuid, boolean hasRequestFiles) {
            this.uuid = uuid;
            this.hasRequestFiles = hasRequestFiles;
        }

        public String toString() {
            return (hasRequestFiles ? "T" : "F") + uuid.toString();
        }

        public static LastSftRequestInfo emptyRequest() {
            return new LastSftRequestInfo(EMPTY_REQUEST_ID, false);
        }

        public static LastSftRequestInfo fromString(String value) {
            if (value != null && !value.isEmpty()) {
                try {
                    boolean hasRequestFiles = value.substring(0, 1).equals("T");
                    UUID uuid = UUID.fromString(value.substring(1));
                    return new LastSftRequestInfo(uuid, hasRequestFiles);
                } catch (Exception e) {
                    Logger.error(TAG, String.format("LastSftRequestInfo.fromString('%s') - error parsing uuid value", value.substring(1)), e);
                }
            }

            return emptyRequest();
        }

    }

}
