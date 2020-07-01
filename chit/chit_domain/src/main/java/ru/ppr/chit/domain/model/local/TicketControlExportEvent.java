package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.TicketControlEventRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * @author Dmitry Nevolin
 */
public class TicketControlExportEvent implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Идентификатор события
     */
    private Long eventId;
    /**
     * Событие
     */
    private Event event;
    /**
     * Идентификатор события контроля
     */
    private Long ticketControlEventId;
    /**
     * Событие контроля
     */
    private TicketControlEvent ticketControlEvent;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
        if (this.event != null && !ObjectUtils.equals(this.event.getId(), eventId)) {
            this.event = null;
        }
    }

    public Event getEvent(EventRepository eventRepository) {
        Event local = event;
        if (local == null && eventId != null) {
            synchronized (this) {
                if (event == null) {
                    event = eventRepository.load(eventId);
                }
            }
            return event;
        }
        return local;
    }

    public void setEvent(Event event) {
        this.event = event;
        this.eventId = event != null ? event.getId() : null;
    }

    public Long getTicketControlEventId() {
        return ticketControlEventId;
    }

    public void setTicketControlEventId(Long ticketControlEventId) {
        this.ticketControlEventId = ticketControlEventId;
        if (this.ticketControlEvent != null && !ObjectUtils.equals(this.ticketControlEvent.getId(), ticketControlEventId)) {
            this.ticketControlEvent = null;
        }
    }

    public TicketControlEvent getTicketControlEvent(TicketControlEventRepository ticketControlEventRepository) {
        TicketControlEvent local = ticketControlEvent;
        if (local == null && ticketControlEventId != null) {
            synchronized (this) {
                if (ticketControlEvent == null) {
                    ticketControlEvent = ticketControlEventRepository.load(ticketControlEventId);
                }
            }
            return ticketControlEvent;
        }
        return local;
    }

    public void setTicketControlEvent(TicketControlEvent ticketControlEvent) {
        this.ticketControlEvent = ticketControlEvent;
        this.ticketControlEventId = ticketControlEvent != null ? ticketControlEvent.getId() : null;
    }

}
