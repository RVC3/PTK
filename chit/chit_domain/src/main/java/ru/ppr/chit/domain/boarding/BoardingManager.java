package ru.ppr.chit.domain.boarding;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import ru.ppr.chit.domain.controlstation.ControlStationManager;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.domain.repository.local.BoardingEventRepository;
import ru.ppr.core.exceptions.UserException;
import ru.ppr.logger.Logger;

/**
 * Менеджер посадок.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class BoardingManager {

    private static final String TAG = Logger.makeLogTag(BoardingManager.class);

    private final BoardingEventRepository boardingEventRepository;
    private final Provider<StartedBoardingEventBuilder> startedBoardingEventBuilderProvider;
    private final Provider<EndedBoardingEventBuilder> endedBoardingEventBuilderProvider;
    private final StoreBoardingEventInteractor storeBoardingEventInteractor;
    private final BoardingStatusChecker boardingStatusChecker;
    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final ControlStationManager controlStationManager;
    private final LocalDbTransaction localDbTransaction;
    private final Subject<BoardingEvent.Status> statusSubject = BehaviorSubject.create();

    @Inject
    BoardingManager(BoardingEventRepository boardingEventRepository,
                    Provider<StartedBoardingEventBuilder> startedBoardingEventBuilderProvider,
                    Provider<EndedBoardingEventBuilder> endedBoardingEventBuilderProvider,
                    LocalDbTransaction localDbTransaction,
                    ControlStationManager controlStationManager,
                    StoreBoardingEventInteractor storeBoardingEventInteractor,
                    BoardingStatusChecker boardingStatusChecker,
                    TripServiceInfoStorage tripServiceInfoStorage) {
        this.boardingEventRepository = boardingEventRepository;
        this.startedBoardingEventBuilderProvider = startedBoardingEventBuilderProvider;
        this.endedBoardingEventBuilderProvider = endedBoardingEventBuilderProvider;
        this.storeBoardingEventInteractor = storeBoardingEventInteractor;
        this.boardingStatusChecker = boardingStatusChecker;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.controlStationManager = controlStationManager;
        this.localDbTransaction = localDbTransaction;
        // Актуализируем текущее состояние
        forceUpdate();
    }

    public boolean isBoardingStarted() {
        boolean isBoardingStarted = boardingStatusChecker.isStarted(boardingEventRepository.loadLast());
        Logger.trace(TAG, "isBoardingStarted: " + isBoardingStarted);
        return isBoardingStarted;
    }

    public BoardingEvent getLastBoardingEvent() {
        Logger.trace(TAG, "getLastBoardingEvent");
        return boardingEventRepository.loadLast();
    }

    /**
     * Создает событие начала посадки.
     */
    public void startBoarding() throws Exception {
        Logger.trace(TAG, "startBoarding");
        try {
            localDbTransaction.begin();
            // Устанавливаем следующую станцию контроля
            controlStationManager.setNextControlStation();
            // Создаем событие посадки и сохраняем изменения в БД в транзакции
            BoardingEvent boardingEvent = startedBoardingEventBuilderProvider.get().build();
            storeBoardingEventInteractor.store(boardingEvent);
            localDbTransaction.commit();

            statusSubject.onNext(boardingEvent.getStatus());
        } catch (Exception e) {
            tripServiceInfoStorage.clearControlStationCache();
            throw e;
        } finally {
            localDbTransaction.end();
        }
    }

    /**
     * Создает событие оконачания посадки.
     */
    public void endBoarding() throws Exception {
        Logger.trace(TAG, "endBoarding");
        BoardingEvent boardingEvent = endedBoardingEventBuilderProvider.get().build();
        storeBoardingEventInteractor.store(boardingEvent);
        statusSubject.onNext(boardingEvent.getStatus());
    }

    public Observable<BoardingEvent.Status> statusChanges() {
        return statusSubject;
    }

    private void forceUpdate() {
        Logger.trace(TAG, "forceUpdate");
        BoardingEvent boardingEvent = boardingEventRepository.loadLast();
        statusSubject.onNext(boardingEvent == null ? BoardingEvent.Status.ENDED : boardingEvent.getStatus());
    }

}
