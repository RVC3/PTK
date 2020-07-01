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
import ru.ppr.chit.localdb.greendao.EventEntityDao;
import ru.ppr.chit.localdb.greendao.ExchangeEventEntityDao;
import ru.ppr.database.references.ReferenceInfo;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = ExchangeEventEntity.TABLE_NAME)
public class ExchangeEventEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "ExchangeEvent";
    private static final String EventIdField = "EventId";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {

        public Meta(){
            registerReference(EventEntity.TABLE_NAME, EventIdField, ReferenceInfo.ReferencesType.CASCADE);
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
    @Property(nameInDb = "Type")
    private Integer type;
    @Property(nameInDb = "Status")
    private Integer status;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 817161416)
    private transient ExchangeEventEntityDao myDao;
    @Generated(hash = 1662125473)
    public ExchangeEventEntity(Long id, Long eventId, Integer type,
            Integer status) {
        this.id = id;
        this.eventId = eventId;
        this.type = type;
        this.status = status;
    }
    @Generated(hash = 466926890)
    public ExchangeEventEntity() {
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
    public Integer getType() {
        return this.type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public Integer getStatus() {
        return this.status;
    }
    public void setStatus(Integer status) {
        this.status = status;
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
    @Generated(hash = 223789043)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getExchangeEventEntityDao() : null;
    }


}
