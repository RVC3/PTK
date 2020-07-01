package ru.ppr.chit.bs.synchronizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import ru.ppr.chit.api.request.PingRequest;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.synchronizer.base.SynchronizeException;
import ru.ppr.chit.bs.synchronizer.base.Synchronizer;
import ru.ppr.chit.bs.synchronizer.event.SyncInfoEvent;
import ru.ppr.chit.domain.exchangeevent.ExchangeEventManager;
import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.logger.Logger;

/**
 * Глобальный синхронизатор, занимается закачкой всего и вся
 * во время первичной (пока единственной) синхронизации,
 * позже часть функционала будет вынесена отсюда.
 *
 * @author Dmitry Nevolin
 */
public class GlobalSynchronizer {

    private static final String TAG = Logger.makeLogTag(GlobalSynchronizer.class);

    private final ApiManager apiManager;
    private final SynchronizerInformer synchronizerInformer;
    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final LocalDbTransaction localDbTransaction;
    private final ExchangeEventManager exchangeEventManager;

    private List<Synchronizer> synchronizers = new ArrayList<>();
    private ExchangeEvent exchangeEvent;

    @Inject
    GlobalSynchronizer(ApiManager apiManager,
                       SynchronizerInformer synchronizerInformer,
                       TripServiceInfoStorage tripServiceInfoStorage,
                       LocalDbTransaction localDbTransaction,
                       NsiSynchronizer nsiSynchronizer,
                       SecuritySynchronizer securitySynchronizer,
                       SoftwareSynchronizer softwareSynchronizer,
                       SftSynchronizer sftSynchronizer,
                       TrainInfoSynchronizer trainInfoSynchronizer,
                       TicketListSynchronizer ticketListSynchronizer,
                       TicketBoardingSynchronizer ticketBoardingSynchronizer,
                       ExchangeEventManager exchangeEventManager) {
        this.apiManager = apiManager;
        this.synchronizerInformer = synchronizerInformer;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.localDbTransaction = localDbTransaction;

        this.exchangeEventManager = exchangeEventManager;

        // Формируем последовательность синхронизаторов. Последовательность важна!!!!
        // первой должна идти синхронизация SFT,
        // затем синхронизация нити поезда
        synchronizers.add(sftSynchronizer);
        synchronizers.add(trainInfoSynchronizer);
        synchronizers.add(nsiSynchronizer);
        synchronizers.add(securitySynchronizer);
        synchronizers.add(ticketListSynchronizer);
        synchronizers.add(ticketBoardingSynchronizer);
        synchronizers.add(softwareSynchronizer);
    }

    public Completable start() {
        return Completable
                .fromAction(() -> notify("Синхронизация с БС"))
                // Дерагаем ping для проверки доступа к базовой станции (во время синхронизации может быть пересоздан токне авторизации и прокси может вернуть ошибку доступа)
                .andThen(checkAuthAccess())
                // Создаём событие обмена данными
                .doOnComplete(this::exchangeEventStart)
                // Прогружаем все данные
                .andThen(loadAll())
                // Делаем резервные копии
                .andThen(backupAll())
                // Сохраняем все данные
                .andThen(applyAll())
                .doOnError(error -> exchangeEventError())
                .doOnComplete(this::exchangeEventSuccess)
                .onErrorResumeNext(error -> {
                    Logger.error(TAG, error);
                    return Completable.error(SynchronizeException.wrap(error, "Ошибка синхронизации с БС"));
                });
    }

    // уведомление о событии синхронизации
    public void notify(String message) {
        synchronizerInformer.notify(new SyncInfoEvent(Synchronizer.SynchronizeType.SYNC_GLOBAL, message));
    }

    private void exchangeEventStart() {
        exchangeEvent = exchangeEventManager.startExchangeEvent(ExchangeEvent.Type.SYNC);
    }

    private void exchangeEventSuccess() {
        exchangeEventManager.completeExchangeEvent(exchangeEvent, ExchangeEvent.Status.SUCCESS);
    }

    private void exchangeEventError() {
        exchangeEventManager.completeExchangeEvent(exchangeEvent, ExchangeEvent.Status.ERROR);
    }

    // Проверяет доступность базовой станции и авторизацию на ней
    private Completable checkAuthAccess() {
        return Single
                .fromCallable(() -> {
                    Logger.info(TAG, "check access bs");
                    return new PingRequest();
                })
                .flatMap(apiManager.api()::ping)
                .doOnError(error -> {
                    Logger.info(TAG, "check access bs failed", error);
                })
                .timeout(5, TimeUnit.SECONDS)
                .toCompletable()
                .onErrorResumeNext(error -> Completable.error(new SynchronizeException("Базовая станция недоступна", error)));
    }

    private Completable loadAll() {
        Logger.info(TAG, "Executing loadAll");
        return Observable
                .fromIterable(synchronizers)
                .flatMapCompletable(synchronizer -> {
                    Logger.info(TAG, String.format("%s: %s", synchronizer.getType().name(), "load data"));
                    return synchronizer.load();
                });
    }

    private Completable applyAll() {
        Logger.info(TAG, "Executing applyAll");
        return Completable
                .fromAction(localDbTransaction::begin)
                .andThen(Observable
                        .fromIterable(synchronizers)
                        .flatMapCompletable(synchronizer -> {
                            // Если данные были загружены, то сохраняем их
                            if (synchronizer.hasLoadedData()) {
                                Logger.info(TAG, String.format("%s: %s", synchronizer.getType().name(), "apply data"));
                                return synchronizer.apply()
                                        .onErrorResumeNext(error ->
                                                Completable.error(SynchronizeException.wrap(error, String.format(synchronizer.getTitle() + " - ошибка сохранения данных")))
                                        );
                            } else {
                                Logger.info(TAG, String.format("%s: %s", synchronizer.getType().name(), "no data to apply"));
                                return Completable.complete();
                            }
                        }))
                // Сохраняем транзакцию
                .doOnComplete(() -> {
                    localDbTransaction.commit();
                    localDbTransaction.end();
                    Logger.info(TAG, "applyAll: database commit");
                    tripServiceInfoStorage.clearCache();
                })
                // В случае ошибки отменяем транзакцию и восстанавливаем данные из бакапа
                .doOnError(error -> {
                    localDbTransaction.end();
                    Logger.info(TAG, "applyAll: database rollback");
                    restoreAll();
                    tripServiceInfoStorage.clearCache();
                });
    }

    private Completable backupAll() {
        Logger.info(TAG, "Executing backupAll");
        return Observable
                .fromIterable(synchronizers)
                .doOnNext(synchronizer -> {
                    synchronizer.getBackupManager().backup();
                })
                .ignoreElements();
    }

    private void restoreAll() throws Exception {
        Logger.info(TAG, "Executing restoreAll");
        for (Synchronizer synchronizer : synchronizers) {
            if (synchronizer.getBackupManager().hasBackup()) {
                synchronizer.getBackupManager().restore();
            }
        }
    }

}
