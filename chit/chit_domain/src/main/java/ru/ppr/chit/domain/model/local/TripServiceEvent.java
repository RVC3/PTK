package ru.ppr.chit.domain.model.local;

import java.util.Date;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.TrainInfoRepository;
import ru.ppr.chit.domain.repository.local.UserRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Событие обслуживания поездки
 *
 * @author Dmitry Nevolin
 */
public class TripServiceEvent implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Идентификатор поездки
     */
    private String tripUuid;
    /**
     * Идентификатор события
     */
    private Long eventId;
    /**
     * Событие
     */
    private Event event;
    /**
     * Статус обслуживания поездки
     */
    private Status status;
    /**
     * Время начала обслуживания поездки
     */
    private Date startTime;
    /**
     * Время завершения обслуживания поездки
     */
    private Date endTime;
    /**
     * Идентификатор user
     */
    private Long userId;
    /**
     * Проводник, обслуживающий поездку
     */
    private User user;
    /**
     * Идентификатор trainInfo
     */
    private Long trainInfoId;
    /**
     * Информация об обслуживаемом поезде
     */
    private TrainInfo trainInfo;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getTripUuid() {
        return tripUuid;
    }

    public void setTripUuid(String tripUuid) {
        this.tripUuid = tripUuid;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
        if (this.user != null && !ObjectUtils.equals(this.user.getId(), userId)) {
            this.user = null;
        }
    }

    public User getUser(UserRepository userRepository) {
        User local = user;
        if (local == null && userId != null) {
            synchronized (this) {
                if (user == null) {
                    user = userRepository.load(userId);
                }
            }
            return user;
        }
        return local;
    }

    public void setUser(User user) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
    }

    public Long getTrainInfoId() {
        return trainInfoId;
    }

    public void setTrainInfoId(Long trainInfoId) {
        this.trainInfoId = trainInfoId;
        if (this.trainInfo != null && !ObjectUtils.equals(this.trainInfo.getId(), trainInfoId)) {
            this.trainInfo = null;
        }
    }

    public TrainInfo getTrainInfo(TrainInfoRepository trainInfoRepository) {
        TrainInfo local = trainInfo;
        if (local == null && trainInfoId != null) {
            synchronized (this) {
                if (trainInfo == null) {
                    trainInfo = trainInfoRepository.load(trainInfoId);
                }
            }
            return trainInfo;
        }
        return local;
    }

    public void setTrainInfo(TrainInfo trainInfo) {
        this.trainInfo = trainInfo;
        this.trainInfoId = trainInfo != null ? trainInfo.getId() : null;
    }

    /**
     * Статус обслуживания поездки
     */
    public enum Status {

        /**
         * Начато обслуживание поездки
         */
        STARTED(10),
        /**
         * Обслуживание поездки передано другому проводнику
         */
        TRANSFERRED(20),
        /**
         * Обслуживание поездки завершено
         */
        ENDED(30);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status valueOf(int code) {
            for (Status status : Status.values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return null;
        }

    }

}
