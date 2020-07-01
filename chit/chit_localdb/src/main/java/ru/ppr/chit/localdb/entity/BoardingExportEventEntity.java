package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import ru.ppr.chit.localdb.greendao.DaoSession;
import ru.ppr.chit.localdb.greendao.BoardingEventEntityDao;
import ru.ppr.chit.localdb.greendao.EventEntityDao;
import ru.ppr.chit.localdb.greendao.BoardingExportEventEntityDao;
import ru.ppr.database.references.ReferenceInfo;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = BoardingExportEventEntity.TABLE_NAME)
public class BoardingExportEventEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "BoardingExportEvent";
    private static final String EventIdField = "EventId";
    private static final String BoardingEventIdField = "BoardingEventId";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {

        public Meta(){
            registerReference(EventEntity.TABLE_NAME, EventIdField, ReferenceInfo.ReferencesType.CASCADE);
            registerReference(BoardingEventEntity.TABLE_NAME, BoardingEventIdField);
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = EventIdField)
    private Long eventId;
    @ToOne(joinProperty = "eventId")
    private EventEntity event;
    @Property(nameInDb = BoardingEventIdField)
    private Long boardingEventId;
    @ToOne(joinProperty = "boardingEventId")
    private BoardingEventEntity boardingEvent;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 366066803)
    private transient BoardingExportEventEntityDao myDao;
    @Generated(hash = 1866724210)
    public BoardingExportEventEntity(Long id, Long eventId, Long boardingEventId) {
        this.id = id;
        this.eventId = eventId;
        this.boardingEventId = boardingEventId;
    }
    @Generated(hash = 985482611)
    public BoardingExportEventEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getEventId() {
        return this.eventId;
    }
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    public Long getBoardingEventId() {
        return this.boardingEventId;
    }
    public void setBoardingEventId(Long boardingEventId) {
        this.boardingEventId = boardingEventId;
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
    @Generated(hash = 1145002642)
    private transient Long boardingEvent__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1479642869)
    public BoardingEventEntity getBoardingEvent() {
        Long __key = this.boardingEventId;
        if (boardingEvent__resolvedKey == null
                || !boardingEvent__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            BoardingEventEntityDao targetDao = daoSession
                    .getBoardingEventEntityDao();
            BoardingEventEntity boardingEventNew = targetDao.load(__key);
            synchronized (this) {
                boardingEvent = boardingEventNew;
                boardingEvent__resolvedKey = __key;
            }
        }
        return boardingEvent;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2000920684)
    public void setBoardingEvent(BoardingEventEntity boardingEvent) {
        synchronized (this) {
            this.boardingEvent = boardingEvent;
            boardingEventId = boardingEvent == null ? null : boardingEvent.getId();
            boardingEvent__resolvedKey = boardingEventId;
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
    @Generated(hash = 1849775132)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBoardingExportEventEntityDao()
                : null;
    }

}
