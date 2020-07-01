package ru.ppr.chit.bs.synchronizer.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import ru.ppr.chit.api.entity.ErrorEntity;
import ru.ppr.chit.api.entity.PacketStatusEntity;
import ru.ppr.chit.api.response.BasePrepareSyncResponse;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.synchronizer.SynchronizerInformer;
import ru.ppr.chit.bs.synchronizer.event.SyncInfoEvent;
import ru.ppr.chit.bs.synchronizer.operation.CheckUpdatesOperation;
import ru.ppr.chit.bs.synchronizer.operation.ClearDirOperation;
import ru.ppr.chit.bs.synchronizer.operation.ExtractPacketOperation;
import ru.ppr.chit.bs.synchronizer.operation.GetPacketChunkOperation;
import ru.ppr.core.exceptions.UserException;

/**
 * Базовый класс синхронизации на основе файловых пакетов
 *
 * @author m.sidorov
 */
public abstract class FileRequestSynchronizer implements Synchronizer<File>, Notifier<String> {

    protected static final UUID EMPTY_REQUEST_ID = new UUID(0, 0);

    protected final ApiManager apiManager;
    protected final Config config;
    protected final SynchronizerInformer synchronizerInformer;

    private File loadedFile;

    protected FileRequestSynchronizer(ApiManager apiManager, Config config, SynchronizerInformer synchronizerInformer) {
        this.apiManager = apiManager;
        this.config = config;
        this.synchronizerInformer = synchronizerInformer;
    }

    // уведомление о событии синхронизации
    @Override
    public void notify(String message) {
        synchronizerInformer.notify(new SyncInfoEvent(getType(), message));
    }


    @Override
    public Completable load() {
        // Флаг, что был создан новый запрос
        AtomicBoolean newRequestCreated = new AtomicBoolean(false);
        return Completable
                .fromAction(() -> setLoadedFile(null)) // Обнуляем загруженный файл
                .andThen(getRequestId())
                .flatMap(requestId -> {
                    // Если запроса еще нет, то создаем его, иначе используем существующий
                    if (requestId.equals(EMPTY_REQUEST_ID)) {
                        notify(String.format("Запрос [%s]: создание запроса", getTitle()));
                        newRequestCreated.set(true);
                        return prepareNewRequest()
                                .map(this::onRequestResponse)
                                .map(response -> response.getRequestId());
                    } else {
                        return Single.just(requestId);
                    }
                })
                .flatMap(this::processRequest)
                .flatMapCompletable(packetData ->
                        Completable.fromAction(() -> {
                            // Запоминаем загруженный файл
                            setLoadedFile(packetData.getExtractedFile());
                        }))
                .onErrorResumeNext(error -> {

                    if (error instanceof SynchronizeNoDataException || error instanceof SynchronizedPendingDataException) {
                        notify(error.getMessage());
                        // Если вернулась ошибка "Нет данных" то просто завершаем операцию
                        return Completable.complete();
                    }

                    // Если при обработке ранее существовавшего запроса сервер вернул ошибку "Неизвестный идентификатор запроса",
                    // то повторно запускаем процесс формирования и загрузки запроса
                    // Такая ошибка бывает, если на базовой станции очищают кеш
                    if (!newRequestCreated.get() && error instanceof SynchronizeUnknownRequestIdException){
                        return load();
                    }

                    return Completable.error(UserException.wrap(error, String.format("Запрос [%s]: ошибка обработки запроса", getTitle())));
                });
    }

    @Override
    public Completable apply() {
        return Maybe
                .fromCallable(() -> hasLoadedData())
                // если данные были загружены
                .filter(Boolean.TRUE::equals)
                .flatMapCompletable(aBoolean -> Completable
                        .fromAction(() -> {
                            notify(String.format("%s: копирование данных", getTitle()));
                        })
                        .andThen(doApply(loadedFile))
                        .doOnComplete(() -> String.format("%s: данные сохранены", getTitle()))
                        .onErrorResumeNext(throwable -> Completable.error(UserException.wrap(throwable, String.format("%s: ошибка сохранения данных", getTitle()))))
                );
    }

    @Override
    @Nullable
    public File getLoadedData() {
        return loadedFile;
    }

    private void setLoadedFile(File file) {
        loadedFile = file;
    }

    @Override
    public boolean hasLoadedData(){
        return getLoadedData() != null;
    }

    // Наследники должны вернуть собственную имплементацию backupManager
    @Override
    public abstract BackupManager getBackupManager();

    /**
     * Наследники должны реализовать логику подготовки и отправки запроса на сервер
     *
     * @return requestId полученный от сервера
     */
    protected abstract Single<BasePrepareSyncResponse> prepareNewRequest();

    /**
     * Наследники должны реализовать логику копирования и применения полученного с сервера файла
     */
    protected abstract Completable doApply(File dataFile);

