package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.repository.local.BoardingEventRepository;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Событие выгрузки посадки
 *
 * @author Dmitry Nevolin
 */
public class BoardingExportEvent implements LocalModelWithId<Long> {

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
     * Идентификатор события посадки
     */
    private Long boardingEventId;
    /**
     * События посадки
     */
    private BoardingEvent boardingEvent;

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

    public Long getBoardingEventId() {
        return boardingEventId;
    }

    public void setBoardingEventId(Long boardingEventId) {
        this.boardingEventId = boardingEventId;
        if (this.boardingEvent != null && !ObjectUtils.equals(this.boardingEvent.getId(), boardingEventId)) {
            this.boardingEvent = null;
        }
    }

    public BoardingEvent getBoardingEvent(BoardingEventRepository boardingEventRepository) {
        BoardingEvent local = boardingEvent;
        if (local == null && boardingEventId != null) {
            synchronized (this) {
                if (boardingEvent == null) {
                    boardingEvent = boardingEventRepository.load(boardingEventId);
                }
            }
            return boardingEvent;
        }
        return local;
    }

    public void setBoardingEvent(BoardingEvent boardingEvent) {
        this.boardingEvent = boardingEvent;
        this.boardingEventId = boardingEvent != null ? boardingEvent.getId() : null;
    }

}
