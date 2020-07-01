package ru.ppr.chit.bs.export;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.api.entity.ErrorEntity;
import ru.ppr.chit.api.entity.TicketBoardingEntity;
import ru.ppr.chit.api.entity.TicketDataEntity;
import ru.ppr.chit.api.mapper.LocationMapper;
import ru.ppr.chit.api.mapper.PassengerMapper;
import ru.ppr.chit.api.mapper.SmartCardMapper;
import ru.ppr.chit.api.mapper.TicketBoardingMapper;
import ru.ppr.chit.api.mapper.TicketDataMapper;
import ru.ppr.chit.api.mapper.TicketIdMapper;
import ru.ppr.chit.api.request.PushBoardingListRequest;
import ru.ppr.chit.api.response.PushBoardingListResponse;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.RegistrationInformant;
import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.domain.exchangeevent.ExchangeEventManager;
import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.domain.model.local.TicketBoarding;
import ru.ppr.chit.domain.model.local.TicketControlEvent;
import ru.ppr.chit.domain.model.local.TicketControlExportEvent;
import ru.ppr.chit.domain.model.local.TicketData;
import ru.ppr.chit.domain.repository.local.LocationRepository;
import ru.ppr.chit.domain.repository.local.PassengerRepository;
import ru.ppr.chit.domain.repository.local.SmartCardRepository;
import ru.ppr.chit.domain.repository.local.TicketBoardingRepository;
import ru.ppr.chit.domain.repository.local.TicketControlEventRepository;
import ru.ppr.chit.domain.repository.local.TicketControlExportEventRepository;
import ru.ppr.chit.domain.repository.local.TicketDataRepository;
import ru.ppr.chit.domain.repository.local.TicketIdRepository;
import ru.ppr.chit.domain.ticketcontrolexport.TicketControlExportEventBuilder;
import ru.ppr.chit.domain.ticketcontrolexport.TicketControlExportEventStoreInteractor;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.manager.WiFiManager;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
public class TicketBoardingExporter extends BaseExporter {

    private static final String TAG = Logger.makeLogTag(TicketBoardingExporter.class);
    /**
     * Интервал выгрузки (в минутах)
     */
    private static final long EXPORT_INTERVAL = 2;

    private final ExchangeEventManager exchangeEventManager;
    private final TicketControlExportEventRepository ticketControlExportEventRepository;
    private final TicketControlEventRepository ticketControlEventRepository;
    private final TicketBoardingRepository ticketBoardingRepository;
    private final TicketIdRepository ticketIdRepository;
    private final TicketDataRepository ticketDataRepository;
    private final PassengerRepository passengerRepository;
    private final LocationRepository locationRepository;
    private final SmartCardRepository smartCardRepository;
    private final TicketControlExportEventBuilder ticketControlExportEventBuilder;
    private final TicketControlExportEventStoreInteractor ticketControlExportEventStoreInteractor;

    private Disposable exportDisposable = Disposables.disposed();

