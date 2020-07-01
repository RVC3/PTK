package ru.ppr.chit.domain.exchangeevent;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.domain.repository.local.ExchangeEventRepository;

/**
 * Выполняет сохранение событий обмена данными с БС со статусами SUCCESS/ERROR
 *
 * @author Dmitry Nevolin
 */
public class CompletedExchangeEventStoreInteractor {

    private final ExchangeEventRepository exchangeEventRepository;

    @Inject
    CompletedExchangeEventStoreInteractor(ExchangeEventRepository exchangeEventRepository) {
        this.exchangeEventRepository = exchangeEventRepository;
    }

    public void store(ExchangeEvent exchangeEvent) {
        // Обновляем в БД событие обмена данными
        exchangeEventRepository.update(exchangeEvent);
    }

}
