package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.repository.local.BoardingEventRepository;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.TicketBoardingRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Событие контроля билета
 *
 * @author Dmitry Nevolin
 */
public class TicketControlEvent implements LocalModelWithId<Long> {

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
     * Идентификатор посадки
     */
    private Long ticketBoardingId;
    /**
     * Посадка
     */
    private TicketBoarding ticketBoarding;
    /**
     * Статус
     */
    private Status status;
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

    //region Event getters and setters
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
    //endregion

    //region TicketBoarding getters and setters
    public Long getTicketBoardingId() {
        return ticketBoardingId;
    }

    public void setTicketBoardingId(Long ticketBoardingId) {
        this.ticketBoardingId = ticketBoardingId;
        if (this.ticketBoarding != null && !ObjectUtils.equals(this.ticketBoarding.getId(), ticketBoardingId)) {
            this.ticketBoarding = null;
        }
    }

    public TicketBoarding getTicketBoarding(TicketBoardingRepository ticketBoardingRepository) {
        TicketBoarding local = ticketBoarding;
        if (local == null && ticketBoardingId != null) {
            synchronized (this) {
                if (ticketBoarding == null) {
                    ticketBoarding = ticketBoardingRepository.load(ticketBoardingId);
                }
            }
            return ticketBoarding;
        }
        return local;
    }

    public void setTicketBoarding(TicketBoarding ticketBoarding) {
        this.ticketBoarding = ticketBoarding;
        this.ticketBoardingId = ticketBoarding != null ? ticketBoarding.getId() : null;
    }
    //endregion

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    //region BoardingEvent getters and setters
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
    //endregion

    /**
     * Статус события контроля
     */
    public enum Status {

        /**
         * Создано
         */
        CREATED(10),
        /**
         * Готово к выгрузке
         */
        COMPLETED(20);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status valueOf(int code) {
            for (Status status : Status.values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }
            return null;
        }

    }

}