    /**
     * Возвращает ID существующего запроса, если его не надо создавать повторно
     * По умолчанию всегда EMPTY_REQUEST_ID, но наследники могут переопределять эту логику
     *
     * @return requestId актуального запроса
     */
    protected Single<UUID> getRequestId() {
        return Single.just(EMPTY_REQUEST_ID);
    }

    /**
     * Ждет завершения обработки запроса
     * - ждет завершения обработки запроса (getPacketStatus() != PacketStatusEntity.PROCESS)
     * - проверяет результат на наличие данных и ошибки
     * - если запрос вернул новые дданные, то закачивает их
     * !!! если новых данных нет, то возвращает специальную ошибку SynchronizeNoDataException
     */
    protected Single<ExtractPacketOperation.Result> processRequest(UUID requestId) {
        return Completable
                .fromAction(() -> ClearDirOperation.clearDir(config.packetFileDir))
                .andThen(checkUpdates(requestId))
                .flatMap(checkUpdatesResult -> {
                    // Если запрос ожидает данных с сервера, то возвращаем пустой результат
                    if (checkUpdatesResult.getPacketStatus() == PacketStatusEntity.PENDING_DATA) {
                        return Single.error(new SynchronizedPendingDataException(String.format("Запрос [%s]: ожидание данных от ЦОД", getTitle())));
                    } else
                        // Если запрос обработан, то берем из него данные
                        if (checkUpdatesResult.getPacketStatus() == PacketStatusEntity.READY) {
                            // Если данные есть, то копируем их, иначе возвращаем пустой результат
                            if (checkUpdatesResult.hasUpdates()) {
                                notify(String.format("Запрос [%s]: загрузка данных с сервера", getTitle()));
                                // Если пакет успешно сформирован - качаем
                                return startGetPacketChunkOperation(checkUpdatesResult)
                                        .flatMap(result -> startExtractPacketOperation(result))
                                        .onErrorResumeNext(error -> Single.error(UserException.wrap(error, String.format("Запрос [%s]: ошибка загрузки файлов пакета", getTitle()))));
                            } else {
                                return Single.error(new SynchronizeNoDataException(String.format("Запрос [%s]: нет новых данных", getTitle())));
                            }
                        } else {
                            return Single.error(new SynchronizeException(String.format("Запрос [%s]: ошибка обработки запроса, статус [%s]", getTitle(), checkUpdatesResult.getPacketStatus())));
                        }
                });
    }

    /**
     * Периодически опрашивает сервер, запрашивая статус выполнения запроса
     * проверяет до тех пор, пока не получит внятный результат или не наступит timeout:
     */
    private Single<CheckUpdatesOperation.Result> checkUpdates(UUID requestId) {
        return startCheckUpdatesOperation(requestId)
                .repeatWhen(flowable -> flowable.delay(config.statusRequestInterval, TimeUnit.SECONDS))
                .filter(passResult -> passResult.getPacketStatus() != PacketStatusEntity.PROCESS)
                .firstOrError()
                .timeout(config.getPacketStatusTimeout, TimeUnit.MINUTES)
                .onErrorResumeNext(error -> {
                    if (error instanceof TimeoutException) {
                        return Single.error(new SynchronizeException(String.format("Запрос [%s]: истекло время ожидания обработки запроса", getTitle())));
                    }
                    return Single.error(SynchronizeException.wrap(error, String.format("Запрос [%s]: ошибка обработки запроса", getTitle())));
                });
    }

    /**
     * Выполняет проверку состояния обработки запроса по requestId
     */
    private Single<CheckUpdatesOperation.Result> startCheckUpdatesOperation(UUID requestId) {
        return Single
                .fromCallable(() -> {
                    notify(String.format("Запрос [%s]: проверка состояния", getTitle()));
                    return new CheckUpdatesOperation.Params(requestId);
                })
                .flatMap(params -> new CheckUpdatesOperation(apiManager.api(), params).rxStart())
                .doOnSuccess(this::onRequestResult);
    }

    // Общий метод проверки ответа на создание запроса
    // Проверяет результат отправки запроса (вынесен в отдельный метод, чтобы в наследниках можно было цеплять свою реализацию)
    @NonNull
    protected BasePrepareSyncResponse onRequestResponse(BasePrepareSyncResponse prepareResponse) throws SynchronizeException {
        if (prepareResponse.getError() != null) {
            throw new SynchronizeException(String.format("Запрос [%s], ошибка создания запроса: %s", getTitle(), prepareResponse.getError().getDescription()));
        }

        return prepareResponse;
    }

