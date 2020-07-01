package ru.ppr.chit.domain.exchangeevent;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.ExchangeEvent;

/**
 * Создаёт события обмена данными с БС со статусами SUCCESS/ERROR
 *
 * @author Dmitry Nevolin
 */
public class CompletedExchangeEventBuilder {

    private ExchangeEvent exchangeEvent;
    private ExchangeEvent.Status status;

    @Inject
    CompletedExchangeEventBuilder() {

    }

    public CompletedExchangeEventBuilder setExchangeEvent(ExchangeEvent exchangeEvent) {
        this.exchangeEvent = exchangeEvent;
        return this;
    }

    public CompletedExchangeEventBuilder setStatus(ExchangeEvent.Status status) {
        this.status = status;
        return this;
    }

    public ExchangeEvent build() {
        if (exchangeEvent == null || status == null) {
            throw new IllegalStateException("exchangeEvent and status required");
        }
        if (status == ExchangeEvent.Status.STARTED) {
            throw new IllegalStateException("status must be not STARTED");
        }
        // Заполняем собственные поля
        exchangeEvent.setStatus(status);
        return exchangeEvent;
    }

}
