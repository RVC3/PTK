package ru.ppr.chit.bs.synchronizer;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import ru.ppr.chit.bs.synchronizer.event.SyncEvent;
import ru.ppr.logger.Logger;

/**
 * Класс, информирующий о процессе синхронизации (нотификация событий синхронизации)
 *
 * @author m.sidorov
 */
@Singleton
public class SynchronizerInformer {

    public static final String TAG = Logger.makeLogTag(SynchronizerInformer.class);

    private final PublishSubject<SyncEvent> syncPublisher = PublishSubject.create();

    @Inject
    public SynchronizerInformer() {

    }

    public Observable<SyncEvent> getSynchronizePublisher() {
        return syncPublisher;
    }

    public void notify(SyncEvent event) {
        Logger.info(TAG, String.format("%s: %s", event.getSyncType().name(), event.getMessage()));
        syncPublisher.onNext(event);
    }

}
