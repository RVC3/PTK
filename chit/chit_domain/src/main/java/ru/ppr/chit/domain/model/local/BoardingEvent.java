package ru.ppr.chit.domain.model.local;

import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.model.nsi.Station;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.chit.domain.repository.nsi.StationRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Событие посадки в поезд
 *
 * @author Dmitry Nevolin
 */
public class BoardingEvent implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Идентификатор посадки
     */
    private String boardingUuid;
    /**
     * Идентификатор события
     */
    private Long eventId;
    /**
     * Событие
     */
    private Event event;
    /**
     * Идентификатор события обслуживания поездки
     */
    private Long tripServiceEventId;
    /**
     * Событие обслуживания поездки
     */
    private TripServiceEvent tripServiceEvent;
    /**
     * Время начала посадки
     */
    private Date startTime;
    /**
     * Время завершения посадки
     */
    private Date endTime;
    /**
     * Код станции
     */
    private Long stationCode;
    /**
     * Станция
     */
    private Station station;
    /**
     * Статус посадки
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

    public String getBoardingUuid() {
        return boardingUuid;
    }

    public void setBoardingUuid(String boardingUuid) {
        this.boardingUuid = boardingUuid;
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


    public Long getTripServiceEventId() {
        return tripServiceEventId;
    }

    public void setTripServiceEventId(Long tripServiceEventId) {
        this.tripServiceEventId = tripServiceEventId;
        if (this.tripServiceEvent != null && !ObjectUtils.equals(this.tripServiceEvent.getId(), tripServiceEventId)) {
            this.tripServiceEvent = null;
        }
    }

    public TripServiceEvent getTripServiceEvent(TripServiceEventRepository tripServiceEventRepository) {
        TripServiceEvent local = tripServiceEvent;
        if (local == null && tripServiceEventId != null) {
            synchronized (this) {
                if (tripServiceEvent == null) {
                    tripServiceEvent = tripServiceEventRepository.load(tripServiceEventId);
                }
            }
            return tripServiceEvent;
        }
        return local;
    }

    public void setTripServiceEvent(TripServiceEvent tripServiceEvent) {
        this.tripServiceEvent = tripServiceEvent;
        this.tripServiceEventId = tripServiceEvent != null ? tripServiceEvent.getId() : null;
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

    //region Station getters and setters
    public Long getStationCode() {
        return stationCode;
    }

    public void setStationCode(Long stationCode) {
        this.stationCode = stationCode;
        if (station != null && !ObjectUtils.equals(station.getCode(), stationCode)) {
            station = null;
        }
    }

    public Station getStation(@NonNull StationRepository stationRepository, int versionId) {
        Station local = station;
        if (local == null && stationCode != null) {
            synchronized (this) {
                if (station == null) {
                    station = stationRepository.load(stationCode, versionId);
                }
            }
            return station;
        }
        return local;
    }

    public void setStation(Station station) {
        this.station = station;
        stationCode = station != null ? station.getCode() : null;
    }
    //endregion

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Статус посадки
     */
    public enum Status {

        /**
         * Начато обслуживание поездки
         */
        STARTED(10),
        /**
         * Обслуживание поездки завершено
         */
        ENDED(20);

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
