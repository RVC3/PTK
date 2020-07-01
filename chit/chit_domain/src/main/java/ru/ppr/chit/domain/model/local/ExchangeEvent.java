package ru.ppr.chit.domain.model.local;

import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Событие обмена данными с БС
 *
 * @author Dmitry Nevolin
 */
public class ExchangeEvent implements LocalModelWithId<Long> {

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
     * Тип события
     */
    private Type type;
    /**
     * Статус события
     */
    private Status status;

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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Тип обмена данными с БС
     */
    public enum Type {

        /**
         * Синхронизация (по большому QR-коду)
         */
        SYNC(10),
        /**
         * Выгрузка событий контроля посадки
         */
        EXPORT_TICKET_BOARDING(20),
        /**
         * Выгрузка событий завешенной посадки
         */
        EXPORT_BOARDING_EVENT(21),
        /**
         * Загрузка списка билетов
         */
        LOAD_TICKET_LIST(30);

        private final int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        @Nullable
        public static Type valueOf(int code) {
            for (Type type : Type.values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }
            return null;
        }

    }

    /**
     * Статус попытки обмена данными с БС
     */
    public enum Status {

        /**
         * Результат пока неизвестен
         */
        STARTED(10),
        /**
         * Успех
         */
        SUCCESS(20),
        /**
         * Ошибка
         */
        ERROR(30);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        @Nullable
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
