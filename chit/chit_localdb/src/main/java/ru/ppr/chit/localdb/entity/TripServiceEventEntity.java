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
import ru.ppr.chit.localdb.greendao.DaoSession;
import ru.ppr.chit.localdb.greendao.EventEntityDao;
import ru.ppr.chit.localdb.greendao.TripServiceEventEntityDao;
import ru.ppr.chit.localdb.greendao.UserEntityDao;
import ru.ppr.chit.localdb.greendao.TrainInfoEntityDao;
import ru.ppr.database.references.ReferenceInfo;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = TripServiceEventEntity.TABLE_NAME)
public class TripServiceEventEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "TripServiceEvent";
    private static final String EventIdField = "EventId";
    private static final String UserIdField = "UserId";
    private static final String TrainInfoIdField = "TrainInfoId";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {

        public Meta(){
            registerReference(EventEntity.TABLE_NAME, EventIdField, ReferenceInfo.ReferencesType.CASCADE);
            registerReference(UserEntity.TABLE_NAME, UserIdField);
            registerReference(TrainInfoEntity.TABLE_NAME, TrainInfoIdField);
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "TripUuid")
    private String tripUuid;
    @Property(nameInDb = EventIdField)
    private Long eventId;
    @ToOne(joinProperty = "eventId")
    private EventEntity event;
    @Property(nameInDb = "Status")
    private Integer status;
    @Property(nameInDb = "StartTime")
    private Date startTime;
    @Property(nameInDb = "EndTime")
    private Date endTime;
    @Property(nameInDb = UserIdField)
    private Long userId;
    @ToOne(joinProperty = "userId")
    private UserEntity user;
    @Property(nameInDb = TrainInfoIdField)
    private Long trainInfoId;
    @ToOne(joinProperty = "trainInfoId")
    private TrainInfoEntity trainInfo;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 197082263)
    private transient TripServiceEventEntityDao myDao;
    @Generated(hash = 19694160)
    public TripServiceEventEntity(Long id, String tripUuid, Long eventId,
            Integer status, Date startTime, Date endTime, Long userId,
            Long trainInfoId) {
        this.id = id;
        this.tripUuid = tripUuid;
        this.eventId = eventId;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userId = userId;
        this.trainInfoId = trainInfoId;
    }
    @Generated(hash = 264995261)
    public TripServiceEventEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTripUuid() {
        return this.tripUuid;
    }
    public void setTripUuid(String tripUuid) {
        this.tripUuid = tripUuid;
    }
    public Long getEventId() {
        return this.eventId;
    }
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    public Integer getStatus() {
        return this.status;
    }
    public void setStatus(Integer status) {
        this.status = status;
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
    public Long getUserId() {
        return this.userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getTrainInfoId() {
        return this.trainInfoId;
    }
    public void setTrainInfoId(Long trainInfoId) {
        this.trainInfoId = trainInfoId;
    }
    @Generated(hash = 520039006)
    private transient Long event__resolvedKey;
    /** To-one relationship, resolved on first access. */
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
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 38217897)
    public void setEvent(EventEntity event) {
        synchronized (this) {
            this.event = event;
            eventId = event == null ? null : event.getId();
            event__resolvedKey = eventId;
        }
    }
    @Generated(hash = 251390918)
    private transient Long user__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 2105083445)
    public UserEntity getUser() {
        Long __key = this.userId;
        if (user__resolvedKey == null || !user__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserEntityDao targetDao = daoSession.getUserEntityDao();
            UserEntity userNew = targetDao.load(__key);
            synchronized (this) {
                user = userNew;
                user__resolvedKey = __key;
            }
        }
        return user;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 720140878)
    public void setUser(UserEntity user) {
        synchronized (this) {
            this.user = user;
            userId = user == null ? null : user.getId();
            user__resolvedKey = userId;
        }
    }
    @Generated(hash = 862743012)
    private transient Long trainInfo__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 955538069)
    public TrainInfoEntity getTrainInfo() {
        Long __key = this.trainInfoId;
        if (trainInfo__resolvedKey == null
                || !trainInfo__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TrainInfoEntityDao targetDao = daoSession.getTrainInfoEntityDao();
            TrainInfoEntity trainInfoNew = targetDao.load(__key);
            synchronized (this) {
                trainInfo = trainInfoNew;
                trainInfo__resolvedKey = __key;
            }
        }
        return trainInfo;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 214715411)
    public void setTrainInfo(TrainInfoEntity trainInfo) {
        synchronized (this) {
            this.trainInfo = trainInfo;
            trainInfoId = trainInfo == null ? null : trainInfo.getId();
            trainInfo__resolvedKey = trainInfoId;
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
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2097231681)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTripServiceEventEntityDao()
                : null;
    }

}
