package ru.ppr.chit.bs.export;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.api.entity.ErrorEntity;
import ru.ppr.chit.api.request.BoardingCompleteRequest;
import ru.ppr.chit.api.response.BoardingCompleteResponse;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.RegistrationInformant;
import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.domain.boardingexport.BoardingExportEventBuilder;
import ru.ppr.chit.domain.boardingexport.BoardingExportEventStoreInteractor;
import ru.ppr.chit.domain.exchangeevent.ExchangeEventManager;
import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.domain.model.local.BoardingExportEvent;
import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.repository.local.BoardingEventRepository;
import ru.ppr.chit.domain.repository.local.TrainInfoRepository;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.manager.WiFiManager;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
public class BoardingEventExporter extends BaseExporter {

    private static final String TAG = Logger.makeLogTag(BoardingEventExporter.class);
    /**
     * Интервал выгрузки (в минутах)
     */
    private static final long EXPORT_INTERVAL = 2;

    private final ExchangeEventManager exchangeEventManager;
    private final BoardingExportEventBuilder boardingExportEventBuilder;
    private final BoardingExportEventStoreInteractor boardingExportEventStoreInteractor;
    private final BoardingEventRepository boardingEventRepository;
    private final TripServiceEventRepository tripServiceEventRepository;
    private final TrainInfoRepository trainInfoRepository;

    private Disposable exportDisposable = Disposables.disposed();

    @Inject
    BoardingEventExporter(RegistrationInformant registrationInformant,
                          WiFiManager wiFiManager,
                          ApiManager apiManager,
                          ExchangeEventManager exchangeEventManager,
                          BoardingExportEventBuilder boardingExportEventBuilder,
                          BoardingExportEventStoreInteractor boardingExportEventStoreInteractor,
                          BoardingEventRepository boardingEventRepository,
                          TripServiceEventRepository tripServiceEventRepository,
                          TrainInfoRepository trainInfoRepository,
                          LocalDbManager localDbManager,
                          NsiDbManager nsiDbManager) {
        super(registrationInformant, wiFiManager, apiManager, localDbManager, nsiDbManager);
        this.exchangeEventManager = exchangeEventManager;
        this.boardingExportEventBuilder = boardingExportEventBuilder;
        this.boardingExportEventStoreInteractor = boardingExportEventStoreInteractor;
        this.boardingEventRepository = boardingEventRepository;
        this.tripServiceEventRepository = tripServiceEventRepository;
        this.trainInfoRepository = trainInfoRepository;
    }

    public void start() {
        Logger.info(TAG, "start working");
        exportDisposable = canExchangeBehaviour()
                .flatMapCompletable(canExchange -> export())
                // Делаем выгрузку каждые EXPORT_INTERVAL минут если возможно
                .repeatWhen(flowable -> flowable.delay(EXPORT_INTERVAL, TimeUnit.MINUTES))
                .subscribeOn(AppSchedulers.background())
                .subscribe();
    }
    private Completable export() {
        Logger.info(TAG, "export started");

        return buildList()
                // проверяем, что есть что выгружать
                .filter(this::checkBuildResultList)
                // создаем событие выгрузки
                .flatMap(buildResultList ->
                        Maybe.fromCallable(() -> {
                            ExchangeEvent event = exchangeEventManager.startExchangeEvent(ExchangeEvent.Type.EXPORT_BOARDING_EVENT);
                            return new ExportData(event, buildResultList);
                        })
                )
                // обрабатываем событие выгрузки
                .flatMapCompletable(exportData ->
                        Observable
                                .fromIterable(exportData.list)
                                // Выгружаем на сервер
                                .flatMapCompletable(buildResult ->
                                        getApiManager().api()
                                                .boardingComplete(buildResult.request)
                                                // Сохраняем результат
                                                .flatMapCompletable(response ->
                                                        storeExportedEvents(response, buildResult))
                                )
                                .andThen(Completable.fromAction(() -> {
                                    // Сохраняем событие обмена данными с успехом
                                    exchangeEventManager.completeExchangeEvent(exportData.event, ExchangeEvent.Status.SUCCESS);
                                    Logger.info(TAG, "export success");
                                }))
                                .onErrorResumeNext(error -> {
                                    Logger.error(TAG, "export failed");
                                    // Сохраняем событие обмена данными с ошибкой
                                    exchangeEventManager.completeExchangeEvent(exportData.event, ExchangeEvent.Status.ERROR);
                                    return Completable.error(error);
                                })
                )
                .onErrorResumeNext(error -> {
                    Logger.error(TAG, "error occurred during export:");
                    Logger.error(TAG, error);
                    return Completable.complete();
                })
                .doOnComplete(() -> Logger.info(TAG, "export finished"));
    }