    @Inject
    TicketBoardingExporter(RegistrationInformant registrationInformant,
                           WiFiManager wiFiManager,
                           ApiManager apiManager,
                           ExchangeEventManager exchangeEventManager,
                           TicketControlExportEventRepository ticketControlExportEventRepository,
                           TicketControlEventRepository ticketControlEventRepository,
                           TicketBoardingRepository ticketBoardingRepository,
                           TicketIdRepository ticketIdRepository,
                           TicketDataRepository ticketDataRepository,
                           PassengerRepository passengerRepository,
                           LocationRepository locationRepository,
                           SmartCardRepository smartCardRepository,
                           TicketControlExportEventBuilder ticketControlExportEventBuilder,
                           TicketControlExportEventStoreInteractor ticketControlExportEventStoreInteractor,
                           LocalDbManager localDbManager,
                           NsiDbManager nsiDbManager) {
        super(registrationInformant, wiFiManager, apiManager, localDbManager, nsiDbManager);
        this.exchangeEventManager = exchangeEventManager;
        this.ticketControlExportEventRepository = ticketControlExportEventRepository;
        this.ticketControlEventRepository = ticketControlEventRepository;
        this.ticketBoardingRepository = ticketBoardingRepository;
        this.ticketIdRepository = ticketIdRepository;
        this.ticketDataRepository = ticketDataRepository;
        this.passengerRepository = passengerRepository;
        this.locationRepository = locationRepository;
        this.smartCardRepository = smartCardRepository;
        this.ticketControlExportEventBuilder = ticketControlExportEventBuilder;
        this.ticketControlExportEventStoreInteractor = ticketControlExportEventStoreInteractor;
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

        return build()
                // Проверяем, что нам есть что выгружать
                .filter(this::checkBuildResult)
                // создаем событие выгрузки
                .flatMap(buildResultList -> Maybe.fromCallable(() -> {
                                    ExchangeEvent event = exchangeEventManager.startExchangeEvent(ExchangeEvent.Type.EXPORT_TICKET_BOARDING);
                                    return new ExportData(event, buildResultList);
                                })
                )
                .flatMapCompletable(exportData ->
                        getApiManager().api()
                                // Выгружаем на сервер
                                .pushBoardingList(exportData.result.request)
                                // Сохраняем результат
                                .flatMapCompletable(response -> storeExportedEvents(response, exportData.result))
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
    private Completable storeExportedEvents(PushBoardingListResponse response, CompositeBuildResult buildResult) {
        // Ошибка TRAIN_THREAD_CODE_NOT_SET является допустимой в данной ситуации, остальные ошибки трактуем как ошибки
        if (response.getError() != null && !response.getError().getCode().equals(ErrorEntity.Code.TRAIN_THREAD_CODE_NOT_SET)) {
            return Completable.error(new Exception("pushBoardingList response.getError() = " + response.getError()));
        } else {
            // если есть варнинги, фиксируем их в логах
            if (response.getError() != null) {
                Logger.warning(TAG, "pushBoardingList warning = " + response.getError());
            }
            Logger.info(TAG, "pushBoardingList completed");
            return Completable.fromAction(() -> {
                Logger.info(TAG, "start storing exported events");
                List<TicketControlExportEvent> ticketControlExportEventList = new ArrayList<>();
                for (TicketControlEvent ticketControlEvent : buildResult.ticketControlEventList) {
                    ticketControlExportEventList.add(ticketControlExportEventBuilder
                            .setTicketControlEvent(ticketControlEvent)
                            .build());
                }
                Logger.info(TAG, "store ticketControlExportEvents");
                ticketControlExportEventStoreInteractor.storeAll(ticketControlExportEventList);
                Logger.info(TAG, "end storing exported events");
            });
        }
    }

    private boolean checkBuildResult(CompositeBuildResult buildResult) {
        boolean hasEventsToExport = !buildResult.ticketControlEventList.isEmpty();
        if (hasEventsToExport) {
            Logger.info(TAG, "exporting control events: " + buildResult.ticketControlEventList.size());
        } else {
            Logger.info(TAG, "no control events to export");
        }
        return hasEventsToExport;
    }

    /**
     * Билдит даные для выгрузки
     */
    @NonNull
    private Single<CompositeBuildResult> build() {
        return Single.fromCallable(() -> {
            Logger.info(TAG, "build started");
            // События контроля для выгрузки
            List<TicketControlEvent> ticketControlEventList = ticketControlEventRepository.loadAllNotExported();
            Logger.info(TAG, "ticketControlEventList.size(): " + ticketControlEventList.size());
            // Смаппленыне события контроля для API
            List<TicketBoardingEntity> ticketBoardingEntityList = new ArrayList<>();
            for (TicketControlEvent ticketControlEvent : ticketControlEventList) {
                ticketBoardingEntityList.add(map(ticketControlEvent.getTicketBoarding(ticketBoardingRepository)));
            }
            PushBoardingListRequest request = new PushBoardingListRequest();
            request.setTicketBoardingList(ticketBoardingEntityList);
            CompositeBuildResult compositeBuildResult = new CompositeBuildResult();
            compositeBuildResult.request = request;
            compositeBuildResult.ticketControlEventList = ticketControlEventList;
            return compositeBuildResult;
        });
    }

    private TicketBoardingEntity map(@NonNull TicketBoarding ticketBoarding) {
        // Маппим вручную вложенные сущности, т.к. геттеры требуют на вход репозиторий,
        // а мы пока не придумали как научить mapstruct работать с ними
        // В будущем придумать более правильное решение
        TicketData ticketData = ticketBoarding.getTicketData(ticketDataRepository);
        TicketDataEntity ticketDataEntity = TicketDataMapper.INSTANCE.modelToEntity(ticketData);
        if (ticketData != null && ticketDataEntity != null) {
            ticketDataEntity.setPassenger(PassengerMapper.INSTANCE.modelToEntity(ticketData.getPassenger(passengerRepository)));
            ticketDataEntity.setLocation(LocationMapper.INSTANCE.modelToEntity(ticketData.getLocation(locationRepository)));
            ticketDataEntity.setSmartCard(SmartCardMapper.INSTANCE.modelToEntity(ticketData.getSmartCard(smartCardRepository)));
        }
        TicketBoardingEntity ticketBoardingEntity = TicketBoardingMapper.INSTANCE.modelToEntity(ticketBoarding);
        ticketBoardingEntity.setTicketId(TicketIdMapper.INSTANCE.modelToEntity(ticketBoarding.getTicketId(ticketIdRepository)));
        ticketBoardingEntity.setTicketData(ticketDataEntity);
        return ticketBoardingEntity;
    }

    private static class ExportData {

        public final CompositeBuildResult result;
        public final ExchangeEvent event;

        protected ExportData(ExchangeEvent event, CompositeBuildResult result) {
            this.result = result;
            this.event = event;
        }
    }

    /**
     * Результат сборки данных
     */
    private static class CompositeBuildResult {

        /**
         * Запрос к серверу
         */
        private PushBoardingListRequest request;
        /**
         * Список локальных событий контроля, которые мы отправляем
         */
        private List<TicketControlEvent> ticketControlEventList;

    }

}
