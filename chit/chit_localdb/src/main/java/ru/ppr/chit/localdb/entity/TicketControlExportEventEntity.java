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
import ru.ppr.chit.localdb.greendao.TicketControlEventEntityDao;
import ru.ppr.chit.localdb.greendao.EventEntityDao;
import ru.ppr.chit.localdb.greendao.TicketControlExportEventEntityDao;
import ru.ppr.database.references.ReferenceInfo;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = TicketControlExportEventEntity.TABLE_NAME)
public class TicketControlExportEventEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "TicketControlExportEvent";
    private static final String EventIdField = "EventId";
    private static final String TicketControlEventIdField = "TicketControlEventId";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {

        public Meta(){
            registerReference(EventEntity.TABLE_NAME, EventIdField, ReferenceInfo.ReferencesType.CASCADE);
            registerReference(TicketControlEventEntity.TABLE_NAME, TicketControlEventIdField);
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
    @Property(nameInDb = TicketControlEventIdField)
    private Long ticketControlEventId;
    @ToOne(joinProperty = "ticketControlEventId")
    private TicketControlEventEntity ticketControlEvent;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1133564769)
    private transient TicketControlExportEventEntityDao myDao;
    @Generated(hash = 1885556770)
    public TicketControlExportEventEntity(Long id, Long eventId,
            Long ticketControlEventId) {
        this.id = id;
        this.eventId = eventId;
        this.ticketControlEventId = ticketControlEventId;
    }
    @Generated(hash = 1181461225)
    public TicketControlExportEventEntity() {
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
    public Long getTicketControlEventId() {
        return this.ticketControlEventId;
    }
    public void setTicketControlEventId(Long ticketControlEventId) {
        this.ticketControlEventId = ticketControlEventId;
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
    @Generated(hash = 1936422978)
    private transient Long ticketControlEvent__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1045510154)
    public TicketControlEventEntity getTicketControlEvent() {
        Long __key = this.ticketControlEventId;
        if (ticketControlEvent__resolvedKey == null
                || !ticketControlEvent__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TicketControlEventEntityDao targetDao = daoSession
                    .getTicketControlEventEntityDao();
            TicketControlEventEntity ticketControlEventNew = targetDao.load(__key);
            synchronized (this) {
                ticketControlEvent = ticketControlEventNew;
                ticketControlEvent__resolvedKey = __key;
            }
        }
        return ticketControlEvent;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 603030468)
    public void setTicketControlEvent(TicketControlEventEntity ticketControlEvent) {
        synchronized (this) {
            this.ticketControlEvent = ticketControlEvent;
            ticketControlEventId = ticketControlEvent == null ? null
                    : ticketControlEvent.getId();
            ticketControlEvent__resolvedKey = ticketControlEventId;
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
    @Generated(hash = 1875963803)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null
                ? daoSession.getTicketControlExportEventEntityDao() : null;
    }

}
