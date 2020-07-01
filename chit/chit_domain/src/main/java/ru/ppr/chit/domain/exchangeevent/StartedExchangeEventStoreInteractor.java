package ru.ppr.chit.domain.exchangeevent;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.BoardingExportEvent;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.domain.repository.local.BoardingExportEventRepository;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.ExchangeEventRepository;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;

/**
 * Выполняет сохранение событий обмена данными с БС со статусом STARTED
 *
 * @author Dmitry Nevolin
 */
public class StartedExchangeEventStoreInteractor {

    private final EventRepository eventRepository;
    private final ExchangeEventRepository exchangeEventRepository;
    private final LocalDbTransaction localDbTransaction;

    @Inject
    StartedExchangeEventStoreInteractor(EventRepository eventRepository,
                                        ExchangeEventRepository exchangeEventRepository,
                                        LocalDbTransaction localDbTransaction) {
        this.eventRepository = eventRepository;
        this.exchangeEventRepository = exchangeEventRepository;
        this.localDbTransaction = localDbTransaction;
    }

    public void store(ExchangeEvent exchangeEvent) {
        try {
            localDbTransaction.begin();
            // Добавляем в БД базовое событие
            Event event = exchangeEvent.getEvent(eventRepository);
            eventRepository.insert(event);
            exchangeEvent.setEventId(event.getId());
            // Добавляем в БД событие обмена данными
            exchangeEventRepository.insert(exchangeEvent);
            localDbTransaction.commit();
        } finally {
            localDbTransaction.end();
        }
    }

}
