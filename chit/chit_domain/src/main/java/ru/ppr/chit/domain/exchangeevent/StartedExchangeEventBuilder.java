package ru.ppr.chit.domain.exchangeevent;

import javax.inject.Inject;

import ru.ppr.chit.domain.event.EventBuilder;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.ExchangeEvent;

/**
 * Создаёт события обмена данными с БС со статусом STARTED
 *
 * @author Dmitry Nevolin
 */
public class StartedExchangeEventBuilder {

    private final EventBuilder eventBuilder;

    private ExchangeEvent.Type type;

    @Inject
    StartedExchangeEventBuilder(EventBuilder eventBuilder) {
        this.eventBuilder = eventBuilder;
    }

    public StartedExchangeEventBuilder setType(ExchangeEvent.Type type) {
        this.type = type;
        return this;
    }

    public ExchangeEvent build() {
        if (type == null) {
            throw new IllegalStateException("type required");
        }
        ExchangeEvent exchangeEvent = new ExchangeEvent();
        // Заполняем базовое событие
        Event event = eventBuilder.build();
        exchangeEvent.setEvent(event);
        // Заполняем собственные поля
        exchangeEvent.setStatus(ExchangeEvent.Status.STARTED);
        exchangeEvent.setType(type);
        return exchangeEvent;
    }

}
