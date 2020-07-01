package ru.ppr.chit.domain.ticketcontrolexport;

import javax.inject.Inject;

import ru.ppr.chit.domain.event.EventBuilder;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.TicketControlEvent;
import ru.ppr.chit.domain.model.local.TicketControlExportEvent;

/**
 * @author Dmitry Nevolin
 */
public class TicketControlExportEventBuilder {

    private final EventBuilder eventBuilder;

    private TicketControlEvent ticketControlEvent;

    @Inject
    TicketControlExportEventBuilder(EventBuilder eventBuilder) {
        this.eventBuilder = eventBuilder;
    }

    public TicketControlExportEventBuilder setTicketControlEvent(TicketControlEvent ticketControlEvent) {
        this.ticketControlEvent = ticketControlEvent;
        return this;
    }

    public TicketControlExportEvent build() {
        if (ticketControlEvent == null) {
            throw new IllegalStateException("ticketControlEvent required");
        }
        TicketControlExportEvent ticketControlExportEvent = new TicketControlExportEvent();
        // Заполняем базовое событие
        Event event = eventBuilder.build();
        ticketControlExportEvent.setEvent(event);
        // Заполняем собственные поля
        ticketControlExportEvent.setTicketControlEvent(ticketControlEvent);
        return ticketControlExportEvent;
    }

}
