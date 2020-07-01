package ru.ppr.chit.bs.load;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.api.mapper.TicketMapper;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.RegistrationInformant;
import ru.ppr.chit.bs.RegistrationState;
import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.domain.exchangeevent.ExchangeEventManager;
import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.domain.model.local.Ticket;
import ru.ppr.chit.domain.ticket.TicketStoreInteractor;
import ru.ppr.chit.domain.tripservice.TripServiceManager;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.manager.WiFiManager;
import ru.ppr.logger.Logger;

/**
 * Занимается загрузкой списка билетов
 *
 * @author Dmitry Nevolin
 */
public class TicketListLoader {

    private static final String TAG = Logger.makeLogTag(TicketListLoader.class);

    /**
     * Интервал загрузки (в минутах)
     */
    private static final long LOAD_INTERVAL = 2;

    private final TicketStoreInteractor ticketStoreInteractor;
    private final ExchangeEventManager exchangeEventManager;
    private final RegistrationInformant registrationInformant;
    private final WiFiManager wiFiManager;
    private final ApiManager apiManager;
    private final TripServiceManager tripServiceManager;
    private final LocalDbManager localDbManager;
    private final NsiDbManager nsiDbManager;

    private Disposable loadDisposable = Disposables.disposed();

    @Inject
    TicketListLoader(TicketStoreInteractor ticketStoreInteractor,
                     ExchangeEventManager exchangeEventManager,
                     RegistrationInformant registrationInformant,
                     WiFiManager wiFiManager,
                     ApiManager apiManager,
                     TripServiceManager tripServiceManager,
                     LocalDbManager localDbManager,
                     NsiDbManager nsiDbManager) {
        this.ticketStoreInteractor = ticketStoreInteractor;
        this.exchangeEventManager = exchangeEventManager;
        this.registrationInformant = registrationInformant;
        this.wiFiManager = wiFiManager;
        this.apiManager = apiManager;
        this.tripServiceManager = tripServiceManager;
        this.localDbManager = localDbManager;
        this.nsiDbManager = nsiDbManager;
    }

    public void start() {
        Logger.info(TAG, "start working");
        loadDisposable = Maybe
                .fromCallable(() -> {
                    boolean wiFiPointAvailable = wiFiManager.isPointAvailable();
                    boolean apiAvailable = apiManager.isApiAvailable();
                    // Проверяем доступность локальной базы (мы можем быть в процессе восстановления)
                    boolean localDbConnected = localDbManager.connectionState().blockingFirst();
                    // Проверяем доступность НСИ (мы можем быть в процессе восстановления)
                    boolean nsiDbConnected = nsiDbManager.connectionState().blockingFirst();
                    Logger.info(TAG, "wiFiPointAvailable: " + wiFiPointAvailable);
                    Logger.info(TAG, "apiAvailable: " + apiAvailable);
                    Logger.info(TAG, "localDbConnected: " + localDbConnected);
                    Logger.info(TAG, "nsiDbConnected: " + nsiDbConnected);
                    // Дальше проверяем только при наличии соединения с локальной базой, т.к. данные проверки осуществляются с её помощью
                    if (localDbConnected) {
                        // Флаг, уведомляющий о том, что регистрация на БС была, и неважно в каком состоянии сейчас
                        boolean prepared = registrationInformant.getRegistrationState() != RegistrationState.NOT_PREPARED;
                        // Качаем/докачиваем список билетов в фоне только во время обслуживаия
                        boolean tripServiceStarted = tripServiceManager.isTripServiceStarted();
                        Logger.info(TAG, "prepared: " + prepared);
                        Logger.info(TAG, "tripServiceStarted: " + tripServiceStarted);

                        return wiFiPointAvailable && apiAvailable && nsiDbConnected && prepared && tripServiceStarted;
                    }

                    return false;
                })
                .filter(Boolean.TRUE::equals)
                .flatMapCompletable(canExchange -> load())
                // Делаем загрузку каждые LOAD_INTERVAL минут если возможно
                .repeatWhen(flowable -> flowable.delay(LOAD_INTERVAL, TimeUnit.MINUTES))
                .subscribeOn(AppSchedulers.background())
                .subscribe();
    }

    private Completable load() {
        Logger.info(TAG, "load started");
        return Single
                // Создаём событие обмена данными
                .fromCallable(() -> exchangeEventManager.startExchangeEvent(ExchangeEvent.Type.LOAD_TICKET_LIST))
                .flatMapCompletable(exchangeEvent -> apiManager.api()
                        .getTicketList()
                        .map(response -> {
                            if (response.getError() == null) {
                                Logger.info(TAG, "getTicketList completed");
                                return Completable.fromAction(() -> {
                                    List<Ticket> ticketList = TicketMapper.INSTANCE.entityListToModelList(response.getTickets());
                                    Logger.info(TAG, "store ticketList");
                                    ticketStoreInteractor.storeAll(ticketList);
                                });
                            } else {
                                return Completable.error(new RuntimeException("getTicketList response.getError() = " + response.getError()));
                            }
                        })
                        .flatMapCompletable(completable -> completable
                                .andThen(Completable.fromAction(() -> {
                                    // Сохраняем событие обмена данными с успехом
                                    exchangeEventManager.completeExchangeEvent(exchangeEvent, ExchangeEvent.Status.SUCCESS);
                                    Logger.info(TAG, "load success");
                                })))
                        .onErrorResumeNext(error -> {
                            Logger.error(TAG, "error occurred during load:");
                            Logger.error(TAG, error);
                            // Сохраняем событие обмена данными с ошибкой
                            exchangeEventManager.completeExchangeEvent(exchangeEvent, ExchangeEvent.Status.ERROR);
                            return Completable.complete();
                        })
                        .doOnComplete(() -> Logger.info(TAG, "load finished")));
    }

}
