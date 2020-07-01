package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;

import ru.ppr.chit.localdb.daosession.LocalDaoEntities;
import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import ru.ppr.chit.localdb.greendao.DaoSession;
import ru.ppr.chit.localdb.greendao.PassengerPersonalDataEntityDao;
import ru.ppr.chit.localdb.greendao.PlaceLocationEntityDao;
import ru.ppr.chit.localdb.greendao.TicketEntityDao;
import ru.ppr.chit.localdb.greendao.TicketIdEntityDao;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.DBGarbageCollectorHelper;
import ru.ppr.database.garbage.base.GCOldDataRemovable;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = TicketEntity.TABLE_NAME)
public class TicketEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "Ticket";
    private static final String TicketIdIdField = "TicketIdId";
    private static final String PassengerIdField = "PassengerId";
    private static final String PlaceLocationIdField = "PlaceLocationId";
    private static final String OldPlaceLocationIdField = "OldPlaceLocationId";
    private static final String DepartureDateField = "DepartureDate";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta implements GCOldDataRemovable {

        public Meta(){
            registerReference(TicketIdEntity.TABLE_NAME, TicketIdIdField);
            registerReference(PassengerPersonalDataEntity.TABLE_NAME, PassengerIdField);
            registerReference(PlaceLocationEntity.TABLE_NAME, PlaceLocationIdField);
            registerReference(PlaceLocationEntity.TABLE_NAME, OldPlaceLocationIdField);
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }

        @Override
        public void gcRemoveOldData(Database database, Date dateBefore) {
            // Удаляем старые билеты по дате билета
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ").append(TABLE_NAME).append(" where ").append(DepartureDateField).append(" < ").append(dateBefore.getTime());

            Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcRemoveOldData(): execute sql" + "\n" + sql.toString());
            database.execSQL(sql.toString());

            // Вместе с билетами удаляем соответствующие TicketId, у которых больше нет ссылок на Ticket (удалены предыдущей командой)
            ReferenceInfo ticketReference = LocalDaoEntities.references.getLink(TABLE_NAME, TicketIdIdField);
            sql = new StringBuilder(DBGarbageCollectorHelper.getDeleteNoLinkSql(ticketReference, TicketIdEntity.TABLE_NAME));

            Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcRemoveOldData(): execute sql" + "\n" + sql.toString());
            database.execSQL(sql.toString());
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = TicketIdIdField)
    private Long ticketIdId;
    @ToOne(joinProperty = "ticketIdId")
    private TicketIdEntity ticketId;
    @Property(nameInDb = "TrainThreadCode")
    private String trainThreadCode;
    @Property(nameInDb = "TrainNumber")
    private String trainNumber;
    @Property(nameInDb = "DepartureStationCode")
    private Long departureStationCode;
    @Property(nameInDb = "DestinationStationCode")
    private Long destinationStationCode;
    @Property(nameInDb = DepartureDateField)
    private Date departureDate;
    @Property(nameInDb = "TicketTypeCode")
    private Long ticketTypeCode;
    @Property(nameInDb = "ExemptionExpressCode")
    private Integer exemptionExpressCode;
    @Property(nameInDb = "TicketIssueType")
    private Integer ticketIssueType;
    @Property(nameInDb = "TicketState")
    private Integer ticketState;
    @Property(nameInDb = "StateDate")
    private Date stateDate;
    @Property(nameInDb = PassengerIdField)
    private Long passengerId;
    @ToOne(joinProperty = "passengerId")
    private PassengerPersonalDataEntity passenger;
    @Property(nameInDb = PlaceLocationIdField)
    private Long placeLocationId;
    @ToOne(joinProperty = "placeLocationId")
    private PlaceLocationEntity placeLocation;
    @Property(nameInDb = OldPlaceLocationIdField)
    private Long oldPlaceLocationId;
    @ToOne(joinProperty = "oldPlaceLocationId")
    private PlaceLocationEntity oldPlaceLocation;
    @Property(nameInDb = "NsiVersion")
    private int nsiVersion;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 709595447)
    private transient TicketEntityDao myDao;
    @Generated(hash = 615651002)
    public TicketEntity(Long id, Long ticketIdId, String trainThreadCode,
            String trainNumber, Long departureStationCode,
            Long destinationStationCode, Date departureDate, Long ticketTypeCode,
            Integer exemptionExpressCode, Integer ticketIssueType,
            Integer ticketState, Date stateDate, Long passengerId,
            Long placeLocationId, Long oldPlaceLocationId, int nsiVersion) {
        this.id = id;
        this.ticketIdId = ticketIdId;
        this.trainThreadCode = trainThreadCode;
        this.trainNumber = trainNumber;
        this.departureStationCode = departureStationCode;
        this.destinationStationCode = destinationStationCode;
        this.departureDate = departureDate;
        this.ticketTypeCode = ticketTypeCode;
        this.exemptionExpressCode = exemptionExpressCode;
        this.ticketIssueType = ticketIssueType;
        this.ticketState = ticketState;
        this.stateDate = stateDate;
        this.passengerId = passengerId;
        this.placeLocationId = placeLocationId;
        this.oldPlaceLocationId = oldPlaceLocationId;
        this.nsiVersion = nsiVersion;
    }
    @Generated(hash = 905377976)
    public TicketEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getTicketIdId() {
        return this.ticketIdId;
    }
    public void setTicketIdId(Long ticketIdId) {
        this.ticketIdId = ticketIdId;
    }
    public String getTrainThreadCode() {
        return this.trainThreadCode;
    }
    public void setTrainThreadCode(String trainThreadCode) {
        this.trainThreadCode = trainThreadCode;
    }
    public String getTrainNumber() {
        return this.trainNumber;
    }
    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }
    public Long getDepartureStationCode() {
        return this.departureStationCode;
    }
    public void setDepartureStationCode(Long departureStationCode) {
        this.departureStationCode = departureStationCode;
    }
    public Long getDestinationStationCode() {
        return this.destinationStationCode;
    }
    public void setDestinationStationCode(Long destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
    }
    public Date getDepartureDate() {
        return this.departureDate;
    }
    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }
    public Long getTicketTypeCode() {
        return this.ticketTypeCode;
    }
    public void setTicketTypeCode(Long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
    }
    public Integer getExemptionExpressCode() {
        return this.exemptionExpressCode;
    }
    public void setExemptionExpressCode(Integer exemptionExpressCode) {
        this.exemptionExpressCode = exemptionExpressCode;
    }
    public Integer getTicketIssueType() {
        return this.ticketIssueType;
    }
    public void setTicketIssueType(Integer ticketIssueType) {
        this.ticketIssueType = ticketIssueType;
    }
    public Integer getTicketState() {
        return this.ticketState;
    }
    public void setTicketState(Integer ticketState) {
        this.ticketState = ticketState;
    }
    public Date getStateDate() {
        return this.stateDate;
    }
    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }
    public Long getPassengerId() {
        return this.passengerId;
    }
    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }
    public Long getPlaceLocationId() {
        return this.placeLocationId;
    }
    public void setPlaceLocationId(Long placeLocationId) {
        this.placeLocationId = placeLocationId;
    }
    public Long getOldPlaceLocationId() {
        return this.oldPlaceLocationId;
    }
    public void setOldPlaceLocationId(Long oldPlaceLocationId) {
        this.oldPlaceLocationId = oldPlaceLocationId;
    }
    public int getNsiVersion() {
        return this.nsiVersion;
    }
    public void setNsiVersion(int nsiVersion) {
        this.nsiVersion = nsiVersion;
    }
    @Generated(hash = 1172539595)
    private transient Long ticketId__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 2102290187)
    public TicketIdEntity getTicketId() {
        Long __key = this.ticketIdId;
        if (ticketId__resolvedKey == null || !ticketId__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TicketIdEntityDao targetDao = daoSession.getTicketIdEntityDao();
            TicketIdEntity ticketIdNew = targetDao.load(__key);
            synchronized (this) {
                ticketId = ticketIdNew;
                ticketId__resolvedKey = __key;
            }
        }
        return ticketId;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1700979074)
    public void setTicketId(TicketIdEntity ticketId) {
        synchronized (this) {
            this.ticketId = ticketId;
            ticketIdId = ticketId == null ? null : ticketId.getId();
            ticketId__resolvedKey = ticketIdId;
        }
    }
    @Generated(hash = 2101617364)
    private transient Long passenger__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 32936141)
    public PassengerPersonalDataEntity getPassenger() {
        Long __key = this.passengerId;
        if (passenger__resolvedKey == null
                || !passenger__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PassengerPersonalDataEntityDao targetDao = daoSession
                    .getPassengerPersonalDataEntityDao();
            PassengerPersonalDataEntity passengerNew = targetDao.load(__key);
            synchronized (this) {
                passenger = passengerNew;
                passenger__resolvedKey = __key;
            }
        }
        return passenger;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1803777355)
    public void setPassenger(PassengerPersonalDataEntity passenger) {
        synchronized (this) {
            this.passenger = passenger;
            passengerId = passenger == null ? null : passenger.getId();
            passenger__resolvedKey = passengerId;
        }
    }
    @Generated(hash = 1045296305)
    private transient Long placeLocation__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 618826922)
    public PlaceLocationEntity getPlaceLocation() {
        Long __key = this.placeLocationId;
        if (placeLocation__resolvedKey == null
                || !placeLocation__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PlaceLocationEntityDao targetDao = daoSession
                    .getPlaceLocationEntityDao();
            PlaceLocationEntity placeLocationNew = targetDao.load(__key);
            synchronized (this) {
                placeLocation = placeLocationNew;
                placeLocation__resolvedKey = __key;
            }
        }
        return placeLocation;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1164655706)
    public void setPlaceLocation(PlaceLocationEntity placeLocation) {
        synchronized (this) {
            this.placeLocation = placeLocation;
            placeLocationId = placeLocation == null ? null : placeLocation.getId();
            placeLocation__resolvedKey = placeLocationId;
        }
    }
    @Generated(hash = 674990632)
    private transient Long oldPlaceLocation__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1756517545)
    public PlaceLocationEntity getOldPlaceLocation() {
        Long __key = this.oldPlaceLocationId;
        if (oldPlaceLocation__resolvedKey == null
                || !oldPlaceLocation__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PlaceLocationEntityDao targetDao = daoSession
                    .getPlaceLocationEntityDao();
            PlaceLocationEntity oldPlaceLocationNew = targetDao.load(__key);
            synchronized (this) {
                oldPlaceLocation = oldPlaceLocationNew;
                oldPlaceLocation__resolvedKey = __key;
            }
        }
        return oldPlaceLocation;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1893224843)
    public void setOldPlaceLocation(PlaceLocationEntity oldPlaceLocation) {
        synchronized (this) {
            this.oldPlaceLocation = oldPlaceLocation;
            oldPlaceLocationId = oldPlaceLocation == null ? null
                    : oldPlaceLocation.getId();
            oldPlaceLocation__resolvedKey = oldPlaceLocationId;
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
    @Generated(hash = 1912012593)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTicketEntityDao() : null;
    }


}
