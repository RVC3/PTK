package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import ru.ppr.chit.localdb.greendao.BoardingEventEntityDao;
import ru.ppr.chit.localdb.greendao.DaoSession;
import ru.ppr.chit.localdb.greendao.EventEntityDao;
import ru.ppr.chit.localdb.greendao.TripServiceEventEntityDao;
import ru.ppr.database.references.ReferenceInfo;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = BoardingEventEntity.TABLE_NAME)
public class BoardingEventEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "BoardingEvent";
    private static final String EventIdField = "EventId";
    private static final String TripServiceEventIdField = "TripServiceEventId";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {

        public Meta(){
            registerReference(EventEntity.TABLE_NAME, EventIdField, ReferenceInfo.ReferencesType.CASCADE);
            registerReference(TripServiceEventEntity.TABLE_NAME, TripServiceEventIdField);
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "BoardingUuid")
    private String boardingUuid;
    @Property(nameInDb = EventIdField)
    private Long eventId;
    @ToOne(joinProperty = "eventId")
    private EventEntity event;
    @Property(nameInDb = TripServiceEventIdField)
    private Long tripServiceEventId;
    @ToOne(joinProperty = "tripServiceEventId")
    private TripServiceEventEntity tripServiceEvent;
    @Property(nameInDb = "StartTime")
    private Date startTime;
    @Property(nameInDb = "endTime")
    private Date endTime;
    @Property(nameInDb = "StationCode")
    private Long stationCode;
    @Property(nameInDb = "Status")
    private Integer status;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1615105727)
    private transient BoardingEventEntityDao myDao;

    @Generated(hash = 749663753)
    public BoardingEventEntity(Long id, String boardingUuid, Long eventId,
                               Long tripServiceEventId, Date startTime, Date endTime, Long stationCode,
                               Integer status) {
        this.id = id;
        this.boardingUuid = boardingUuid;
        this.eventId = eventId;
        this.tripServiceEventId = tripServiceEventId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.stationCode = stationCode;
        this.status = status;
    }

    @Generated(hash = 1962888793)
    public BoardingEventEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBoardingUuid() {
        return this.boardingUuid;
    }

    public void setBoardingUuid(String boardingUuid) {
        this.boardingUuid = boardingUuid;
    }

    public Long getEventId() {
        return this.eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getTripServiceEventId() {
        return this.tripServiceEventId;
    }

    public void setTripServiceEventId(Long tripServiceEventId) {
        this.tripServiceEventId = tripServiceEventId;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Long getStationCode() {
        return this.stationCode;
    }

    public void setStationCode(Long stationCode) {
        this.stationCode = stationCode;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Generated(hash = 520039006)
    private transient Long event__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 1503876065)
    public EventEntity getEvent() {
        Long __key = this.eventId;
        if (event__resolvedKey == null || !event__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            EventEntityDao targetDao = daoSession.getEventEntityDao();
            EventEntity eventNew = targetDao.load(__key);
            synchronized (this) {
                event = eventNew;
                event__resolvedKey = __key;
            }
        }
        return event;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 38217897)
    public void setEvent(EventEntity event) {
        synchronized (this) {
            this.event = event;
            eventId = event == null ? null : event.getId();
            event__resolvedKey = eventId;
        }
    }

    @Generated(hash = 2131641471)
    private transient Long tripServiceEvent__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 1147958752)
    public TripServiceEventEntity getTripServiceEvent() {
        Long __key = this.tripServiceEventId;
        if (tripServiceEvent__resolvedKey == null
                || !tripServiceEvent__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TripServiceEventEntityDao targetDao = daoSession
                    .getTripServiceEventEntityDao();
            TripServiceEventEntity tripServiceEventNew = targetDao.load(__key);
            synchronized (this) {
                tripServiceEvent = tripServiceEventNew;
                tripServiceEvent__resolvedKey = __key;
            }
        }
        return tripServiceEvent;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 191872954)
    public void setTripServiceEvent(TripServiceEventEntity tripServiceEvent) {
        synchronized (this) {
            this.tripServiceEvent = tripServiceEvent;
            tripServiceEventId = tripServiceEvent == null ? null
                    : tripServiceEvent.getId();
            tripServiceEvent__resolvedKey = tripServiceEventId;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1903599315)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBoardingEventEntityDao() : null;
    }

}
