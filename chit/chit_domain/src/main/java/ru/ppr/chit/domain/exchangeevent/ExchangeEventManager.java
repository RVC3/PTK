package ru.ppr.chit.domain.exchangeevent;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.logger.Logger;

/**
 * Менеджер событий обмена данными с БС, начинает и завершает события, оповещает подписчиков
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class ExchangeEventManager {

    private static final String TAG = Logger.makeLogTag(ExchangeEventManager.class);

    private final PublishSubject<ExchangeEvent> exchangeEventCompletedPublisher = PublishSubject.create();
    private final StartedExchangeEventBuilder startedExchangeEventBuilder;
    private final StartedExchangeEventStoreInteractor startedExchangeEventStoreInteractor;
    private final CompletedExchangeEventBuilder completedExchangeEventBuilder;
    private final CompletedExchangeEventStoreInteractor completedExchangeEventStoreInteractor;

    @Inject
    ExchangeEventManager(StartedExchangeEventBuilder startedExchangeEventBuilder,
                         StartedExchangeEventStoreInteractor startedExchangeEventStoreInteractor,
                         CompletedExchangeEventBuilder completedExchangeEventBuilder,
                         CompletedExchangeEventStoreInteractor completedExchangeEventStoreInteractor) {
        this.startedExchangeEventBuilder = startedExchangeEventBuilder;
        this.startedExchangeEventStoreInteractor = startedExchangeEventStoreInteractor;
        this.completedExchangeEventBuilder = completedExchangeEventBuilder;
        this.completedExchangeEventStoreInteractor = completedExchangeEventStoreInteractor;
    }

    public ExchangeEvent startExchangeEvent(ExchangeEvent.Type exchangeEventType) {
        Logger.trace(TAG, "startExchangeEvent(exchangeEventType): " + exchangeEventType);
        ExchangeEvent exchangeEvent = startedExchangeEventBuilder
                .setType(exchangeEventType)
                .build();
        startedExchangeEventStoreInteractor.store(exchangeEvent);
        return exchangeEvent;
    }

    public ExchangeEvent completeExchangeEvent(ExchangeEvent exchangeEvent, ExchangeEvent.Status exchangeEventStatus) {
        Logger.trace(TAG, "startExchangeEvent(exchangeEventStatus): " + exchangeEventStatus);
        ExchangeEvent completedExchangeEvent = completedExchangeEventBuilder
                .setExchangeEvent(exchangeEvent)
                .setStatus(exchangeEventStatus)
                .build();
        completedExchangeEventStoreInteractor.store(completedExchangeEvent);
        exchangeEventCompletedPublisher.onNext(exchangeEvent);
        return completedExchangeEvent;
    }

    public Observable<ExchangeEvent> completedExchangeEvents() {
        return exchangeEventCompletedPublisher;
    }

}