    // Общий метод проверки результата обработки запроса
    // Проверяет результат обработки запроса (вынесен в отдельный метод, чтобы в наследниках можно было цеплять свою реализацию)
    @NonNull
    protected CheckUpdatesOperation.Result onRequestResult(CheckUpdatesOperation.Result requestResult) throws SynchronizeException {
        switch (requestResult.getPacketStatus()) {
            case ERROR:
                String srvError = "";
                if (requestResult.getError() != null) {
                    srvError = requestResult.getError().getDescription();
                    // Оборачиваем ошибку неизвестного ID запроса в специальный тип (он обрабатывается на верхнем уровне)
                    if (requestResult.getError().getCode().equals(ErrorEntity.Code.UNKNOWN_PACKET_ID)){
                        throw new SynchronizeUnknownRequestIdException(String.format("Запрос [%s]: ошибка обработки запроса сервером %s", getTitle(), srvError));
                    }
                }
                throw new SynchronizeException(String.format("Запрос [%s]: ошибка обработки запроса сервером %s", getTitle(), srvError));
            case PROCESS:
                notify(String.format("Запрос [%s]: в обработке", getTitle()));
                break;
            case READY:
                if (!requestResult.hasUpdates()) {
                    notify(String.format("Запрос [%s]: нет новых данных", getTitle()));
                } else {
                    notify(String.format("Запрос [%s]: данные получены", getTitle()));
                }
                break;
            case PENDING_DATA:
                notify(String.format("Запрос [%s]: ожидание данных от ЦОД", getTitle()));
                break;
            default:
                throw new SynchronizeException(String.format("Запрос [%s]: неизвестный формат ответа", getTitle()));
        }
        return requestResult;
    }

    /**
     * Выполняет операцию закачки пакета
     */
    private Single<GetPacketChunkOperation.Result> startGetPacketChunkOperation(CheckUpdatesOperation.Result result) {
        return Single
                .fromCallable(() -> new GetPacketChunkOperation.Params(
                        result.getRequestId(),
                        config.packetOffset,
                        result.getPacketLength(),
                        config.packetFileDir,
                        // Если имя файла не задано, используем requestId
                        config.packetFileName == null ? result.getRequestId().toString() : config.packetFileName,
                        Config.packetFileExt,
                        result.getPacketHash()))
                .flatMap(params -> new GetPacketChunkOperation(apiManager.api(), params).rxStart());
    }

    private Single<ExtractPacketOperation.Result> startExtractPacketOperation(GetPacketChunkOperation.Result result) {
        return Single
                .fromCallable(() ->
                        // Извлекаем в папке архива
                        new ExtractPacketOperation.Params(
                                getTitle(),
                                result.getPacketFile(),
                                config.packetFileDir,
                                config.extractedFileName != null ? config.extractedFileName : result.getPacketFile().getName())
                                .setExtractToDir(config.extractToDir))
                .flatMap(params -> new ExtractPacketOperation(params, this).rxStart());
    }

    /**
     * Конфигурация, содержит как обязательные, так и опциональные параметры,
     * у опциональных есть значение по-умолчанию.
     */
    protected static class Config {

        /**
         * Расширение закачиваемого файла пакета
         */
        private static final String packetFileExt = "gz";
        /**
         * Папка для закачки и распаковки файла пакета
         */
        private final File packetFileDir;
        /**
         * Папка для бекапов
         */
        private File backupFileDir = null;
        /**
         * Интервал опроса сервера (в секундах) о завершенном
         * статусе пакета т.е. READY или ERROR
         */
        private long statusRequestInterval = 10;
        /**
         * Сдвиг в байтах относительно начала пакета
         */
        private long packetOffset = 0;
        /**
         * Имя закачиваемого файла пакета, если null - значит будет использован requestId
         */
        private String packetFileName = null;
        /**
         * Имя разархивированного файла пакета, если null - значит будет использовано имя архива
         */
        private String extractedFileName = null;
        /**
         * Если true, то разархивация будет происходить в папку с именем extractedFileName, а не в файл. По-умолчанию false.
         */
        private boolean extractToDir;
        /**
         * БС при запросе статуса о подготовке пакета может отвечать PROCESS очень долго, поэтому используется таймаут.
         * Значение в минутах, по-умолчанию - 8 минут.
         */
        private long getPacketStatusTimeout = 8;

        public Config(File packetFileDir) {
            this.packetFileDir = packetFileDir;

        }

        public File getPacketFileDir() {
            return packetFileDir;
        }

        public Config setBackupFileDir(File backupFileDir) {
            this.backupFileDir = backupFileDir;

            return this;
        }

        public File getBackupFileDir() {
            return backupFileDir;
        }


        public Config setStatusRequestInterval(long statusRequestInterval) {
            this.statusRequestInterval = statusRequestInterval;

            return this;
        }

        public Config setPacketOffset(long packetOffset) {
            this.packetOffset = packetOffset;

            return this;
        }

        public Config setPacketFileName(String packetFileName) {
            this.packetFileName = packetFileName;

            return this;
        }

        public String getPacketFileName() {
            return packetFileName;
        }

        public Config setExtractedFileName(String extractedFileName) {
            this.extractedFileName = extractedFileName;

            return this;
        }

        public String getExtractedFileName() {
            return extractedFileName;
        }

        public Config setExtractToDir(boolean extractToDir) {
            this.extractToDir = extractToDir;

            return this;
        }

        public Config setGetPacketStatusTimeout(long getPacketStatusTimeout) {
            this.getPacketStatusTimeout = getPacketStatusTimeout;

            return this;
        }

    }

}
