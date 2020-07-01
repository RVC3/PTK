package ru.ppr.chit.domain.tripservice;

import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;

import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;
import ru.ppr.chit.domain.ticket.TicketDeleteInteractor;
import ru.ppr.logger.Logger;

/**
 * Менеджер обслуживаний поездок.
 *
 * @author Dmitry Nevolin
 */
public class TripServiceManager {

    private static final String TAG = Logger.makeLogTag(TripServiceManager.class);

    private final TripServiceEventRepository tripServiceEventRepository;
    private final Provider<StartedTripServiceEventBuilder> startedTripServiceEventBuilderProvider;
    private final Provider<TransferredTripServiceEventBuilder> transferredTripServiceEventBuilderProvider;
    private final Provider<EndedTripServiceEventBuilder> endedTripServiceEventBuilderProvider;
    private final TripServiceEventStoreInteractor tripServiceEventStoreInteractor;
    private final EndedTripServiceEventStoreInteractor endedTripServiceEventStoreInteractor;
    private final TripServiceStatusChecker tripServiceStatusChecker;
    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final LocalDbTransaction localDbTransaction;
    private final TicketDeleteInteractor ticketDeleteInteractor;

    @Inject
    TripServiceManager(TripServiceEventRepository tripServiceEventRepository,
                       Provider<StartedTripServiceEventBuilder> startedTripServiceEventBuilderProvider,
                       Provider<TransferredTripServiceEventBuilder> transferredTripServiceEventBuilderProvider,
                       Provider<EndedTripServiceEventBuilder> endedTripServiceEventBuilderProvider,
                       TripServiceEventStoreInteractor tripServiceEventStoreInteractor,
                       EndedTripServiceEventStoreInteractor endedTripServiceEventStoreInteractor,
                       TripServiceStatusChecker tripServiceStatusChecker,
                       TripServiceInfoStorage tripServiceInfoStorage,
                       TicketDeleteInteractor ticketDeleteInteractor,
                       LocalDbTransaction localDbTransaction) {
        this.tripServiceEventRepository = tripServiceEventRepository;
        this.startedTripServiceEventBuilderProvider = startedTripServiceEventBuilderProvider;
        this.transferredTripServiceEventBuilderProvider = transferredTripServiceEventBuilderProvider;
        this.endedTripServiceEventBuilderProvider = endedTripServiceEventBuilderProvider;
        this.tripServiceEventStoreInteractor = tripServiceEventStoreInteractor;
        this.endedTripServiceEventStoreInteractor = endedTripServiceEventStoreInteractor;
        this.tripServiceStatusChecker = tripServiceStatusChecker;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.ticketDeleteInteractor = ticketDeleteInteractor;
        this.localDbTransaction = localDbTransaction;
    }

    public boolean isTripServiceStarted() {
        boolean isTripServiceStarted = tripServiceStatusChecker.isStarted(tripServiceEventRepository.loadLast());
        Logger.trace(TAG, "isTripServiceStarted: " + isTripServiceStarted);
        return isTripServiceStarted;
    }

    @Nullable
    public TripServiceEvent getLastTripService() {
        Logger.trace(TAG, "getLastTripService");
        return tripServiceEventRepository.loadLast();
    }

    /**
     * Создает событие начала обслуживания поездки.
     */
    public void startTripService() throws Exception {
        Logger.trace(TAG, "startTripService");
        TripServiceEvent tripServiceEvent = startedTripServiceEventBuilderProvider.get().build();
        tripServiceEventStoreInteractor.store(tripServiceEvent);
    }

    /**
     * Создает событие передачи обслуживания поездки.
     */
    public void transferTripService() throws Exception {
        Logger.trace(TAG, "transferTripService");
        TripServiceEvent tripServiceEvent = transferredTripServiceEventBuilderProvider.get().build();
        tripServiceEventStoreInteractor.store(tripServiceEvent);
    }

    /**
     * Создает событие оконачания обслуживания поездки.
     */
    public void endTripService() throws Exception {
        Logger.trace(TAG, "endTripService");
        try {
            localDbTransaction.begin();
            TripServiceEvent tripServiceEvent = endedTripServiceEventBuilderProvider.get().build();
            endedTripServiceEventStoreInteractor.store(tripServiceEvent);
            // Удаляем станцию контроля после завершения поездки
            tripServiceInfoStorage.clearControlStation();
            // Удаляем билеты, т.к. хранить их больше смысла нет
            ticketDeleteInteractor.deleteAll();
            localDbTransaction.commit();
        } finally {
            localDbTransaction.end();
            // Т.к. мы делаем TrainInfo устаревшим, надо очистить кеш где он лежит
            tripServiceInfoStorage.clearCache();
        }
    }

}