    /**
     * Сохраняет в БД выгруженные на сервер события
     */
    private Completable storeExportedEvents(BoardingCompleteResponse response, CompositeBuildResult buildResult) {
        // Ошибка TRAIN_THREAD_CODE_NOT_SET является допустимой в данной ситуации, остальные ошибки трактуем как ошибки
        if (response.getError() != null && !response.getError().getCode().equals(ErrorEntity.Code.TRAIN_THREAD_CODE_NOT_SET)) {
            return Completable.error(new Exception("boardingComplete response.getError() = " + response.getError()));
        } else {
            // если есть варнинги, фиксируем их в логах
            if (response.getError() != null) {
                Logger.warning(TAG, "boardingComplete warning = " + response.getError());
            }
            Logger.info(TAG, "boardingComplete completed");
            return Completable.fromAction(() -> {
                Logger.info(TAG, "start storing exported event");
                BoardingExportEvent boardingExportEvent = boardingExportEventBuilder
                        .setBoardingEvent(buildResult.boardingEvent)
                        .build();
                Logger.info(TAG, "store boardingExportEvent");
                boardingExportEventStoreInteractor.store(boardingExportEvent);
                Logger.info(TAG, "end storing exported event");
            });
        }
    }

    private boolean checkBuildResultList(List<CompositeBuildResult> buildResultList) {
        boolean hasEventsToExport = !buildResultList.isEmpty();
        if (hasEventsToExport) {
            Logger.info(TAG, "exporting boarding events: " + buildResultList.size());
        } else {
            Logger.info(TAG, "no boarding events to export");
        }
        return hasEventsToExport;
    }

    /**
     * Билдит список данных для выгрузки
     */
    @NonNull
    private Single<List<CompositeBuildResult>> buildList() {
        return Single.fromCallable(() -> {
            Logger.info(TAG, "buildList started");
            // События посадки для выгрузки
            List<BoardingEvent> boardingEventList = boardingEventRepository.loadAllNotExported();
            Logger.info(TAG, "boardingEventList.size(): " + boardingEventList.size());
            // Запросы к API для выгрузки
            List<CompositeBuildResult> resultList = new ArrayList<>();
            for (BoardingEvent boardingEvent : boardingEventList) {
                // Нужны только завершенные посадки
                if (boardingEvent.getStatus() == BoardingEvent.Status.ENDED) {
                    resultList.add(build(boardingEvent));
                }
            }
            Logger.info(TAG, "resultList.size(): " + resultList.size());
            return resultList;
        });
    }

    /**
     * Билдит даные для выгрузки
     */
    @NonNull
    private CompositeBuildResult build(@NonNull BoardingEvent boardingEvent) {
        Logger.info(TAG, "build started");
        // Если мы вообще создали событие посадки, значит информация о нити поезда была в
        // рамках события обслуживания, смысла проверять на null нет
        TripServiceEvent tripServiceEvent = boardingEvent.getTripServiceEvent(tripServiceEventRepository);
        Logger.info(TAG, "tripServiceEventUuid: " + tripServiceEvent.getTripUuid());
        TrainInfo trainInfo = tripServiceEvent.getTrainInfo(trainInfoRepository);
        Logger.info(TAG, "trainInfoId: " + trainInfo.getId());
        BoardingCompleteRequest request = new BoardingCompleteRequest();
        request.setControlStationCode(boardingEvent.getStationCode());
        request.setTrainThreadId(trainInfo.getTrainThreadId());
        CompositeBuildResult compositeBuildResult = new CompositeBuildResult();
        compositeBuildResult.request = request;
        compositeBuildResult.boardingEvent = boardingEvent;
        return compositeBuildResult;
    }

    private static class ExportData {

        public final List<CompositeBuildResult> list;
        public final ExchangeEvent event;

        protected ExportData(ExchangeEvent event, List<CompositeBuildResult> list) {
            this.list = list;
            this.event = event;
        }
    }

    /**
     * Результат сборки данных для 1 посадки
     */
    private static class CompositeBuildResult {

        /**
         * Запрос к серверу
         */
        private BoardingCompleteRequest request;
        /**
         * Событие посадки в рамках которого мы отправляем данные
         */
        private BoardingEvent boardingEvent;

    }

}
